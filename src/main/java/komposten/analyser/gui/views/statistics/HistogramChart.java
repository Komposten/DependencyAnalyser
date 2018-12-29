package komposten.analyser.gui.views.statistics;

import java.awt.BasicStroke;
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
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.util.PaintList;
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
		private PaintList paintList;
		private PaintList outlinePaintList;
		private PaintList fillPaintList;
		private PaintList highlightPaintList;
		private PaintList highlightOutlinePaintList;
		
		
		public BarRenderer(HistogramDataset dataset)
		{
			this.dataset = dataset;
			createPaints();
			
			setDefaultPaint(paintList.getPaint(0), false);
			setDefaultFillPaint(fillPaintList.getPaint(0), false);
			setDefaultOutlinePaint(outlinePaintList.getPaint(0), false);
			
			setDrawBarOutline(true);
			setDefaultOutlineStroke(new BasicStroke(2), false);
			
			setBarPainter(new StandardXYBarPainter());
		}


		private void createPaints()
		{
			Color[] colours = new Color[]
					{
							new Color(248, 118, 109),
							new Color(216, 144, 0),
							new Color(163, 165, 0),
							new Color(57, 182, 0),
							new Color(0, 191, 125),
							new Color(0, 191, 196),
							new Color(0, 176, 246),
							new Color(149, 144, 255),
							new Color(231, 107, 243),
							new Color(255, 98, 188)
					};
			Color[] fillColours = new Color[colours.length];
			Color[] outlineColours = new Color[colours.length];
			Color[] highlightColours = new Color[colours.length];
			Color[] highlightOutlineColours = new Color[colours.length];
			
			for (int i = 0; i < colours.length; i++)
			{
				fillColours[i] = colours[i].brighter();
				outlineColours[i] = colours[i].darker();
				highlightColours[i] = fillColours[i].brighter();
				highlightOutlineColours[i] = new Color(outlineColours[i].getRGB());
			}
			
			paintList = createPaintList(colours);
			fillPaintList = createPaintList(fillColours);
			outlinePaintList = createPaintList(outlineColours);
			highlightPaintList = createPaintList(highlightColours);
			highlightOutlinePaintList = createPaintList(highlightOutlineColours);
			
			for (int i = 0; i < paintList.size(); i++)
			{
				setSeriesPaint(i, paintList.getPaint(i), false);
				setSeriesFillPaint(i, fillPaintList.getPaint(i), false);
				setSeriesOutlinePaint(i, outlinePaintList.getPaint(i), false);
			}
		}


		private PaintList createPaintList(Color[] colours)
		{
			PaintList list = new PaintList();
			for (int i = 0; i < colours.length; i++)
			{
				list.setPaint(i, colours[i]);
			}
			return list;
		}
		
		
		@Override
		public Paint getItemPaint(int row, int column)
		{
			if (isHighlighted(row, column))
				return highlightPaintList.getPaint(row);
			return super.getItemPaint(row, column);
		}
		
		
		@Override
		public Paint getItemOutlinePaint(int row, int column)
		{
			if (isHighlighted(row, column))
				return highlightOutlinePaintList.getPaint(row);
			return super.getItemOutlinePaint(row, column);
		}
		
		
		private boolean isHighlighted(int row, int column)
		{
			return dataset.dataSeries.get(row).getHighlightedXValue() == column;
		}
	}
}
