package komposten.analyser.gui.views.statistics;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;

import komposten.analyser.backend.util.FrequencyStatistic;
import komposten.analyser.backend.util.ProportionStatistic;
import komposten.analyser.backend.util.Statistic;

public class StatisticsChartPanel extends JPanel
{
	private ChartPanel chartPanel;
	private DefaultPieDataset pieDataset;
	private HistogramDataset barDataset;
	private JFreeChart pieChart;
	private JFreeChart barChart;

	public StatisticsChartPanel()
	{
		pieDataset = new DefaultPieDataset();
		barDataset = new HistogramDataset();
		pieChart = ChartFactory.createPieChart(null, pieDataset);
		barChart = ChartFactory.createHistogram(null, null, null, barDataset);
		chartPanel = new ChartPanel(pieChart, true);
		chartPanel.setPopupMenu(null);
		chartPanel.setMouseZoomable(false);
		chartPanel.removeMouseListener(chartPanel);
		
		pieChart.setAntiAlias(true);
		barChart.setAntiAlias(true);
		
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
	}
	
	
	public void display(Statistic statistic)
	{
		if (statistic instanceof ProportionStatistic)
		{
			ProportionStatistic pStatistic = (ProportionStatistic) statistic;
			
			double[] values = pStatistic.getAllValues();
			for (int i = 0; i < values.length; i++)
			{
				pieDataset.clear();
				pieDataset.setValue(Integer.valueOf(i), values[i]);
			}
			
			chartPanel.setChart(pieChart);
		}
		else if (statistic instanceof FrequencyStatistic)
		{
			FrequencyStatistic fStatistic = (FrequencyStatistic) statistic;
			
			int[] values = fStatistic.getAllValues();
			double[] dValues = new double[values.length];
			for (int i = 0; i < values.length; i++)
			{
				dValues[i] = values[i];
			}
			
			barDataset = new HistogramDataset();
			barDataset.addSeries("series", dValues, 10);
			((XYPlot)barChart.getPlot()).setDataset(barDataset);
			chartPanel.setChart(barChart);
		}
		
		chartPanel.getChart().setTitle("Title");
		chartPanel.setVisible(true);
		chartPanel.repaint();
	}
	
	
	public void clear()
	{
		chartPanel.setVisible(false);
	}
}
