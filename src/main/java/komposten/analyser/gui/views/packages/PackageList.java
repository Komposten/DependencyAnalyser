package komposten.analyser.gui.views.packages;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import komposten.analyser.backend.AnalysisListener;
import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.PackageDataComparator;
import komposten.analyser.backend.Analyser.AnalysisStage;
import komposten.analyser.backend.Analyser.AnalysisType;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;

public class PackageList extends JList<PackageData>
{
	private Backend backend;

	private DefaultListModel<PackageData> listModel;
	private boolean onlyShowCycles;
	private String filter = "";
	
	private PackageData selection;


	public PackageList(Backend backend)
	{
		this.backend = backend;
		
		backend.addAnalysisListener(analysisListener);
		backend.addPropertyChangeListener(propertyChangeListener, Backend.SELECTED_PACKAGE);
		
		listModel = new DefaultListModel<PackageData>();
		
		setModel(listModel);
		setCellRenderer(new CellRenderer());
		addListSelectionListener(listListener);
	}



	public PackageList(ListModel<PackageData> dataModel)
	{
		super(dataModel);
	}
	
	
	private void notifyPackageSelected(PackageData packageData)
	{
		backend.setSelectedPackage(packageData);
	}
	
	
	public void setShowOnlyCycles(boolean onlyCycles) //FIXME PackageList; When changing mode, only reset/move the selection if the selected item was removed!
	{
		onlyShowCycles = onlyCycles;
		updateList(false);
	}


	public void setFilter(String text)
	{
		if (text == null)
			text = "";
		filter = text;
		
		updateList(false);
	}
	
	
	public void updateList(final boolean clearSelection)
	{
		final ArrayList<PackageData> packages = new ArrayList<PackageData>(backend.getAnalyser().getPackageData());
		packages.sort(new PackageDataComparator());
		
		if (onlyShowCycles)
			removeAllNotInCycles(packages);
		
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				PackageData value = getSelectedValue();
				
				listModel.removeAllElements();
				for (PackageData packageData : packages)
				{
					if (packageData.fullName.contains(filter))
						listModel.addElement(packageData);
				}
				
				if (clearSelection || value == null && selection == null) //FIXME PackageList; Re-factor this code to make it more readable!
				{
					setSelectedIndex(0);
				}
				else
				{
					if (value == null)
						value = selection;
					
					if (listModel.contains(value))
					{
						setSelectedValue(value, true);
					}
					else
					{
						clearSelection();
						notifyPackageSelected(value);
						selection = value;
					}
				}
			}
		};
		
		SwingUtilities.invokeLater(runnable);
	}
	
	
	private void removeAllNotInCycles(ArrayList<PackageData> packages)
	{
		Iterator<PackageData> iterator = packages.iterator();
		while (iterator.hasNext())
		{
			PackageData packageData = iterator.next();
			
			if (!packageData.isInCycle)
				iterator.remove();
		}
	}


	private AnalysisListener analysisListener = new AnalysisListener()
	{
		@Override
		public void analysisBegun(AnalysisType analysisType, File sourceFolder)
		{
		}
		
		
		@Override
		public void analysisStageChanged(AnalysisStage newStage)
		{
		}
		
		
		@Override
		public void analysisPartiallyComplete(AnalysisType analysisType)
		{
		}
		
		
		@Override
		public void analysisComplete(AnalysisType analysisType)
		{
			if (analysisType == AnalysisType.Full)
			{
				updateList(true);
				requestFocus();
			}
		}
		
		
		@Override
		public void analysisAborted(AnalysisType analysisType)
		{
		}


		@Override
		public void analysisSearchingFolder(File folder)
		{
		}


		@Override
		public void analysisAnalysingPackage(PackageData currentPackage, int packageIndex,
				int packageCount)
		{
		}


		@Override
		public void analysisCurrentCycleCount(int currentCycleCount)
		{
		}
	};
	
	
	private ListSelectionListener listListener = new ListSelectionListener()
	{
		@Override
		public void valueChanged(ListSelectionEvent event)
		{
			if (!event.getValueIsAdjusting())
			{
				PackageData selectedData = getSelectedValue();
				
				selection = selectedData;
				
				notifyPackageSelected(selectedData);
			}
		}
	};
	
	
	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChanged(String key, Object value)
		{
			if (key.equals(Backend.SELECTED_PACKAGE))
				setSelectedValue(value, true);
		}
	};
	
	
	private class CellRenderer implements ListCellRenderer<PackageData>
	{
	  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private Border emptyBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		private Border emptyBorder2 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		private Border compundBorder;
	  
		
		@Override
		public Component getListCellRendererComponent(JList<? extends PackageData> list,
				PackageData value, int index, boolean isSelected, boolean cellHasFocus)
		{
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(
					list, value.fullName, index, isSelected, cellHasFocus);

			if (!isSelected)
			{
				renderer.setBorder(emptyBorder2);
			}
			else
			{
				if (compundBorder == null)
					compundBorder = BorderFactory.createCompoundBorder(renderer.getBorder(), emptyBorder);
				renderer.setBorder(compundBorder);
			}
			
			if (backend.getAnalyser().isContainedInCycle(value))
				renderer.setForeground(getCycleTextColor(renderer));
			
			return renderer;
		}

		
		
		/**
		 * Convert the label's background colour from RGB to YIQ and set the
		 * foreground to a light or dark colour based on the Y value (brightness
		 * based on the eye's sensitivity for different colours).
		 * 
		 * @param label The label to adapt the colour for.
		 * @return
		 */
		private Color getCycleTextColor(JLabel label)
		{
			Color color = label.getBackground();
			double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
			return y >= 128 ? Color.RED.darker() : Color.RED.brighter();
		}
	}
}
