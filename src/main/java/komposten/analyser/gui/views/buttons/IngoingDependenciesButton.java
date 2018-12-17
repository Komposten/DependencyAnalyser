package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconToggleButton;

public class IngoingDependenciesButton extends IconToggleButton
{
	private GraphPanel<?, ?> panel;


	public IngoingDependenciesButton(GraphPanel<?, ?> panel, String tooltip)
	{
		super("/buttons/show_dependencies_selected_in.png", tooltip);
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleEdges(GraphPanel.SHOW_ALL_EDGES_TO_SELECTED);
		panel.refreshGraph(false);
	}
}
