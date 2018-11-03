package komposten.analyser.backend.analysis;

public abstract class AnalysisRunnable implements Runnable
{
	protected AnalysisListener analysisListener;
	
	
	public AnalysisRunnable(AnalysisListener analysisListener)
	{
		this.analysisListener = analysisListener;
	}
	
	
	abstract void cycleLimitReached();
	public abstract void abort();
	public abstract boolean hasFinished();
}
