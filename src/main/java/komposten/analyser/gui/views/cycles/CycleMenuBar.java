package komposten.analyser.gui.views.cycles;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import komposten.analyser.gui.views.buttons.AllDependenciesButton;
import komposten.analyser.gui.views.buttons.BothDependenciesButton;
import komposten.analyser.gui.views.buttons.IngoingDependenciesButton;
import komposten.analyser.gui.views.buttons.OutgoingDependenciesButton;

public class CycleMenuBar extends JPanel
{

	public CycleMenuBar(CyclePanel panel)
	{
		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
		AllDependenciesButton buttonAll = new AllDependenciesButton(panel);
		BothDependenciesButton buttonBoth = new BothDependenciesButton(panel);
		IngoingDependenciesButton buttonIn = new IngoingDependenciesButton(panel);
		OutgoingDependenciesButton buttonOut = new OutgoingDependenciesButton(panel);
		
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
