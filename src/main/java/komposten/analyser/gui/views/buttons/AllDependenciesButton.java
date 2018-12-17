package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconToggleButton;

public class AllDependenciesButton extends IconToggleButton
{
	private GraphPanel<?, ?> panel;

	
	public AllDependenciesButton(GraphPanel<?, ?> panel, String tooltip)
	{
		super("/buttons/show_dependencies_all.png", tooltip);
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleEdges(GraphPanel.SHOW_ALL_EDGES);
		panel.refreshGraph(false);
	}
}
