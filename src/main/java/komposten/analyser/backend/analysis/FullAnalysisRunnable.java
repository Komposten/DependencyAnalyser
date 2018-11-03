package komposten.analyser.backend.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import komposten.analyser.backend.Cycle;
import komposten.analyser.backend.GraphCycleFinder;
import komposten.analyser.backend.GraphCycleFinder.GraphNode;
import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.analysis.AnalysisListener.AnalysisStage;
import komposten.analyser.backend.analysis.AnalysisListener.AnalysisType;
import komposten.analyser.backend.util.Constants;
import komposten.utilities.tools.Graph.CircuitListener;

public class FullAnalysisRunnable extends AnalysisRunnable
{
	private ThreadPoolExecutor threadPool;
	
	private File sourceFolder;
	private boolean analyseComments;
	private boolean analyseStrings;
	
	private GraphCycleFinder cycleFinder;
	private volatile boolean finished;
	private volatile boolean abort;
	
	private List<PackageData> packages;


	public FullAnalysisRunnable(File sourceFolder, boolean analyseComments, boolean analyseStrings, ThreadPoolExecutor threadPool, AnalysisListener analysisListener)
	{
		super(analysisListener);
		this.sourceFolder = sourceFolder;
		this.analyseComments = analyseComments;
		this.analyseStrings = analyseStrings;
		this.threadPool = threadPool;
	}
	
	
	public List<PackageData> getPackages()
	{
		return packages;
	}
	
	
	@Override
	public boolean hasFinished()
	{
		return finished;
	}
	
	
	@Override
	public void abort()
	{
		abort = true;
		if (cycleFinder != null) cycleFinder.abort();
	}
	
	
	@Override
	public void cycleLimitReached()
	{
		analysisListener.analysisStageChanged(AnalysisStage.FindingPackagesInCycles);
		if (cycleFinder != null) cycleFinder.abort();
	}
	
	
	@Override
	public void run()
	{
		analyseSource(sourceFolder);
	}
	

	public void analyseSource(File sourceFolder)
	{
		analysisListener.analysisBegun(AnalysisType.Full, sourceFolder);
		analysisListener.analysisStageChanged(AnalysisStage.FindingPackages);
		
		packages = new ArrayList<>(getPackageList(sourceFolder));
		
		analysisListener.analysisStageChanged(AnalysisStage.AnalysingFiles);
		
		analysePackages(packages);
		
		analysisListener.analysisStageChanged(AnalysisStage.FindingCycles);
		List<Cycle> cycles = findAllCyclicDependencies(packages);
		
		if (cycles != null)
		{
			System.out.println("Cycle count: " + cycles.size());
			analysisListener.analysisComplete(AnalysisType.Full);
		}
		else if (!abort)
		{
			System.gc();
			findAllPackagesInCycles(packages);
			
			if (!abort)
			{
				analysisListener.analysisComplete(AnalysisType.Full);
			}
			else
			{
				analysisListener.analysisAborted(AnalysisType.Full);
			}
		}
		else if (abort)
		{
			analysisListener.analysisAborted(AnalysisType.Full);
		}
		
		System.gc();
		finished = true;
	}
	
	
	private List<PackageData> getPackageList(File sourceFolder)
	{
		//NEXT_TASK Figure out a better way to do this since we search multiple folders!
		analysisListener.analysisSearchingFolder(sourceFolder);
		
		if (abort)
			return null;
		
		PackageFinderTask packageFinder = new PackageFinderTask(sourceFolder, threadPool);
		threadPool.submit(packageFinder);
		
		while (!packageFinder.hasFinished())
		{
			if (abort)
				break;
			
			try
			{
				Thread.sleep(250);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return null;
			}
		}
		
		return packageFinder.getPackageList();
	}
	
	
	private void analysePackages(List<PackageData> packages)
	{
		List<PackageData> syncList = Collections.synchronizedList(new LinkedList<>(packages));
		List<StateRunnable> runnables = new ArrayList<>();
		
		for (int i = 0; i < threadPool.getCorePoolSize(); i++)
		{
			if (abort) break;
			PackageAnalysisTask task = new PackageAnalysisTask(syncList, analyseComments, analyseStrings, analysisListener);
			runnables.add(task);
		}
		
		for (StateRunnable task : runnables)
		{
			threadPool.submit(task);
		}
		
		boolean hasFinished = false;
		while (!hasFinished)
		{
			if (abort)
				break;
			
			try
			{
				Thread.sleep(250);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
			
			hasFinished = true;
			for (StateRunnable runnable : runnables)
			{
				if (!runnable.hasFinished())
					hasFinished = false;
			}
		}
	}
	
	
	private List<Cycle> findAllCyclicDependencies(List<PackageData> packages)
	{
		if (abort)
			return null;
		
		cycleFinder = new GraphCycleFinder(packages);
		boolean success = cycleFinder.findCycles(circuitListener);
		
		if (success)
		{
			List<GraphNode[]> cyclesFromFinder = cycleFinder.getCycles();
			List<Cycle> cycles = new ArrayList<>(cyclesFromFinder.size());

			for (GraphNode[] nodeArray : cyclesFromFinder)
			{
				if (abort)
					return null;
				PackageData[] dataArray = new PackageData[nodeArray.length];

				for (int i = 0; i < nodeArray.length; i++)
				{
					PackageData packageData = (PackageData)nodeArray[i];
					packageData.isInCycle = true;
					dataArray[i] = packageData;
				}

				Cycle cycle = new Cycle(dataArray);
				cycles.add(cycle);
				
				for (PackageData packageData : cycle.getPackages())
				{
					packageData.cycles.add(cycle);
				}
			}
			
			return cycles;
		}
		else
		{
			return null;
		}
	}
	
	
	private void findAllPackagesInCycles(List<PackageData> packages)
	{
		if (abort)
			return;
		
		cycleFinder = new GraphCycleFinder(packages);
		List<GraphNode> nodesInCycles = cycleFinder.findNodesInCycles(circuitListener);
		
		if (nodesInCycles != null)
		{
			for (GraphNode node : nodesInCycles)
			{
				if (abort)
					return;
				PackageData packageData = (PackageData)node;
				packageData.isInCycle = true;
			}
		}
	}
	
	
	private CircuitListener circuitListener = new CircuitListener()
	{
		@Override
		public void onNewCircuitCount(int newCount)
		{
			if (newCount <= Constants.CYCLE_LIMIT)
				analysisListener.analysisCurrentCycleCount(newCount);
			else
				cycleLimitReached();
		}

		@Override
		public void onNextVertex(int vertex, int processedCount, int vertexCount)
		{
			analysisListener.analysisAnalysingPackage(packages.get(vertex), processedCount, vertexCount);
		}
	};
}
