package komposten.analyser.gui.views.buttons;

import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.IconToggleButton;

public class AllDependenciesButton extends IconToggleButton
{
	private GraphPanel<?, ?> panel;


	public AllDependenciesButton(GraphPanel<?, ?> panel)
	{
		super("/buttons/show_dependencies_all.png", "Show all dependencies for all packages.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleEdges(GraphPanel.SHOW_ALL_EDGES);
		panel.refreshGraph(false);
	}
}
