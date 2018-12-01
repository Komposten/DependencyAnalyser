package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconButton;

public class ZoomResetButton extends IconButton
{
	private GraphPanel<?, ?> panel;


	public ZoomResetButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/zoom_reset.png", "Reset zoom (Ctrl+0)");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.zoomReset();
	}
}
