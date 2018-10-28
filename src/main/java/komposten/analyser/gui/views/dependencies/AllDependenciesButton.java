package komposten.analyser.gui.views.dependencies;

import komposten.analyser.gui.views.IconToggleButton;

public class AllDependenciesButton extends IconToggleButton
{
	private DependencyPanel panel;


	public AllDependenciesButton(DependencyPanel panel)
	{
		super("/buttons/show_dependencies_internal_external.png", "Show all dependencies.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleVertices(DependencyPanel.SHOW_ALL_VERTICES);
		panel.refreshGraph(true);
	}
}
