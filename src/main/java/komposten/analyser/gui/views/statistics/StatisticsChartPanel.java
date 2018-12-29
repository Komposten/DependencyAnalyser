package komposten.analyser.gui.views.statistics;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import komposten.analyser.backend.statistics.FrequencyStatistic;
import komposten.analyser.backend.statistics.ProportionStatistic;
import komposten.analyser.backend.statistics.Statistic;

public class StatisticsChartPanel extends JPanel
{
	private ChartPanel chartPanel;
	private DefaultPieDataset pieDataset;
	private JFreeChart pieChart;
	private HistogramChart barChart;

	public StatisticsChartPanel()
	{
		pieDataset = new DefaultPieDataset();
		pieChart = ChartFactory.createPieChart(null, pieDataset);
		barChart = HistogramChart.createChart();
		chartPanel = new ChartPanel(null, true);
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
			
			//LATER StatisticsChartPanel; Create a pie chart class similar to the HistogramChart to make it easier to edit.
			chartPanel.setChart(pieChart);
		}
		else if (statistic instanceof FrequencyStatistic)
		{
			FrequencyStatistic fStatistic = (FrequencyStatistic) statistic;
			
			HistogramData data = new HistogramData(10, fStatistic.getAllValues(), (int)Math.round(fStatistic.getValue()));
			
			barChart.setData(data);
			barChart.setTitle("Frequencies of lengths");
			barChart.setAxisTitles("Length (in lines)", "Frequency");
			chartPanel.setChart(barChart);
		}
	}
	
	
	public void clear()
	{
		chartPanel.setChart(null);
	}
}
