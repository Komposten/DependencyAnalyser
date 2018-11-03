package komposten.analyser.gui.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;

import komposten.analyser.gui.views.buttons.ZoomFitBothButton;
import komposten.analyser.gui.views.buttons.ZoomFitHeightButton;
import komposten.analyser.gui.views.buttons.ZoomFitWidthButton;
import komposten.analyser.gui.views.buttons.ZoomInButton;
import komposten.analyser.gui.views.buttons.ZoomOutButton;
import komposten.analyser.gui.views.buttons.ZoomResetButton;

public class ZoomMenuBar extends JPanel
{
	//FIXME Move ZoomMenuBar to gui.menubar, and the stuff in gui.menubar to gui.menubar.components?
	public ZoomMenuBar(GraphPanel<?, ?> panel)
	{
		setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
		IconButton buttonZoomIn = new ZoomInButton(panel);
		IconButton buttonZoomOut = new ZoomOutButton(panel);
		IconButton buttonZoomReset = new ZoomResetButton(panel);
		IconButton buttonZoomFitBoth = new ZoomFitBothButton(panel);
		IconButton buttonZoomFitWidth = new ZoomFitWidthButton(panel);
		IconButton buttonZoomFitHeight = new ZoomFitHeightButton(panel);
		
		add(buttonZoomIn);
		add(buttonZoomOut);
		add(buttonZoomReset);
		add(buttonZoomFitBoth);
		add(buttonZoomFitWidth);
		add(buttonZoomFitHeight);
	}
	

	protected void addSeparator()
	{
		JPanel separator = new JPanel();
		separator.setPreferredSize(new Dimension(10, 1));
		separator.setOpaque(false);
		separator.setBackground(new Color(0, 0, 0, 0));
		add(separator);
	}
}
