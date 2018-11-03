package komposten.analyser.gui.menubar;

import javax.swing.JOptionPane;

import komposten.analyser.gui.backend.AnalyserSettings;
import komposten.analyser.gui.backend.Backend;

public class ThreadCountItem extends AbstractMenuItem
{
	private Backend backend;


	public ThreadCountItem(Backend backend)
	{
		super("Set thread count", 't');
		this.backend = backend;
	}


	@Override
	protected void onClick()
	{
		String currentValueString = backend.getSettings().get(AnalyserSettings.THREAD_COUNT);
		int defaultValue = backend.getSettings().getDefaultThreadCount();
		int maxValue = backend.getSettings().getMaxThreadCount();
		int currentValue = (currentValueString == null ? defaultValue : Integer.parseInt(currentValueString));
		
		Integer[] options = new Integer[maxValue];
		for (int i = 0; i < options.length; i++)
			options[i] = i+1;
		
		String title = getText();
		String msg = String.format("Choose a thread count (default = %d)", defaultValue);
		Object newValue = JOptionPane.showInputDialog(getTopLevelAncestor(), msg, title, JOptionPane.QUESTION_MESSAGE, null, options, currentValue);
		
		backend.getSettings().set(AnalyserSettings.THREAD_COUNT, newValue.toString());
	}
}
