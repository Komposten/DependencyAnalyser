package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconToggleButton;

public class OutgoingDependenciesButton extends IconToggleButton
{
	private GraphPanel<?, ?> panel;


	public OutgoingDependenciesButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/show_dependencies_selected_out.png", "Show only dependencies from the selected packages.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleEdges(GraphPanel.SHOW_ALL_EDGES_FROM_SELECTED);
		panel.refreshGraph(false);
	}
}
