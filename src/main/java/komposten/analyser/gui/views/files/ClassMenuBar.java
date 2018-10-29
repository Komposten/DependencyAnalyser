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
		
		AllDependenciesButton buttonAll = new AllDependenciesButton(panel);
		BothDependenciesButton buttonBoth = new BothDependenciesButton(panel);
		IngoingDependenciesButton buttonIn = new IngoingDependenciesButton(panel);
		OutgoingDependenciesButton buttonOut = new OutgoingDependenciesButton(panel);
		
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
