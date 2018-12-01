package komposten.analyser.gui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import komposten.analyser.backend.PackageData;
import komposten.analyser.backend.analysis.AnalysisListener;
import komposten.analyser.backend.util.Constants;
import komposten.analyser.gui.backend.AnalyserSettings;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.menubar.AnalyserMenuBar;
import komposten.utilities.logging.Level;
import komposten.utilities.logging.LogUtils;

public class AnalyserWindow extends JFrame
{
	private static final String UI_VERSION = "0.1-181201";
	
	private Backend backend;
	private AnalyserPanel panel;
	private AnalysisProgressDialog progressDialog;

	public AnalyserWindow()
	{
		super("Dependency Analyser " + UI_VERSION); 
		
		initialiseResources();
		
		backend = new Backend();
		backend.addAnalysisListener(analysisListener);
		
		progressDialog = new AnalysisProgressDialog(this, backend);
		panel = new AnalyserPanel(backend);
		
		createMenuBar();
		addWindowListener(windowListener);
		setContentPane(panel);
		setSize(1024, 768);
		setMinimumSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		
		if (backend.getSettings().getBoolean(AnalyserSettings.REMEMBER_LAST_PROJECT) == true)
			loadLastProject();
	}
	
	
	private void initialiseResources()
	{
		LogUtils.writeToFile("log.txt");
	}


	private void createMenuBar()
	{
		AnalyserMenuBar menuBar = new AnalyserMenuBar(backend);
		
		setJMenuBar(menuBar);
	}


	private void loadLastProject()
	{
		String lastProjectPath = backend.getSettings().get(AnalyserSettings.LAST_OPENED_PROJECT);
		
		backend.analyseFolder(lastProjectPath);
	}
	
	
	private AnalysisListener analysisListener = new AnalysisListener()
	{
		private File lastAnalysedSource;
		
		@Override
		public void analysisBegun(AnalysisType analysisType, final File sourceFolder)
		{
			if (analysisType == AnalysisType.Full)
				lastAnalysedSource = sourceFolder;
			
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					progressDialog.show(sourceFolder);
				}
			};
			
			SwingUtilities.invokeLater(runnable);
		}


		@Override
		public void analysisSearchingFolder(File folder)
		{
			progressDialog.setText("Current folder: " + folder);
		}


		@Override
		public void analysisAnalysingPackage(PackageData currentPackage, int packageIndex,
				int packageCount)
		{
			progressDialog.setText("Package " + packageIndex + "/" + packageCount + ":<br />" + currentPackage.fullName);
		}


		@Override
		public void analysisCurrentCycleCount(int currentCycleCount)
		{
			progressDialog.setText("Current cycle count: " + currentCycleCount);
		}
		
		
		@Override
		public void analysisStageChanged(AnalysisStage newStage)
		{
			switch (newStage)
			{
				case FindingPackages :
					progressDialog.setText("Finding packages...", "");
					break;
				case AnalysingFiles :
					progressDialog.setText("Analysing source files...", "");
					break;
				case FindingCycles :
					progressDialog.setText("Finding cycles...", "");
					break;
				case FindingPackagesInCycles :
					String title = "Cycle limit reached!";
					String message = String.format("More than %d cycles found!\nCycles will be analysed when packages are selected instead!", Constants.CYCLE_LIMIT);
					JOptionPane.showMessageDialog(AnalyserWindow.this, message, title, JOptionPane.INFORMATION_MESSAGE);
					progressDialog.setText("Too many cycles, listing packages in cycles instead...", "");
					break;
				default :
					break;
			}
		}
		
		
		@Override
		public void analysisPartiallyComplete(AnalysisType analysisType)
		{
			if (analysisType == AnalysisType.Package)
			{
				analysisComplete(analysisType);
				String title = "Showing a limited cycle count!";
				String message = String.format("Cycle limit reached! Only the first %d will be shown!", Constants.CYCLE_LIMIT);
				JOptionPane.showMessageDialog(AnalyserWindow.this, message, title, JOptionPane.INFORMATION_MESSAGE);
			}
		}
		
		
		@Override
		public void analysisComplete(AnalysisType analysisType)
		{
			if (analysisType == AnalysisType.Full)
				backend.getSettings().set(AnalyserSettings.LAST_OPENED_PROJECT, lastAnalysedSource.getPath());
			
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					progressDialog.setVisible(false);
					
					if (analysisType == AnalysisType.Package)
						panel.rebuildGraphs();
				}
			};
			
			SwingUtilities.invokeLater(runnable);
		}
		
		
		@Override
		public void analysisAborted(AnalysisType analysisType)
		{
			
		}
	};
	
	
	private WindowListener windowListener = new WindowAdapter()
	{
		@Override
		public void windowClosing(WindowEvent e)
		{
			try
			{
				backend.getSettings().saveToFile();
			}
			catch (IOException ex)
			{
				String msg = "Could not save the settings to file!";
				if (LogUtils.hasInitialised())
					LogUtils.log(Level.ERROR, AnalyserWindow.class.getName(), msg, ex, true);
				JOptionPane.showMessageDialog(AnalyserWindow.this, msg, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};


	public static void main(String[] args)
	{
		try
		{
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (UnsupportedLookAndFeelException e)
		{
			// handle exception
		}
		catch (ClassNotFoundException e)
		{
			// handle exception
		}
		catch (InstantiationException e)
		{
			// handle exception
		}
		catch (IllegalAccessException e)
		{
			// handle exception
		}
		
		new AnalyserWindow();
	}
}
