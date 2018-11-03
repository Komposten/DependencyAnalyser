package komposten.analyser.backend.analysis;

import java.io.File;

import komposten.analyser.backend.PackageData;

public interface AnalysisListener
{
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
	
	
	public void analysisBegun(AnalysisType analysisType, File sourceFolder);
	public void analysisStageChanged(AnalysisStage newStage);
	public void analysisSearchingFolder(File folder);
	public void analysisAnalysingPackage(PackageData currentPackage, int packageIndex, int packageCount);
	public void analysisCurrentCycleCount(int currentCycleCount);
	/**
	 * Called if the analysis was aborted but still produced a result (e.g. if a limit is reached).
	 * @param analysisType
	 */
	public void analysisPartiallyComplete(AnalysisType analysisType);
	public void analysisComplete(AnalysisType analysisType);
	public void analysisAborted(AnalysisType analysisType);
}
