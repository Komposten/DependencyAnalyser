package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconButton;

public class ZoomFitWidthButton extends IconButton
{
	private GraphPanel<?, ?> panel;


	public ZoomFitWidthButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/zoom_fit_width.png", "Fit graph to width.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.fitGraphToWidth();
	}
}
