package komposten.analyser.gui.views.files;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingConstants;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxPoint;

import komposten.analyser.backend.Dependency;
import komposten.analyser.backend.Edge;
import komposten.analyser.backend.PackageData;
import komposten.analyser.gui.backend.Backend;
import komposten.analyser.gui.views.DependencyEdge;
import komposten.analyser.gui.views.UnrootedGraphPanel;
import komposten.utilities.data.FloatPair;

public class ClassPanel extends UnrootedGraphPanel<ClassVertex, ClassEdge>
{
	private Map<String, ClassVertex> vertices;
	private List<mxCell> vertexCells;
	private List<mxCell> edgeCells;
	private List<mxCell[]> vertexGroupsLane1;
	private List<mxCell[]> vertexGroupsLane2;
	private mxCell lane1Label;
	private mxCell lane2Label;
	private mxCell pool;
	private mxCell lane1;
	private mxCell lane2;
	
	private List<Object> cellsInCycles;
	private List<Object> cellsExternal;
	private List<Object> cellsDefault;
	
	public ClassPanel(Backend backend)
	{
		super(backend, ClassEdge.class);

		jGraph.setEdgesSelectable(true); //FIXME Try to eliminate usage of jGraph in sub-classes! (Can e.g. pass it to createLayout for use there.)
		vertices = new HashMap<>();
		vertexCells = new LinkedList<>();
		edgeCells = new LinkedList<>();
		vertexGroupsLane1 = new ArrayList<>();
		vertexGroupsLane2 = new ArrayList<>();
		cellsInCycles = new ArrayList<>();
		cellsExternal = new ArrayList<>();
		cellsDefault = new ArrayList<>();
		
		jGraph.getModel().beginUpdate();
		try
		{
			//FIXME ClassPanel; Make it impossible to move cells between lanes (or out of lanes in general).
			//				Either make cells immovable, or find another way ("cell moved listeners"?).
			//				Listeners can be added using jGraph.addListener(). Event types can be found in mxEvent (e.g. REMOVE_CELLS_FROM_PARENT and CELLS_MOVED might be of interest).
			String laneStyle = 
					"shape=rectangle;"
					+ "autosize=0;"
					+ "foldable=false;"
					+ "fillOpacity=0.0;"
					+ "movable=false;"
					+ "fillColor=#CCCCCC;"
					+ "strokeColor=#777777";
			String poolStyle = "shape=pool;fontSize=9;fontStyle=1;startSize=20;horizontal=true;autosize=0;foldable=false;movable=false;";
			pool = (mxCell) jGraph.insertVertex(jGraph.getDefaultParent(), null, "", 10, 10, 0, 0, poolStyle);
			lane1 = (mxCell) jGraph.insertVertex(pool, null, "", 0, 0, 0, 0, laneStyle);
			lane2 = (mxCell) jGraph.insertVertex(pool, null, "", 0, 0, 0, 0, laneStyle);
			lane1Label = (mxCell) jGraph.insertVertex(lane1, null, "", 0, 0, 0, 0, "style=label;movable=false;");
			lane2Label = (mxCell) jGraph.insertVertex(lane2, null, "", 0, 0, 0, 0, "style=label;movable=false;");
			pool.setVisible(false);
		}
		finally
		{
			jGraph.getModel().endUpdate();
		}

		add(new ClassMenuBar(this), BorderLayout.NORTH);
	}


	@Override
	protected mxGraphLayout createLayout()
	{
		return new mxHierarchicalLayout(jGraph, SwingConstants.NORTH);
	}


	@Override
	protected void addVertices(Edge baseEdge, boolean isBidirectional)
	{
		cellsInCycles.clear();
		cellsExternal.clear();
		
		if (baseEdge instanceof DependencyEdge)
		{
			PackageData source = (PackageData)baseEdge.getSource();
			PackageData target = (PackageData)baseEdge.getTarget();
			
			lane1Label.setValue(source.fullName);
			lane2Label.setValue(target.fullName);
			
			addVerticesForDependency(source.getDependencyForPackage(target), source);
			
			if (isBidirectional)
				addVerticesForDependency(target.getDependencyForPackage(source), source);
			
			if (source.isInCycle)
				cellsInCycles.add(lane1Label);
			else if (source.isExternal)
				cellsExternal.add(lane1Label);
			else
				cellsDefault.add(lane1Label);
			
			if (target.isInCycle)
				cellsInCycles.add(lane2Label);
			else if (target.isExternal)
				cellsExternal.add(lane2Label);
			else
				cellsDefault.add(lane2Label);
		}
		
		jGraph.applyDefaultStyle(cellsDefault.toArray());
		jGraph.applyCycleStyle(cellsInCycles.toArray());
		jGraph.applyExternalStyle(cellsExternal.toArray());
	}


	private void addVerticesForDependency(Dependency dependency, PackageData lane1Package)
	{
		boolean isSource = dependency.source == lane1Package;
		for (Entry<String, String[]> entry : dependency.classDependencies.entrySet())
		{
			String sourceClass = entry.getKey();
			String[] targetClasses = entry.getValue();

			addVertex(sourceClass, isSource, dependency.source);
			
			for (String targetClass : targetClasses)
			{
				addVertex(targetClass, !isSource, dependency.target);
			}
		}
	}


	private void addVertex(String className, boolean isSource, PackageData packageData)
	{
		if (!vertices.containsKey(className))
		{
			ClassVertex vertex = new ClassVertex(packageData, className, null);
			vertices.put(className, vertex);
			
			jGraph.getModel().beginUpdate();
			try
			{
				mxCell lane = (isSource ? lane1 : lane2);
				mxCell cell = (mxCell) jGraph.insertVertex(lane, null, vertex, 0, 0, 0, 0);
				
				jGraph.updateCellSize(cell);
        jGraph.getVertexToCellMap().put(vertex, cell);
        jGraph.getCellToVertexMap().put(cell, vertex);
        vertexCells.add(cell);
        
        if (packageData.isInCycle)
        	cellsInCycles.add(cell);
        else if (packageData.isExternal)
        	cellsExternal.add(cell);
        else
        	cellsDefault.add(cell);
			}
			finally
			{
				jGraph.getModel().endUpdate();
			}
		}
	}


	@Override
	protected void addEdges(Edge baseEdge, boolean isBidirectional)
	{
		cellsInCycles.clear();
		cellsExternal.clear();
		
		if (baseEdge instanceof DependencyEdge)
		{
			PackageData source = (PackageData)baseEdge.getSource();
			PackageData target = (PackageData)baseEdge.getTarget();
			
			addEdgesForDependency(source.getDependencyForPackage(target));
			
			if (isBidirectional)
				addEdgesForDependency(target.getDependencyForPackage(source));
			
			createVertexGroups();
		}
		
		jGraph.applyCycleStyle(cellsInCycles.toArray());
		jGraph.applyExternalStyle(cellsExternal.toArray());
	}


	private void addEdgesForDependency(Dependency dependency)
	{
		for (Entry<String, String[]> entry : dependency.classDependencies.entrySet())
		{
			String sourceClass = entry.getKey();
			String[] targetClasses = entry.getValue();

			ClassVertex sourceVertex = vertices.get(sourceClass);
			for (String targetClass : targetClasses)
			{
				ClassVertex targetVertex = vertices.get(targetClass);
				addEdge(sourceVertex, targetVertex);
			}
		}
	}


	private void addEdge(ClassVertex sourceVertex, ClassVertex targetVertex)
	{
		ClassEdge edge = new ClassEdge(sourceVertex, targetVertex);

		jGraph.getModel().beginUpdate();
		try
		{
			mxICell sourceCell = jGraph.getCellForVertex(sourceVertex);
			mxICell targetCell = jGraph.getCellForVertex(targetVertex);
			mxCell cell = (mxCell) jGraph.insertEdge(pool, null, edge, sourceCell, targetCell);
			
			jGraph.updateCellSize(cell);
      jGraph.getEdgeToCellMap().put(edge, cell);
      jGraph.getCellToEdgeMap().put(cell, edge);
      edgeCells.add(cell);
      
      if (sourceVertex.packageData.sharesCycleWith(targetVertex.packageData))
  			cellsInCycles.add(cell);
      else if (targetVertex.isExternal)
      	cellsExternal.add(cell);
      else
      	cellsDefault.add(cell);
		}
		finally
		{
			jGraph.getModel().endUpdate();
		}
	}


	private void createVertexGroups()
	{
		List<mxCell> availableVertices = new LinkedList<>(vertexCells);
		List<mxCell> currentGroup1 = new LinkedList<>();
		List<mxCell> currentGroup2 = new LinkedList<>();
		
		vertexGroupsLane1.clear();
		
		while (!availableVertices.isEmpty())
		{
			addVertexToGroup(0, currentGroup1, currentGroup2, availableVertices);
			
			Comparator<mxCell> cellComparator = new Comparator<mxCell>()
			{
				@Override
				public int compare(mxCell o1, mxCell o2)
				{
					return o1.toString().compareTo(o2.toString());
				}
			};
			
			currentGroup1.sort(cellComparator);
			currentGroup2.sort(cellComparator);
			
			vertexGroupsLane1.add(currentGroup1.toArray(new mxCell[currentGroup1.size()]));
			vertexGroupsLane2.add(currentGroup2.toArray(new mxCell[currentGroup2.size()]));
			currentGroup1.clear();
			currentGroup2.clear();
		}
	}
	

	private void addVertexToGroup(int vertexIndex, List<mxCell> currentGroup1,
			List<mxCell> currentGroup2, List<mxCell> availableVertices)
	{
		mxCell vertex = availableVertices.remove(vertexIndex);
		
		if (vertex.getParent() == lane1)
			currentGroup1.add(vertex);
		else
			currentGroup2.add(vertex);

		for (Object edge : jGraph.getEdges(vertex))
		{
			ClassEdge classEdge = jGraph.getCellToEdgeMap().get(edge);
			mxICell target = jGraph.getCellForVertex((ClassVertex) classEdge.getTarget());
			
			if (target == vertex)
				target = jGraph.getCellForVertex((ClassVertex) classEdge.getSource());
			
			int targetIndex = availableVertices.indexOf(target);
			
			if (targetIndex != -1)
				addVertexToGroup(targetIndex, currentGroup1, currentGroup2, availableVertices);
		}
	}


	@Override
	protected void clearVertices()
	{
		super.clearVertices();
		jGraph.removeCells(vertexCells.toArray());
		vertexCells.clear();
		vertices.clear();
		vertexGroupsLane1.clear();
		vertexGroupsLane2.clear();
	}
	
	
	@Override
	protected void clearEdges()
	{
		super.clearEdges();
		jGraph.removeCells(edgeCells.toArray());
		edgeCells.clear();
		vertexGroupsLane1.clear();
		vertexGroupsLane2.clear();
	}
	
	
	@Override
	protected void layoutGraph()
	{
		pool.setVisible(true);
		
		jGraph.updateCellSize(lane1Label);
		jGraph.updateCellSize(lane2Label);

		FloatPair[] laneSizes = layoutClasses(lane1Label.getGeometry().getHeight() + 10);
		FloatPair lane1Size = laneSizes[0];
		FloatPair lane2Size = laneSizes[1];
		
		jGraph.getModel().beginUpdate();
		
		try
		{
			lane1.getGeometry().setWidth(lane1Size.getFirst());
			lane1.getGeometry().setHeight(lane1Size.getSecond());
			lane2.getGeometry().setWidth(lane2Size.getFirst());
			lane2.getGeometry().setHeight(lane2Size.getSecond());
			
			lane1Label.getGeometry().setX(0);
			lane1Label.getGeometry().setY(0);
			lane2Label.getGeometry().setX(0);
			lane2Label.getGeometry().setY(0);
			lane1Label.getGeometry().setWidth(lane1.getGeometry().getWidth());
			lane2Label.getGeometry().setWidth(lane2.getGeometry().getWidth());
			
			int laneSpacing = 40;
			lane1.getGeometry().setX(0);
			lane1.getGeometry().setY(0);
			lane2.getGeometry().setX(lane1.getGeometry().getWidth() + laneSpacing);
			lane2.getGeometry().setY(0);
			
			pool.getGeometry().setX(10);
			pool.getGeometry().setY(10);
			pool.getGeometry().setWidth(lane2.getGeometry().getRectangle().getMaxX());
			pool.getGeometry().setHeight(lane2.getGeometry().getRectangle().getMaxY());
		}
		finally
		{
			jGraph.getModel().endUpdate();
		}
		
		layoutEdges();

		jGraph.refresh();
		System.out.println(graphPanel.getGraph().getView().getState(lane1).getStyle());
	}
	
	
	private FloatPair[] layoutClasses(double minY)
	{
		jGraph.getModel().beginUpdate();
		
		double lane1Width = 0;
		double lane2Width = 0;
		double laneHeight = 0;
		double margin = 10;
		double spacing = 10;
		double y = minY;
		
		try
		{
			for (int i = 0; i < vertexGroupsLane1.size(); i++)
			{
				mxCell[] lane1Group = vertexGroupsLane1.get(i);
				mxCell[] lane2Group = vertexGroupsLane2.get(i);
				
				double vertexHeight = lane1Group[0].getGeometry().getHeight();
				double group1Height = lane1Group.length * (vertexHeight + spacing) - spacing; 
				double group2Height = lane2Group.length * (vertexHeight + spacing) - spacing; 
				double groupHeight = Math.max(group1Height, group2Height);
				
				double group1Width = layoutVertexGroup(lane1Group, y, groupHeight, vertexHeight, spacing);
				double group2Width = layoutVertexGroup(lane2Group, y, groupHeight, vertexHeight, spacing);
				
				if (group1Width > lane1Width)
					lane1Width = group1Width;
				if (group2Width > lane2Width)
					lane2Width = group2Width;
				
				if (i < vertexGroupsLane1.size() - 1)
					y += groupHeight + spacing;
				else
					y += groupHeight;
			}
		}
		finally
		{
			jGraph.getModel().endUpdate();
		}
		
		laneHeight = y + margin;
		lane1Width += 2*margin;
		lane2Width += 2*margin;
		
		FloatPair lane1Dimension = new FloatPair((float)lane1Width, (float) laneHeight);
		FloatPair lane2Dimension = new FloatPair((float)lane2Width, (float) laneHeight);
		
		return new FloatPair[] { lane1Dimension, lane2Dimension };
	}


	/**
	 * @return The width of the widest vertex.
	 */
	private double layoutVertexGroup(mxCell[] group, double startY, double groupHeight, double vertexHeight, double spacing)
	{
		double x = 10;
		double y;
		double midY = startY + groupHeight / 2;
		
		double maxWidth = 0;
		
		if ((group.length & 1) == 0) //Even number
		{
			y = midY - (spacing / 2) - ((vertexHeight + spacing) * (group.length / 2)) + spacing;
		}
		else
		{
			y = midY - (vertexHeight / 2) - ((vertexHeight + spacing) * (group.length / 2));
		}
		
		for (int i = 0; i < group.length; i++)
		{
			mxCell vertex = group[i];

			vertex.getGeometry().setX(x);
			vertex.getGeometry().setY(y);

			y += vertexHeight + spacing;
			
			if (vertex.getGeometry().getWidth() > maxWidth)
				maxWidth = vertex.getGeometry().getWidth();
		}
		
		return maxWidth;
	}


	private void layoutEdges()
	{
		jGraph.getModel().beginUpdate();
		
		try
		{
			for (mxCell edgeCell : edgeCells)
			{
				ClassEdge edge = jGraph.getCellToEdgeMap().get(edgeCell);
				mxICell sourceCell = jGraph.getCellForVertex((ClassVertex)edge.getSource());
				mxICell targetCell = jGraph.getCellForVertex((ClassVertex)edge.getTarget());
				
				boolean startsInLane1 = (sourceCell.getParent() == lane1);
				
				//CURRENT Use the source, target and lane cells to add control points to the edges.
				//				Remember that the vertices have positions relative to their lanes, but edges are relative to the pool!
				ArrayList<mxPoint> pointList = new ArrayList<>();
				edgeCell.getGeometry().setPoints(pointList);

				double x;
				double y;
				
				int temp = 10;
				int yDisplacement = 5;
				if (startsInLane1)
				{
					x = lane1.getGeometry().getRectangle().getMaxX() + temp;
					y = lane1.getGeometry().getY() + sourceCell.getGeometry().getCenterY() - yDisplacement;
					pointList.add(new mxPoint(x, y));
					x = lane2.getGeometry().getX() - temp;
					y = lane2.getGeometry().getY() + targetCell.getGeometry().getCenterY() - yDisplacement;
					pointList.add(new mxPoint(x, y));
				}
				else if (!startsInLane1)
				{
					x = lane2.getGeometry().getX() - temp;
					y = lane2.getGeometry().getY() + sourceCell.getGeometry().getCenterY() + yDisplacement;
					pointList.add(new mxPoint(x, y));
					x = lane1.getGeometry().getRectangle().getMaxX() + temp;
					y = lane1.getGeometry().getY() + targetCell.getGeometry().getCenterY() + yDisplacement;
					pointList.add(new mxPoint(x, y));
				}
			}
		}
		finally
		{
			jGraph.getModel().endUpdate();
		}
	}


	@Override
	protected void selectionChanged(Object[] newSelection)
	{
		refreshGraph(false);
	}


	@Override
	protected void vertexDoubleClicked(ClassVertex vertex) { }


	@Override
	protected void edgeDoubleClicked(ClassEdge edge, boolean isBidirectional) { }
}
