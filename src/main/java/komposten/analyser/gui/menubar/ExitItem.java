package komposten.analyser.gui.menubar;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class ExitItem extends AbstractMenuItem
{

	public ExitItem()
	{
		super("Exit", 'x', KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
	}


	@Override
	protected void onClick()
	{
		Component parent = getParent();
		
		while (parent != null && !(parent instanceof JPopupMenu))
			parent = parent.getParent();

		if (parent != null && parent instanceof JPopupMenu)
		{
			parent = ((JPopupMenu)parent).getInvoker();
			
			Window window = SwingUtilities.getWindowAncestor(parent);
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		}
	}
}
