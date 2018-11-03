package komposten.analyser.backend;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import komposten.utilities.tools.Graph;
import komposten.utilities.tools.Graph.CircuitListener;
import komposten.utilities.tools.Graph.Result;

public class GraphCycleFinder
{
	private List<GraphNode> nodeList;
	private List<GraphNode[]> cycles;
	private int[][] adjacencyLists;
	private boolean hasRun;
	private volatile boolean abort;
	
	public GraphCycleFinder(List<? extends GraphNode> nodes)
	{
		cycles = new ArrayList<>();
		setNodes(nodes);
	}

	
	public void setNodes(List<? extends GraphNode> nodes)
	{
		abort = false;
		hasRun = false;
		nodeList = new ArrayList<>();
		
		for (int i = 0; i < nodes.size(); i++)
		{
			if (abort)
				return;
			if (nodes.get(i).getSuccessorNodes().length > -1) //XXX Debug-code!
				nodeList.add(nodes.get(i));
		}
		
		System.out.println("*** Analysing " + nodeList.size() + " nodes... ***");
		
		adjacencyLists = new int[nodeList.size()][];
		
		int totalEdges = 0;
		int highestEdgeCount = 0;
		
		
		for (int i = 0; i < nodeList.size(); i++)
		{
			if (abort)
				return;
			GraphNode node = nodeList.get(i);
			
			List<GraphNode> validSuccessors = new ArrayList<>(); //XXX Debug-code!
			
			for (GraphNode successor : node.getSuccessorNodes())
			{
				if (nodeList.contains(successor))
					validSuccessors.add(successor);
			}
			
			adjacencyLists[i] = new int[validSuccessors.size()];
			
			for (int j = 0; j < validSuccessors.size(); j++)
			{
				totalEdges++;
				int successorIndex = nodeList.indexOf(validSuccessors.get(j));
				adjacencyLists[i][j] = successorIndex;
			}
			
			if (validSuccessors.size() > highestEdgeCount)
				highestEdgeCount = validSuccessors.size();
			
//			adjacencyLists[i] = new int[node.getSuccessorNodes().length];
//			
//			for (int j = 0; j < node.getSuccessorNodes().length; j++)
//			{
//				totalEdges++;
//				int successorIndex = nodeList.indexOf(node.getSuccessorNodes()[j]);
//				adjacencyLists[i][j] = successorIndex;
//			}
//			
//			if (node.getSuccessorNodes().length > highestEdgeCount)
//				highestEdgeCount = node.getSuccessorNodes().length;
		}
		
		System.out.format("Total edge count: %d\n", totalEdges);
		System.out.format("Highest edge count: %d\n", highestEdgeCount);
		System.out.format("Mean edge count: %.02f\n", (totalEdges / (float)nodeList.size()));
//		System.exit(1);
	}
	
	
	/**
	 * Finds all cycles in the graph.
	 * @param circuitListener 
	 * @return <code>true</code> if the analysis completed successfully,
	 *         <code>false</code> otherwise.
	 */
	public boolean findCycles(CircuitListener circuitListener)
	{
		if (!hasRun)
		{
			Result cycleResult = Graph.findElementaryCircuits(adjacencyLists, false, circuitListener);
			
			if (!cycleResult.wasAborted)
			{
				cycles = createCycleList(cycleResult.data, false);
		
				if (abort)
					return false;
				
				hasRun = true;
			}
		}
		
		return hasRun;
	}
	
	
	/**
	 * Finds all cycles containing <code>node</code> in the graph.
	 * @param node
	 * @param circuitListener
	 * @param resultIfAborted <code>true</code> if the found cycles should be returned even if the analysis was aborted.
	 * @return <code>true</code> if the analysis completed successfully,
	 *         <code>false</code> if it was aborted.
	 */
	public boolean findCycles(GraphNode node, CircuitListener circuitListener, boolean resultIfAborted)
	{
		if (!hasRun)
		{
			int index = nodeList.indexOf(node);
			Result cycleResult = Graph.findElementaryCircuits(index, adjacencyLists, circuitListener);
			
			if (!cycleResult.wasAborted || resultIfAborted)
			{
				System.out.println("Creating cycle list!");
				cycles = createCycleList(cycleResult.data, resultIfAborted);
				
				if (abort)
				{
					hasRun = resultIfAborted;
					return false;
				}
				
				hasRun = true;
			}
		}
		
		return hasRun;
	}


	private List<GraphNode[]> createCycleList(int[][] cyclesInt, boolean ignoreAbort)
	{
		List<GraphNode[]> cycles = new LinkedList<>();
		
		for (int i = 0; i < cyclesInt.length; i++)
		{
			if (abort && !ignoreAbort)
				return null;
			
			int[] cycleInt = cyclesInt[i];
			
			GraphNode[] cycleNode = new GraphNode[cycleInt.length];

			for (int j = 0; j < cycleInt.length; j++)
			{
				cycleNode[j] = nodeList.get(cycleInt[j]);
			}

			cycles.add(cycleNode);
		}
		
		return cycles;
	}
	
	
	/**
	 * Finds all nodes in the graph which are part of at least one cycle.
	 * @param circuitListener
	 */
	public List<GraphNode> findNodesInCycles(CircuitListener circuitListener)
	{
		Result sccResult = Graph.findStronglyConnectedComponents(adjacencyLists, circuitListener);
		
		if (!sccResult.wasAborted)
		{
			List<GraphNode> nodesInCycles = new ArrayList<>();
			int[][] stronglyConnectedComponents = sccResult.data;
			
			for (int i = 0; i < stronglyConnectedComponents.length; i++)
			{
				if (abort)
					return null;
				
				int[] component = stronglyConnectedComponents[i];
				
				if (component.length == 1)
				{
					int node = component[0];
					int[] adjacent = adjacencyLists[node];
					
					for (int j : adjacent)
					{
						if (j == node)
						{
							nodesInCycles.add(nodeList.get(node));
							break;
						}
					}
				}
				else
				{
					for (int j = 0; j < component.length; j++)
					{
						if (abort)
							return null;
						
						nodesInCycles.add(nodeList.get(component[j]));
					}
				}
			}
			
			return nodesInCycles;
		}
		
		return null;
	}
	
	
	public void abort()
	{
		abort = true;
		Graph.abortCurrentOperations();
	}
	
	
	public List<GraphNode[]> getCycles()
	{
		if (!hasRun)
			throw new IllegalStateException("Must call findCycles() before getCycles()!");
		
		return cycles;
	}


	public static interface GraphNode
	{
		/** @return All nodes this node points to. */
		public GraphNode[] getSuccessorNodes();
	}
}
