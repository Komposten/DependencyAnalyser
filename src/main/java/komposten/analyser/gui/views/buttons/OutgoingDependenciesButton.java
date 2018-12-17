package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconToggleButton;

public class OutgoingDependenciesButton extends IconToggleButton
{
	private GraphPanel<?, ?> panel;


	public OutgoingDependenciesButton(GraphPanel<?, ?> panel, String tooltip)
	{
		super("/buttons/show_dependencies_selected_out.png", tooltip);
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleEdges(GraphPanel.SHOW_ALL_EDGES_FROM_SELECTED);
		panel.refreshGraph(false);
	}
}
