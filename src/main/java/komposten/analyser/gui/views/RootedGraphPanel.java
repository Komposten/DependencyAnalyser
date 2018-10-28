package komposten.analyser.gui.views;

import komposten.analyser.backend.PackageData;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.backend.Backend.PropertyChangeListener;

public abstract class RootedGraphPanel extends GraphPanel<PackageData, DependencyEdge>
{
	protected PackageData rootPackage;
	

	public RootedGraphPanel(Backend backend)
	{
		super(backend, DependencyEdge.class);
		
		backend.addPropertyChangeListener(propertyChangeListener, Backend.SELECTED_PACKAGE);
	}
	
	
	protected abstract void addVertices(PackageData rootPackage);
	protected abstract void addEdges(PackageData rootPackage);
	

	public void showGraphForPackage(PackageData packageData)
	{
		if (packageData == null || packageData != rootPackage)
		{
			clearGraph();
			
			rootPackage = packageData;
			
			if (packageData != null)
			{
				addVertices(packageData);
				addEdges(packageData);
				
				refreshGraph(true);
			}
		}
	}
	
	
	@Override
	public void rebuildGraph()
	{
		PackageData root = rootPackage;
		rootPackage = null;
		showGraphForPackage(root);
	}
	
	
	@Override
	protected void clearGraph()
	{
		rootPackage = null;
		super.clearGraph();
	}
	
	
	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChanged(String key, Object value)
		{
			if (key.equals(Backend.SELECTED_PACKAGE))
				showGraphForPackage((PackageData)value);
		}
	};
}
