package komposten.analyser.backend;

import java.io.File;

import komposten.analyser.backend.Analyser.AnalysisStage;
import komposten.analyser.backend.Analyser.AnalysisType;

public interface AnalysisListener
{
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
