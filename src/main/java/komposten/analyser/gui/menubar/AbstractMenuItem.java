package komposten.analyser.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public abstract class AbstractMenuItem extends JMenuItem
{
	
	
	public AbstractMenuItem(String name, char mnemonic)
	{
		this(name, mnemonic, null);
	}

	public AbstractMenuItem(String name, char mnemonic, KeyStroke accelerator)
	{
		super(name);
		
		setMnemonic(mnemonic);
		setAccelerator(accelerator);
		addActionListener(listener);
	}
	
	
	protected abstract void onClick();
	
	
	private ActionListener listener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			onClick();
		}
	};
}
