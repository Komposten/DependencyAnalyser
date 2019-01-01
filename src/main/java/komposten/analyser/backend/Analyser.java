package komposten.analyser.backend;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import komposten.analyser.backend.analysis.AnalysisListener;
import komposten.analyser.backend.analysis.AnalysisRunnable;
import komposten.analyser.backend.analysis.FullAnalysisRunnable;
import komposten.analyser.backend.analysis.PackageAnalysisRunnable;


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
	private List<PackageData> packages;
	
	private List<AnalysisListener> listeners;
	
	private AnalysisThread analysisThread;
	private AnalysisRunnable analysisRunnable;
	

	public Analyser()
	{
		packages = new ArrayList<>();
		listeners = new ArrayList<>();
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
		
		analysisRunnable = new FullAnalysisRunnable(sourceFolder, analyseComments, analyseStrings, analysisListener);
		analysisThread.postRunnable(analysisRunnable);
	}
	
	
	public void analysePackage(PackageData packageData)
	{
		if (analysisRunnable != null && !analysisRunnable.hasFinished())
			abortAnalysis();
		
		analysisRunnable = new PackageAnalysisRunnable(packageData, packages, analysisListener);
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
			for (File file : data.getSourceFiles())
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
	
	
	private AnalysisListener analysisListener = new AnalysisListener()
	{
		@Override
		public void analysisBegun(AnalysisType analysisType, File sourceFolder)
		{
			for (AnalysisListener analysisListener : listeners)
				analysisListener.analysisBegun(analysisType, sourceFolder);
		}
		
		
		@Override
		public void analysisStageChanged(AnalysisStage newStage)
		{
			for (AnalysisListener analysisListener : listeners)
				analysisListener.analysisStageChanged(newStage);
		}
		
		
		@Override
		public void analysisSearchingFolder(File folder)
		{
			for (AnalysisListener analysisListener : listeners)
				analysisListener.analysisSearchingFolder(folder);;
		}
		
		
		@Override
		public void analysisAnalysingPackage(PackageData currentPackage,
				int packageIndex, int packageCount)
		{
			for (AnalysisListener analysisListener : listeners)
				analysisListener.analysisAnalysingPackage(currentPackage, packageIndex, packageCount);
		}
		
		
		@Override
		public void analysisCurrentCycleCount(int currentCycleCount)
		{
			for (AnalysisListener analysisListener : listeners)
				analysisListener.analysisCurrentCycleCount(currentCycleCount);
		}
		
		
		@Override
		public void analysisPartiallyComplete(AnalysisType analysisType)
		{
			for (AnalysisListener analysisListener : listeners)
				analysisListener.analysisPartiallyComplete(analysisType);
		}
		
		
		@Override
		public void analysisComplete(AnalysisType analysisType)
		{
			if (analysisType == AnalysisType.Full)
			{
				packages = ((FullAnalysisRunnable)analysisRunnable).getPackages();
			}
			
			for (AnalysisListener analysisListener : listeners)
				analysisListener.analysisComplete(analysisType);
		}
		
		
		@Override
		public void analysisAborted(AnalysisType analysisType)
		{
			for (AnalysisListener analysisListener : listeners)
				analysisListener.analysisAborted(analysisType);
		}
	};
}
