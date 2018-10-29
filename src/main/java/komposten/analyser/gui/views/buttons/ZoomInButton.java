package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconButton;

public class ZoomInButton extends IconButton
{
	private GraphPanel<?, ?> panel;


	public ZoomInButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/zoom_in.png", "Zoom in");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.zoomIn();
	}
}
