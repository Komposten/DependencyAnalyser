package komposten.analyser.gui.views.statistics;

import java.io.Serializable;
import java.util.Arrays;

public class HistogramData implements Serializable
{
	private static final long serialVersionUID = 0L;
	
	private int bins;
	private double[] xValues;
	private int[] yValues;
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
		if (values.length == 0)
		{
			clear();
			return;
		}
		
		Arrays.sort(values);
		
		int min = values[0];
		int max = values[values.length-1];
		int span = max-min;
		
		double binW = span/(double)bins;
		
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
		highlightedXValue = 0;
		binWidth = 0;
	}
	
	
	public double[] getXValues()
	{
		return xValues;
	}
	
	
	public int[] getYValues()
	{
		return yValues;
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
