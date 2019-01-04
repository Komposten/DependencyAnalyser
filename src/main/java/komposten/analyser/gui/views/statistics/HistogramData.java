package komposten.analyser.gui.views.statistics;

import java.io.Serializable;
import java.util.Arrays;

public class HistogramData implements Serializable
{
	private static final long serialVersionUID = 0L;
	
	private int bins;
	private double[] xValues;
	private int[] yValues;
	private int maxYValue;
	private double binWidth;
	private int highlightedXValue;


	public HistogramData(int bins)
	{
		this(bins, new int[0]);
	}
	
	
	public HistogramData(int bins, int[] values)
	{
		this(bins, values, -1);
	}
	
	
	public HistogramData(int bins, int[] values, int highlighted)
	{
		this.bins = bins;
		setData(values, highlighted);
	}
	
	
	public void setData(int[] values, int highlighted)
	{
		clear();
		
		if (values.length == 0)
		{
			return;
		}
		
		Arrays.sort(values);
		
		double min = values[0];
		int span = values[values.length-1]-values[0]; //Not using max-min here to avoid rounding errors.
		
		int bins = this.bins;
		double binW = span/(double)bins;
		
		if (span == 0)
		{
			binW = 1;
			min = min + binW/2 - (binW*bins)/2;
		}
		
		double[] binPositions = new double[bins];
		
		for (int i = 0; i < bins; i++)
		{
			binPositions[i] = min + binW*i;
		}
		
		int[] frequencies = new int[bins];
		int bin = 0;
		int highlightedBin = -1;
		
		for (int value : values)
		{
			while (bin+1 < binPositions.length && value >= binPositions[bin+1])
			{
				bin++;
			}
			
			frequencies[bin]++;
			
			if (frequencies[bin] > maxYValue)
				maxYValue = frequencies[bin];
			
			if (highlighted == value || (highlighted > binPositions[bin]))
				highlightedBin = bin;
		}
		
		xValues = binPositions;
		yValues = frequencies;
		highlightedXValue = highlightedBin;
		binWidth = binW;
	}
	
	
	public void clear()
	{
		xValues = new double[0];
		yValues = new int[0];
		maxYValue = 0;
		highlightedXValue = 0;
		binWidth = 0;
	}
	
	
	public double[] getXValues()
	{
		return xValues;
	}
	
	
	public double getMinXValue()
	{
		return xValues[0];
	}
	
	
	public double getMaxXValue()
	{
		return xValues[xValues.length-1] + binWidth;
	}
	
	
	public int[] getYValues()
	{
		return yValues;
	}
	
	
	public double getMinYValue()
	{
		return 0;
	}
	
	
	public double getMaxYValue()
	{
		return maxYValue;
	}
	
	
	/**
	 * @return The index for the x-value to highlight, or -1 if there should be no
	 *         highlight.
	 */
	public int getHighlightedXValue()
	{
		return highlightedXValue;
	}
	
	
	public double getBinWidth()
	{
		return binWidth;
	}
}
