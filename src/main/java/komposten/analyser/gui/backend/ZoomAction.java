package komposten.analyser.gui.backend;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ZoomAction extends AbstractAction
{
	public static final int ZOOM_IN = 0;
	public static final int ZOOM_OUT = 1;
	public static final int ZOOM_RESET = 2;
	
	private int mode;
	private Zoomable zoomable;

	public ZoomAction(int mode, Zoomable zoomable)
	{
		this.mode = mode;
		this.zoomable = zoomable;
	}


	@Override
	public void actionPerformed(ActionEvent e)
	{
		switch (mode)
		{
			case ZOOM_IN :
				zoomable.zoomIn();
				break;
			case ZOOM_OUT :
				zoomable.zoomOut();
				break;
			case ZOOM_RESET :
				zoomable.zoomReset();
				break;
		}
	}
}
