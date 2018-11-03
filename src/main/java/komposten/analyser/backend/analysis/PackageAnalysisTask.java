package komposten.analyser.backend.analysis;

import java.util.ArrayList;
import java.util.List;

import komposten.analyser.backend.PackageAnalyser;
import komposten.analyser.backend.PackageData;

public class PackageAnalysisTask implements StateRunnable
{
	private List<PackageData> packageList;
	private List<PackageData> originalPackageList;
	private PackageAnalyser packageAnalyser;
	private AnalysisListener analysisListener;
	private boolean hasFinished;
	

	/**
	 * @param packageDataList A <i>synchronised</i> list of PackageData objects.
	 *          Objects will be removed from the list as they are processed.
	 */
	public PackageAnalysisTask(List<PackageData> packageList, boolean analyseComments, boolean analyseStrings, AnalysisListener analysisListener)
	{
		this.packageList = packageList;
		this.originalPackageList = new ArrayList<>(packageList);
		this.packageAnalyser = new PackageAnalyser(analyseComments, analyseStrings);
		this.analysisListener = analysisListener;
	}
	
	
	@Override
	public boolean hasFinished()
	{
		return hasFinished;
	}


	@Override
	public void run()
	{
		try
		{
			PackageData packageData;
			
			while ((packageData = getNextPackage()) != null)
			{
				if (Thread.currentThread().isInterrupted())
					break;
				analysisListener.analysisAnalysingPackage(packageData, originalPackageList.size() - packageList.size(), originalPackageList.size());
				packageAnalyser.analysePackage(packageData, originalPackageList);
			}
			
			hasFinished = true;
		}
		catch (Exception e)
		{
			//NEXT_TASK PackageAnalysisTask; Exception handling
			System.err.println("Exception in thread " + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	
	private PackageData getNextPackage()
	{
		synchronized (packageList)
		{
			if (!packageList.isEmpty())
				return packageList.remove(0);
			else
				return null;
		}
	}
}
