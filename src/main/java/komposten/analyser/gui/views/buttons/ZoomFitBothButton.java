package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconButton;

public class ZoomFitBothButton extends IconButton
{
	private GraphPanel<?, ?> panel;


	public ZoomFitBothButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/zoom_fit_both.png", "Fit graph to window.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.fitGraphToView();
	}
}
