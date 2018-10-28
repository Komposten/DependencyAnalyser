package komposten.analyser.backend;

import java.util.ArrayList;


/**
 * This class can be used to find cycles in <i>directed</i> graphs. <br />
 * This code is based on this article: <a href=
 * "http://www.geeksforgeeks.org/detect-cycle-direct-graph-using-colors/">Detect
 * cycle in a direct graph using colors</a>.
 * 
 * @author Jakob Hjelm
 */
public class GraphCycleFinderOld
{
	enum State
	{
		Unvisited, //Not processed
		BeingProcessed,  //Being processed
		PartiallyProcessed, //Has been processed at least once, but not all predecessors have accessed it.
		Processed  //Processed
	}
	
	private GraphNode[] nodes;
	private int[][] successorMap;
	private int[][] predecessorMap;
	private State[] colors;

	private boolean abort;


	public GraphCycleFinderOld(GraphNode[] nodes)
	{
		this.nodes = nodes;
		
		successorMap = new int[nodes.length][];
		for (int i = 0; i < successorMap.length; i++)
			successorMap[i] = getSuccessorIndices(nodes[i]);
		
		predecessorMap = new int[nodes.length][];
		for (int i = 0; i < predecessorMap.length; i++)
			predecessorMap[i] = getPredecessorIndices(nodes[i], i);

		colors = new State[nodes.length];
		for (int i = 0; i < colors.length; i++)
			colors[i] = State.Unvisited;
	}
	
	
	private int[] getSuccessorIndices(GraphNode node)
	{
		GraphNode[] successors = node.getSuccessorNodes();
		int[] indices;
		
		if (successors != null)
		{
		  indices = new int[successors.length];
		
			for (int j = 0; j < successors.length; j++)
			{
				for (int i = 0; i < nodes.length; i++)
				{
					if (nodes[i] == successors[j])
					{
						indices[j] = i;
					}
				}
			}
		}
		else
		{
			indices = new int[0];
		}
		
		return indices;
	}
	
	
	private int[] getPredecessorIndices(GraphNode node, int index)
	{
		ArrayList<Integer> predecessors = new ArrayList<Integer>();
		
		for (int i = 0; i < successorMap.length; i++)
		{
			for (int j = 0; j < successorMap[i].length; j++)
			{
				if (successorMap[i][j] == index)
				{
					predecessors.add(i);
				}
			}
		}
		
		int[] array = new int[predecessors.size()];
		for (int i = 0; i < array.length; i++)
			array[i] = predecessors.get(i).intValue();
		
		return array;
	}


	public ArrayList<GraphNode[]> getCycles()
	{
		abort = false;
		ArrayList<GraphNode[]> cycles = new ArrayList<GraphNode[]>();
		ArrayList<GraphNode> currentStack = new ArrayList<GraphNode>();
		
		for (int i = 0; i < nodes.length; i++)
		{
			if (abort)
				return null;
			
			if (colors[i] == State.Unvisited)
				searchNode(i, -1, currentStack, cycles);
		}
		
//		searchNode(1, currentStack, cycles);
		
		return cycles;
	}
	
	
	
	private void searchNode(int nodeIndex, int predecessorIndex, ArrayList<GraphNode> currentStack, ArrayList<GraphNode[]> cycles)
	{
		colors[nodeIndex] = State.BeingProcessed;
		currentStack.add(nodes[nodeIndex]);
		
		int[] successorNodes = successorMap[nodeIndex];
		
		for (int i = 0; i < successorNodes.length; i++)
		{
			if (abort)
				return;
			
			if (colors[successorNodes[i]] == State.BeingProcessed) //We have a cycle!
			{
				GraphNode[] cycle = currentStack.toArray(new GraphNode[currentStack.size()+1]);
				cycle[cycle.length-1] = nodes[successorNodes[i]];
				cycle = getTrimmedCycle(cycle);
				cycles.add(cycle);
			}
			else if (colors[successorNodes[i]] == State.Unvisited || colors[successorNodes[i]] == State.PartiallyProcessed) //Not visited or not visited by all predecessors, run searchNode on it!
			{
				searchNode(successorNodes[i], nodeIndex, currentStack, cycles);
			}
			
			//"Processed" nodes have already been processed completely so we can just skip them.
		}
		
		currentStack.remove(nodes[nodeIndex]);
		
		//This code clears the current predecessor from the list.
		//If all predecessors have been cleared, mark the node as "Processed".
		int[] predecessorNodes = predecessorMap[nodeIndex];
		boolean processedByAllPredecessors = true;
		for (int i = 0; i < predecessorNodes.length; i++)
		{
			if (predecessorNodes[i] == predecessorIndex)
				predecessorNodes[i] = -1;
			
			if (predecessorNodes[i] != -1)
				processedByAllPredecessors = false;
		}
		colors[nodeIndex] = processedByAllPredecessors ? State.Processed : State.PartiallyProcessed;
	}


	private GraphNode[] getTrimmedCycle(GraphNode[] cycle)
	{
		GraphNode endNode = cycle[cycle.length-1];
		int startNodeIndex = 0;
		for (int i = 0; i < cycle.length; i++)
		{
			if (cycle[i] == endNode)
			{
				startNodeIndex = i;
				break;
			}
		}
		
		GraphNode[] dest = new GraphNode[cycle.length - startNodeIndex];
		
		System.arraycopy(cycle, startNodeIndex, dest, 0, dest.length);
		
		return dest;
	}
	
	
	public void abortProcess()
	{
		abort = true;
	}


	public static interface GraphNode
	{
		/** @return All nodes this node points to. */
		public GraphNode[] getSuccessorNodes();
	}
}