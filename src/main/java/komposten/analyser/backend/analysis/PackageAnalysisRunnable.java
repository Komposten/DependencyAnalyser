package komposten.analyser.backend.analysis;

import java.util.ArrayList;
import java.util.List;

import komposten.analyser.backend.Cycle;
import komposten.analyser.backend.GraphCycleFinder;
import komposten.analyser.backend.GraphCycleFinder.GraphNode;
import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.analysis.AnalysisListener.AnalysisStage;
import komposten.analyser.backend.analysis.AnalysisListener.AnalysisType;
import komposten.analyser.backend.util.Constants;
import komposten.utilities.tools.Graph.CircuitListener;

public class PackageAnalysisRunnable extends AnalysisRunnable
{
	private List<PackageData> packageList;
	private PackageData packageData;
	private GraphCycleFinder cycleFinder;
	private volatile boolean finished;
	private volatile boolean abort;
	
	public PackageAnalysisRunnable(PackageData packageData, List<PackageData> packageList, AnalysisListener analysisListener)
	{
		super(analysisListener);
		this.packageList = packageList;
		this.packageData = packageData;
	}

	
	@Override
	public boolean hasFinished()
	{
		return finished;
	}
	

	@Override
	public void cycleLimitReached()
	{
		analysisListener.analysisStageChanged(AnalysisStage.FindingCycles);
		if (cycleFinder != null) cycleFinder.abort();
	}

	
	@Override
	public void abort()
	{
		abort = true;
		if (cycleFinder != null) cycleFinder.abort();
	}
	

	@Override
	public void run()
	{
		analysePackage(packageData);
	}
	
	
	private void analysePackage(PackageData packageData)
	{
		analysisListener.analysisBegun(AnalysisType.Package, packageData.folder);
		
		List<Cycle> cycles = findCyclicDependencies(packageData);
		
		if (cycles != null)
		{
			System.out.println("Cycle count in " + packageData + ": " + cycles.size());
			
			packageData.cycles.addAll(cycles);
			setCyclePropertiesForPackage(packageData);
			
			if (cycles.size() >= Constants.CYCLE_LIMIT)
				analysisListener.analysisPartiallyComplete(AnalysisType.Package);
			else
				analysisListener.analysisComplete(AnalysisType.Package);
		}
		else
		{
			analysisListener.analysisAborted(AnalysisType.Package);
		}
		
		finished = true;
	}


	private List<Cycle> findCyclicDependencies(PackageData packageData)
	{
		if (abort)
			return null;
		
		cycleFinder = new GraphCycleFinder(packageList);
		cycleFinder.findCycles(packageData, circuitListener, true);
		
		List<GraphNode[]> cyclesFromFinder = cycleFinder.getCycles();
		List<Cycle> cycles = new ArrayList<>(cyclesFromFinder.size());
		
		for (GraphNode[] nodeArray : cyclesFromFinder)
		{
			if (abort)
				return null;
			PackageData[] dataArray = new PackageData[nodeArray.length];
			
			for (int i = 0; i < nodeArray.length; i++)
			{
				PackageData nodePackage = (PackageData)nodeArray[i];
				dataArray[i] = nodePackage;
			}
			
			Cycle cycle = new Cycle(dataArray);
			cycles.add(cycle);
		}
		
		return cycles;
	}


	private void setCyclePropertiesForPackage(PackageData packageData)
	{
		int cycleCount = packageData.cycles.size();
		String cycleCountString;
		if (cycleCount <= Constants.CYCLE_LIMIT)
			cycleCountString = String.valueOf(cycleCount);
		else
			cycleCountString = ">" + Constants.CYCLE_LIMIT;
			
		int longestCycle = 0;
		for (Cycle cycle : packageData.cycles)
		{
			if (cycle.getPackages().length > longestCycle)
				longestCycle = cycle.getPackages().length;
		}
		
		packageData.packageProperties.set("Cycle count", cycleCountString);
		packageData.packageProperties.set("Longest cycle", longestCycle);
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
		}
	};
}
