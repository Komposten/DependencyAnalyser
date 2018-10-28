package komposten.analyser.gui.menubar;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import komposten.analyser.gui.backend.AnalyserSettings;
import komposten.analyser.gui.backend.Backend;

public class OpenItem extends AbstractMenuItem
{
	private Backend backend;
	private JFileChooser fileChooser;


	public OpenItem(Backend backend)
	{
		super("Open", 'o', KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		
		this.backend = backend;
		createFileChooser();
	}
	
	
	private void createFileChooser()
	{
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
	  UIManager.put("FileChooser.readOnly", Boolean.TRUE);  
	  
	  String location = backend.getSettings().get(AnalyserSettings.LAST_OPENED_DIRECTORY);
		fileChooser = new JFileChooser(location);
		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(fileFilter);
		
	  UIManager.put("FileChooser.readOnly", old);  
	}


	@Override
	protected void onClick()
	{
		showOpenDialog();
	}


	private void showOpenDialog()
	{
		int result = fileChooser.showOpenDialog(SwingUtilities.getRoot(this));
		
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File currentDirectory = fileChooser.getCurrentDirectory();
			File selectedFile = fileChooser.getSelectedFile();
			
			backend.getSettings().set(AnalyserSettings.LAST_OPENED_DIRECTORY, currentDirectory.getPath());
			backend.getSettings().updateRecentList(selectedFile.getPath());
			backend.analyseFolder(selectedFile);
		}
	}
	
	
	private FileFilter fileFilter = new FileFilter()
	{
		@Override
		public boolean accept(File file)
		{
			return file.isDirectory();
		}

		@Override
		public String getDescription()
		{
			return "Java source folder";
		}
	};
}
