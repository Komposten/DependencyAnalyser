package komposten.analyser.gui;

import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;
import komposten.analyser.gui.backend.ZoomAction;
import komposten.analyser.gui.backend.Zoomable;
import komposten.analyser.gui.views.GraphPanel;
import komposten.analyser.gui.views.cycles.CyclePanel;
import komposten.analyser.gui.views.dependencies.DependencyPanel;
import komposten.analyser.gui.views.files.ClassPanel;

public class GraphTabPanel extends JTabbedPane implements Zoomable
{
	private DependencyPanel panelDependencies;
	private CyclePanel panelCycles;
	private ClassPanel panelClasses;

	public GraphTabPanel(Backend backend)
	{
		backend.addPropertyChangeListener(propertyListener, Backend.SELECTED_EDGE);
		
		panelCycles = new CyclePanel(backend);
		panelDependencies = new DependencyPanel(backend);
		panelClasses = new ClassPanel(backend);

		add("Dependencies", panelDependencies);
		add("Cycles", panelCycles);
		add("Classes", panelClasses);
		
		createKeyboardShortcuts();
	}


	private void createKeyboardShortcuts()
	{
		InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();
		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK, true), "zoomin");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK, true), "zoomout");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK, true), "zoomreset");
		actionMap.put("zoomin", new ZoomAction(ZoomAction.ZOOM_IN, this));
		actionMap.put("zoomout", new ZoomAction(ZoomAction.ZOOM_OUT, this));
		actionMap.put("zoomreset", new ZoomAction(ZoomAction.ZOOM_RESET, this));
	}
	
	
	void rebuildGraphs()
	{
		panelDependencies.rebuildGraph();
		panelCycles.rebuildGraph();
		panelClasses.rebuildGraph();
	}
	
	
	@Override
	public void zoomIn()
	{
		((GraphPanel<?, ?>) getSelectedComponent()).zoomIn();
	}
	
	
	@Override
	public void zoomOut()
	{
		((GraphPanel<?, ?>) getSelectedComponent()).zoomOut();
	}
	
	
	@Override
	public void zoomReset()
	{
		((GraphPanel<?, ?>) getSelectedComponent()).zoomReset();
	}
	
	
	private PropertyChangeListener propertyListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChanged(String key, Object value)
		{
			if (key.equals(Backend.SELECTED_EDGE))
				setSelectedComponent(panelClasses);
		}
	};
}
