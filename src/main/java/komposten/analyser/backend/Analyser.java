package komposten.analyser.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import komposten.analyser.backend.GraphCycleFinder.GraphNode;
import komposten.analyser.backend.util.SourceUtil;
import komposten.utilities.tools.ExtensionFileFilter;
import komposten.utilities.tools.Graph.CircuitListener;


/**
 * A class for recursively going through a java source folder and packages
 * contained therein to find the different classes' dependencies and possible
 * cycles. <br />
 * <br />
 * After analysis with {@link #analyseSource(File)} or
 * {@link #analyseSource(String)} the data can be retrieved using
 * {@link #getPackageData()} and {@link #getCycles()}.
 * 
 * @version 1.0
 * @author Jakob Hjelm
 */
public class Analyser
{
	public static final int CYCLE_LIMIT = 100000;
	
	public enum AnalysisType
	{
		Full,
		Package
	}
	
	public enum AnalysisStage
	{
		FindingPackages,
		AnalysingFiles,
		FindingCycles,
		FindingPackagesInCycles
	}
	
	
	private static final String FILE_EXTENSION = ".java";
	
	private List<PackageData> packages;
	
	private List<AnalysisListener> listeners;
	
	private AnalysisThread analysisThread;
	private AnalysisRunnable analysisRunnable;
	

	public Analyser()
	{
		listeners = new ArrayList<AnalysisListener>();
		createThread();
	}


	private void createThread()
	{
		analysisThread = new AnalysisThread("Analyser Thread");
		analysisThread.start();
	}


	public void addListener(AnalysisListener listener)
	{
		listeners.add(listener);
	}
	
	
	public List<PackageData> getPackageData()
	{
		return packages;
	}
	
	
	public void analyseSource(String sourceFolder, boolean analyseComments, boolean analyseStrings)
	{
		analyseSource(new File(sourceFolder), analyseComments, analyseStrings);
	}
	
	
	public void analyseSource(File sourceFolder, boolean analyseComments, boolean analyseStrings)
	{
		if (!sourceFolder.exists() || !sourceFolder.isDirectory())
			throw new IllegalArgumentException("\"" + sourceFolder.getPath() + "\" does not exist or is not a folder!");
		
		if (analysisRunnable != null && !analysisRunnable.hasFinished())
			abortAnalysis();
		
		analysisRunnable = new FullAnalysisRunnable(sourceFolder, analyseComments, analyseStrings);
		analysisThread.postRunnable(analysisRunnable);
	}
	
	
	public void analysePackage(PackageData packageData)
	{
		if (analysisRunnable != null && !analysisRunnable.hasFinished())
			abortAnalysis();
		
		analysisRunnable = new PackageAnalysisRunnable(packageData);
		analysisThread.postRunnable(analysisRunnable);
	}
	
	
	public void abortAnalysis()
	{
		analysisRunnable.abort();
		analysisRunnable = null;
	}
	
	
	public void printFolderStructure()
	{
		for (PackageData data : packages)
		{
			System.out.println(data.fullName);
			for (File file : data.sourceFiles)
				System.out.println("--" + file.getName());
		}
	}


	public void printDependencies()
	{
		System.out.println("Printing all dependencies for all packages...");
		for (PackageData data : packages)
		{
			System.out.println("\n-----------------------------------------------------");
			if (data.dependencies.length > 0)
			{
				System.out.println("\"" + data.fullName + "\" depends on " + data.dependencies.length + " other packages:");
				for (Dependency dependency : data.dependencies)
					System.out.println("\t" + dependency.toString());
			}
			else
			{
				System.out.println("\"" + data.fullName + "\" does not depend on any other packages!");
			}
			System.out.println("-----------------------------------------------------");
		}

		System.out.println("\nDone!");
	}
	
	
	private void notifyAnalysisBegun(AnalysisType analysisType, File sourceFolder)
	{
		for (AnalysisListener analysisListener : listeners)
			analysisListener.analysisBegun(analysisType, sourceFolder);
	}
	
	
	private void notifyAnalysisStageChanged(AnalysisStage newStage)
	{
		for (AnalysisListener analysisListener : listeners)
			analysisListener.analysisStageChanged(newStage);
	}
	
	
	private void notifyAnalysisSearchingFolder(File folder)
	{
		for (AnalysisListener analysisListener : listeners)
			analysisListener.analysisSearchingFolder(folder);;
	}
	
	
	private void notifyAnalysisAnalysingPackage(PackageData currentPackage, int packageIndex, int packageCount)
	{
		for (AnalysisListener analysisListener : listeners)
			analysisListener.analysisAnalysingPackage(currentPackage, packageIndex, packageCount);
	}
	
	
	private void notifyAnalysisCurrentCycleCount(int currentCycleCount)
	{
		for (AnalysisListener analysisListener : listeners)
			analysisListener.analysisCurrentCycleCount(currentCycleCount);
	}
	
	
	private void notifyAnalysisPartiallyComplete(AnalysisType analysisType)
	{
		for (AnalysisListener analysisListener : listeners)
			analysisListener.analysisPartiallyComplete(analysisType);
	}
	
	
	private void notifyAnalysisComplete(AnalysisType analysisType)
	{
		for (AnalysisListener analysisListener : listeners)
			analysisListener.analysisComplete(analysisType);
	}
	
	
	private void notifyAnalysisAborted(AnalysisType analysisType)
	{
		for (AnalysisListener analysisListener : listeners)
			analysisListener.analysisAborted(analysisType);
	}
	
	
	private interface AnalysisRunnable extends Runnable
	{
		void cycleLimitReached();
		void abort();
		boolean hasFinished();
	}
	
	
	private class FullAnalysisRunnable implements AnalysisRunnable
	{
		private File sourceFolder;
		private boolean analyseComments;
		private boolean analyseStrings;
		
		private GraphCycleFinder cycleFinder;
		private volatile boolean finished;
		private volatile boolean abort;
		
		private List<PackageData> packages;


		public FullAnalysisRunnable(File sourceFolder, boolean analyseComments, boolean analyseStrings)
		{
			this.sourceFolder = sourceFolder;
			this.analyseComments = analyseComments;
			this.analyseStrings = analyseStrings;
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
			notifyAnalysisStageChanged(AnalysisStage.FindingPackagesInCycles);
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
			
			notifyAnalysisBegun(AnalysisType.Full, sourceFolder);
			notifyAnalysisStageChanged(AnalysisStage.FindingPackages);
			getPackageList(sourceFolder, sourceFolder, packages);
			notifyAnalysisStageChanged(AnalysisStage.AnalysingFiles);
			analysePackageDependencies(packages);
			notifyAnalysisStageChanged(AnalysisStage.FindingCycles);
			List<Cycle> cycles = findAllCyclicDependencies(packages);
			
			if (cycles != null)
			{
				System.out.println("Cycle count: " + cycles.size());
				Analyser.this.packages = packages;
//				Analyser.this.cycles = cycles;
				
				notifyAnalysisComplete(AnalysisType.Full);
			}
			else if (!abort)
			{
				System.gc();
				findAllPackagesInCycles(packages);
				
				if (!abort)
				{
					Analyser.this.packages = packages;
//					Analyser.this.cycles = null;
					notifyAnalysisComplete(AnalysisType.Full);
				}
				else
				{
					notifyAnalysisAborted(AnalysisType.Full);
				}
			}
			else if (abort)
			{
				notifyAnalysisAborted(AnalysisType.Full);
			}
			
			System.gc();
			finished = true;
		}


		private void getPackageList(File folder, File sourceFolder, List<PackageData> outputList)
		{
			notifyAnalysisSearchingFolder(folder);
			
			List<File> fileList = new ArrayList<File>();
			
			for (File file : folder.listFiles(new ExtensionFileFilter(true, FILE_EXTENSION)))
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
				notifyAnalysisAnalysingPackage(data, i++, packages.size());
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
	}
	
	
	private class PackageAnalysisRunnable implements AnalysisRunnable
	{
		private PackageData packageData;
		private GraphCycleFinder cycleFinder;
		private volatile boolean finished;
		private volatile boolean abort;
		
		public PackageAnalysisRunnable(PackageData packageData)
		{
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
			notifyAnalysisStageChanged(AnalysisStage.FindingCycles);
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
			notifyAnalysisBegun(AnalysisType.Package, packageData.folder);
			
			List<Cycle> cycles = findCyclicDependencies(packageData);
			
			if (cycles != null)
			{
				System.out.println("Cycle count in " + packageData + ": " + cycles.size());
				
				packageData.cycles.addAll(cycles);
				
				if (cycles.size() >= CYCLE_LIMIT)
					notifyAnalysisPartiallyComplete(AnalysisType.Package);
				else
					notifyAnalysisComplete(AnalysisType.Package);
			}
			else
			{
				notifyAnalysisAborted(AnalysisType.Package);
			}
		}


		private List<Cycle> findCyclicDependencies(PackageData packageData)
		{
			if (abort)
				return null;
			
			cycleFinder = new GraphCycleFinder(Analyser.this.packages);
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
	}
	
	
	private CircuitListener circuitListener = new CircuitListener()
	{
		@Override
		public void onNewCircuitCount(int newCount)
		{
			if (newCount <= CYCLE_LIMIT)
				notifyAnalysisCurrentCycleCount(newCount);
			else
				analysisRunnable.cycleLimitReached();
		}

		@Override
		public void onNextVertex(int vertex, int processedCount, int vertexCount)
		{
			if (analysisRunnable instanceof FullAnalysisRunnable)
			{
				FullAnalysisRunnable full = (FullAnalysisRunnable)analysisRunnable;
				notifyAnalysisAnalysingPackage(full.packages.get(vertex), processedCount, vertexCount);
			}
		}
	};
}
