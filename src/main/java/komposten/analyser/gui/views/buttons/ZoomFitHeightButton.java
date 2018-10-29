package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconButton;

public class ZoomFitHeightButton extends IconButton
{
	private GraphPanel<?, ?> panel;


	public ZoomFitHeightButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/zoom_fit_height.png", "Fit graph to window height.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.fitGraphToHeight();
	}
}
