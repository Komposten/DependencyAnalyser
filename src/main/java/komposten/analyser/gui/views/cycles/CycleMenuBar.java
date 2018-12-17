package komposten.analyser.gui.views.cycles;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;

import komposten.analyser.gui.views.ZoomMenuBar;
import komposten.analyser.gui.views.buttons.AllDependenciesButton;
import komposten.analyser.gui.views.buttons.BothDependenciesButton;
import komposten.analyser.gui.views.buttons.IngoingDependenciesButton;
import komposten.analyser.gui.views.buttons.OutgoingDependenciesButton;

public class CycleMenuBar extends ZoomMenuBar
{

	public CycleMenuBar(CyclePanel panel)
	{
		super(panel);
		
		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
		AllDependenciesButton buttonAll = new AllDependenciesButton(panel, "Show all dependencies for all packages.");
		BothDependenciesButton buttonBoth = new BothDependenciesButton(panel, "Show all dependencies for the selected packages.");
		IngoingDependenciesButton buttonIn = new IngoingDependenciesButton(panel, "Show only dependencies to the selected packages.");
		OutgoingDependenciesButton buttonOut = new OutgoingDependenciesButton(panel, "Show only dependencies from the selected packages.");
		
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
