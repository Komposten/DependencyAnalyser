package komposten.analyser.gui.views.dependencies;

import komposten.analyser.gui.views.IconToggleButton;

public class InternalDependenciesButton extends IconToggleButton
{
	private DependencyPanel panel;


	public InternalDependenciesButton(DependencyPanel panel)
	{
		super("/buttons/show_dependencies_internal.png", "Show internal dependencies only.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleVertices(DependencyPanel.SHOW_INTERNAL_DEPENDENCIES);
		panel.refreshGraph(true);
	}
}
