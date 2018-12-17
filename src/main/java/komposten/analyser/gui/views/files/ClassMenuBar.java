package komposten.analyser.gui.views.files;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;

import komposten.analyser.gui.views.ZoomMenuBar;
import komposten.analyser.gui.views.buttons.AllDependenciesButton;
import komposten.analyser.gui.views.buttons.BothDependenciesButton;
import komposten.analyser.gui.views.buttons.IngoingDependenciesButton;
import komposten.analyser.gui.views.buttons.OutgoingDependenciesButton;

public class ClassMenuBar extends ZoomMenuBar
{

	public ClassMenuBar(ClassPanel panel)
	{
		super(panel);
		
		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		AllDependenciesButton buttonAll = new AllDependenciesButton(panel, "Show all dependencies for all classes.");
		BothDependenciesButton buttonBoth = new BothDependenciesButton(panel, "Show all dependencies for the selected classes.");
		IngoingDependenciesButton buttonIn = new IngoingDependenciesButton(panel, "Show only dependencies to the selected classes.");
		OutgoingDependenciesButton buttonOut = new OutgoingDependenciesButton(panel, "Show only dependencies from the selected classes.");
		
		addSeparator();
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(buttonAll);
		buttonGroup.add(buttonBoth);
		buttonGroup.add(buttonIn);
		buttonGroup.add(buttonOut);
		add(buttonAll);
		add(buttonBoth);
		add(buttonIn);
		add(buttonOut);
	}
}
