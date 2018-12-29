package komposten.analyser.backend.analysis;

import java.util.List;

import komposten.analyser.backend.Cycle;
import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.PackageProperties;
import komposten.analyser.backend.statistics.CycleCountStatistic;
import komposten.analyser.backend.util.Constants;
import komposten.analyser.tools.Settings;

public abstract class AnalysisRunnable implements Runnable
{
	protected AnalysisListener analysisListener;
	protected Settings settings;
	
	
	public AnalysisRunnable(AnalysisListener analysisListener, Settings settings)
	{
		this.analysisListener = analysisListener;
		this.settings = settings;
	}
	
	
	abstract void cycleLimitReached();
	public abstract void abort();
	public abstract boolean hasFinished();


	protected void setCyclePropertiesForPackages(List<PackageData> packages)
	{
		int cycleCountThreshold = Integer.parseInt(settings.get(Constants.SettingKeys.CYCLE_COUNT_THRESHOLD));
		for (PackageData packageData : packages)
		{
			setCyclePropertiesForPackage(packageData, cycleCountThreshold);
		}
	}


	protected void setCyclePropertiesForPackage(PackageData packageData, int cycleCountThreshold)
	{
		int longestCycle = 0;
		for (Cycle cycle : packageData.cycles)
		{
			if (cycle.getPackages().length > longestCycle)
				longestCycle = cycle.getPackages().length;
		}
		
		PackageProperties cycleProperties = new PackageProperties();
		int cycleCount = packageData.cycles.size();
		
		cycleProperties.set("Cycle count", new CycleCountStatistic(cycleCount, cycleCountThreshold, Constants.CYCLE_LIMIT));
		cycleProperties.set("Longest cycle", longestCycle);
		
		packageData.packageProperties.set("Cycles", cycleProperties);
	}
}
