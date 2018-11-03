package komposten.analyser.backend.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import komposten.analyser.backend.Cycle;
import komposten.analyser.backend.GraphCycleFinder;
import komposten.analyser.backend.GraphCycleFinder.GraphNode;
import komposten.analyser.backend.PackageAnalyser;
import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.analysis.AnalysisListener.AnalysisStage;
import komposten.analyser.backend.analysis.AnalysisListener.AnalysisType;
import komposten.analyser.backend.util.Constants;
import komposten.analyser.backend.util.SourceUtil;
import komposten.utilities.tools.ExtensionFileFilter;
import komposten.utilities.tools.Graph.CircuitListener;

public class FullAnalysisRunnable extends AnalysisRunnable
{
	private File sourceFolder;
	private boolean analyseComments;
	private boolean analyseStrings;
	
	private GraphCycleFinder cycleFinder;
	private volatile boolean finished;
	private volatile boolean abort;
	
	private List<PackageData> packages;


	public FullAnalysisRunnable(File sourceFolder, boolean analyseComments, boolean analyseStrings, AnalysisListener analysisListener)
	{
		super(analysisListener);
		this.sourceFolder = sourceFolder;
		this.analyseComments = analyseComments;
		this.analyseStrings = analyseStrings;
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
		packages = new ArrayList<>();
		
		analysisListener.analysisBegun(AnalysisType.Full, sourceFolder);
		analysisListener.analysisStageChanged(AnalysisStage.FindingPackages);
		getPackageList(sourceFolder, sourceFolder, packages);
		analysisListener.analysisStageChanged(AnalysisStage.AnalysingFiles);
		analysePackageDependencies(packages);
		analysisListener.analysisStageChanged(AnalysisStage.FindingCycles);
		List<Cycle> cycles = findAllCyclicDependencies(packages);
		
		if (cycles != null)
		{
			System.out.println("Cycle count: " + cycles.size());
			//NEXT_TASK Analyser's AnalysisListener should retrieve the package data from this runnable when analysisComplete() is called, before propagating the event.
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


	private void getPackageList(File folder, File sourceFolder, List<PackageData> outputList)
	{
		analysisListener.analysisSearchingFolder(folder);
		
		List<File> fileList = new ArrayList<File>();
		
		for (File file : folder.listFiles(new ExtensionFileFilter(true, Constants.FILE_EXTENSION)))
		{
			if (abort)
				return;
			
			if (file.isDirectory())
				getPackageList(file, sourceFolder, outputList);
			else
				fileList.add(file);
		}
		
		if (!fileList.isEmpty())
		{
			File first = fileList.get(0);
			String packageName = findPackageName(first);
			
			if (packageName.isEmpty())
				packageName = "<default package>";
			
			PackageData data = new PackageData(packageName, folder, fileList.toArray(new File[fileList.size()]));
			
			if (!outputList.contains(data))
			{
				outputList.add(data);
			}
			else
			{
				PackageData original = outputList.get(outputList.indexOf(data));
				
				File[] files = Arrays.copyOf(original.sourceFiles, original.sourceFiles.length + data.sourceFiles.length);
				System.arraycopy(data.sourceFiles, 0, files, original.sourceFiles.length, data.sourceFiles.length);
				original.sourceFiles = files;
			}
		}
	}


	private String findPackageName(File sourceFile)
	{
		String packageName = "";
		
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile)))
		{
			boolean lastEndedInComment = false;
			String line = "";
			
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				StringBuilder builder = new StringBuilder(line);
				lastEndedInComment = SourceUtil.removeComments(builder, lastEndedInComment);
				
				line = builder.toString().trim();
				
				if (line.startsWith("package"))
				{
					int indexOfPackage = line.indexOf("package");
					packageName = line.substring(indexOfPackage+7, line.indexOf(";", indexOfPackage)).trim();
					break;
				}
				else if (line.startsWith("import") || line.matches("^(public)?\\s+(abstract|final)?\\s+(class|interface|enum).*$"))
				{
					break;
				}
			}
		}
		catch (IOException e)
		{
			//NEXT_TASK Fix exception handling.
		}
		
		return packageName;
	}


	private void analysePackageDependencies(List<PackageData> packages)
	{
		PackageAnalyser analyser = new PackageAnalyser(analyseComments, analyseStrings);
		
		int i = 1;
		for (PackageData data : packages)
		{
			if (abort) return;
			analysisListener.analysisAnalysingPackage(data, i++, packages.size());
			analyser.analysePackage(data, packages);
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
