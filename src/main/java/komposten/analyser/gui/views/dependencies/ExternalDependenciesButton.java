package komposten.analyser.gui.views.dependencies;

import komposten.analyser.gui.views.IconToggleButton;

public class ExternalDependenciesButton extends IconToggleButton
{
	private DependencyPanel panel;


	public ExternalDependenciesButton(DependencyPanel panel)
	{
		super("/buttons/show_dependencies_external.png", "Show external dependencies only.");
		
		this.panel = panel;
	}


	@Override
	protected void onClick()
	{
		panel.setVisibleVertices(DependencyPanel.SHOW_EXTERNAL_DEPENDENCIES);
		panel.refreshGraph(true);
	}
}
