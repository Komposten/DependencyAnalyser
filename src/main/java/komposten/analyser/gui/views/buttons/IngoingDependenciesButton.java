package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconToggleButton;

public class IngoingDependenciesButton extends IconToggleButton
{
	private GraphPanel<?, ?> panel;


	public IngoingDependenciesButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/show_dependencies_selected_in.png", "Show only dependencies to the selected packages.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleEdges(GraphPanel.SHOW_ALL_EDGES_TO_SELECTED);
		panel.refreshGraph(false);
	}
}
