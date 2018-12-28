package komposten.analyser.gui.views.statistics;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.AbstractIntervalXYDataset;

public class HistogramChart extends JFreeChart
{
	private static final long serialVersionUID = 0L;
	
	private XYPlot plot;
	private HistogramDataset dataset;
	
	
	public static HistogramChart createChart()
	{
		HistogramDataset dataset = new HistogramDataset();
		XYPlot plot = createPlot(dataset);
		
		return new HistogramChart(plot, dataset);
	}
	
	
	private static XYPlot createPlot(HistogramDataset dataset)
	{
		ValueAxis domainAxis = new NumberAxis();
		ValueAxis rangeAxis = new NumberAxis();
		XYBarRenderer renderer = new BarRenderer(dataset);
		
		return new XYPlot(dataset, domainAxis, rangeAxis, renderer);
	}
	

	private HistogramChart(XYPlot plot, HistogramDataset dataset)
	{
		super(null, DEFAULT_TITLE_FONT, plot, false);
		this.plot = plot;
		this.dataset = dataset;
	}
	
	
	public void setData(HistogramData data)
	{
		dataset.addSeries("data", data);
	}
	
	
	public void setAxisTitles(String xTitle, String yTitle)
	{
		plot.getDomainAxis().setLabel(xTitle);
		plot.getRangeAxis().setLabel(yTitle);
	}


	private static class HistogramDataset extends AbstractIntervalXYDataset
	{
		private static final long serialVersionUID = 0L;
		
		private Map<Comparable<?>, Integer> seriesIndices = new HashMap<>();
		private Map<Integer, Comparable<?>> seriesKeys = new HashMap<>();
		private List<HistogramData> dataSeries = new ArrayList<>();


		/**
		 * Adds the specified HistogramData as a data series. If a data series with
		 * the same key already exists, it will be replaced.
		 */
		public void addSeries(Comparable<?> key, HistogramData data)
		{
			int index = seriesIndices.getOrDefault(key, -1);

			if (index == -1)
			{
				seriesIndices.put(key, dataSeries.size());
				seriesKeys.put(dataSeries.size(), key);
				dataSeries.add(data);
			}
			else
			{
				dataSeries.set(index, data);
			}

			fireDatasetChanged();
		}


		@Override
		public int getItemCount(int series)
		{
			return dataSeries.get(series).getXValues().length;
		}


		@Override
		public Comparable<?> getSeriesKey(int series)
		{
			return seriesKeys.get(series);
		}


		@Override
		public Number getStartX(int series, int item)
		{
			return dataSeries.get(series).getXValues()[item];
		}


		@Override
		public Number getEndX(int series, int item)
		{
			HistogramData data = dataSeries.get(series);
			return data.getXValues()[item] + data.getBinWidth();
		}


		@Override
		public Number getStartY(int series, int item)
		{
			return 0;
		}


		@Override
		public Number getEndY(int series, int item)
		{
			return dataSeries.get(series).getYValues()[item];
		}


		@Override
		public Number getX(int series, int item)
		{
			return dataSeries.get(series).getXValues()[item];
		}


		@Override
		public Number getY(int series, int item)
		{
			return dataSeries.get(series).getYValues()[item];
		}


		@Override
		public int getSeriesCount()
		{
			return dataSeries.size();
		}
	}
	
	
	private static class BarRenderer extends XYBarRenderer
	{
		private HistogramDataset dataset;
		
		
		public BarRenderer(HistogramDataset dataset)
		{
			this.dataset = dataset;
		}
		
		
		@Override
		public Paint getItemPaint(int row, int column)
		{
			//CURRENT Create a complete set of colours to use for multiple series (with highlights)
			//				to use instead of the default!
			int highlightedBar = dataset.dataSeries.get(row).getHighlightedXValue();
			
			if (highlightedBar == column)
				return Color.GREEN;
			return super.getItemPaint(row, column);
		}
	}
}
