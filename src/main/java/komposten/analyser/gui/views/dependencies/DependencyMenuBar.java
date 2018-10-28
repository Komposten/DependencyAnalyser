package komposten.analyser.gui.views.dependencies;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;

public class DependencyMenuBar extends JPanel
{

	public DependencyMenuBar(DependencyPanel panel)
	{
		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
		AllDependenciesButton buttonAll = new AllDependenciesButton(panel);
		InternalDependenciesButton buttonInternal = new InternalDependenciesButton(panel);
		ExternalDependenciesButton buttonExternal = new ExternalDependenciesButton(panel);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(buttonAll);
		buttonGroup.add(buttonInternal);
		buttonGroup.add(buttonExternal);
		add(buttonAll);
		add(buttonInternal);
		add(buttonExternal);
	}
}
