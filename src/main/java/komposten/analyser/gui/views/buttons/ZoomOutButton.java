package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconButton;

public class ZoomOutButton extends IconButton
{
	private GraphPanel<?, ?> panel;


	public ZoomOutButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/zoom_out.png", "Zoom out (Ctrl+-)");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.zoomOut();
	}
}
