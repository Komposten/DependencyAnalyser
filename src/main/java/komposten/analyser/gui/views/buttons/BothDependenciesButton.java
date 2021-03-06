package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconToggleButton;

public class BothDependenciesButton extends IconToggleButton
{
	private GraphPanel<?, ?> panel;


	public BothDependenciesButton(GraphPanel<?, ?> panel, String tooltip)
	{
		super("/buttons/show_dependencies_selected_both.png", tooltip);
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleEdges(GraphPanel.SHOW_ALL_EDGES_FOR_SELECTED);
		panel.refreshGraph(false);
	}
}
