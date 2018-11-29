package komposten.analyser.backend;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import komposten.analyser.backend.GraphCycleFinder.GraphNode;
import komposten.utilities.tools.Graph.CircuitListener;

class GraphCycleFinderTest
{
	static Node[] nodes;
	static List<Node> nodeList;
	GraphCycleFinder finder;
	Listener listener;

	@BeforeAll
	static void setUpGraph()
	{
		int nodeCount = 6;
		nodes = new Node[nodeCount];

		for (int i = 0; i < nodeCount; i++)
			nodes[i] = new Node(i);

		setSuccessors(0, 1); // This node is not part of any cycle.
		setSuccessors(1, 2, 3, 4); // This node is part of all cycles.
		setSuccessors(2, 3); // This node is part of two cycles (2312 and 23412)
		setSuccessors(3, 1, 4); // This node is part of all cycles except one.
		setSuccessors(4, 1); // This node is part of three cycles (41234, 4134 and 414).
		setSuccessors(5, 3, 5); // This node is part of a loop.
		// There are a total of 5 cycles in the graph.

		GraphCycleFinderTest.nodeList = Arrays.asList(nodes);
	}
	
	
	@BeforeEach
	void setUpFinder()
	{
		finder = new GraphCycleFinder(nodeList);
		listener = new Listener();
	}


	private static void setSuccessors(int node, int... successors)
	{
		for (int successor : successors)
		{
			nodes[node].successors.add(nodes[successor]);
		}
	}


	@Test
	void findCycles_allCycles()
	{
		finder.findCycles(null);

		String[] cycleStrings = createSortedCycleArray(finder.getCycles());

		String[] expected = new String[]
				{
						"1231",
						"12341",
						"131",
						"1341",
						"141",
						"55"
				};

		assertArrayEquals(expected, cycleStrings);
	}
	
	
	@Test
	void findCycles_allCycles_circuitListenerCalled()
	{
		finder.findCycles(listener);
		
		assertTrue(listener.circuitCountUpdated);
	}
	
	
	@Test
	void findCycles_allCyclesForPackage_twoCycles()
	{
		finder.findCycles(nodes[2], null, false);

		String[] cycleStrings = createSortedCycleArray(finder.getCycles());
		
		String[] expected = new String[]
				{
						"2312",
						"23412"
				};
		
		assertArrayEquals(expected, cycleStrings);
	}
	
	
	@Test
	void findCycles_allCyclesForPackage_circuitListenerCalled()
	{
		finder.findCycles(nodes[2], listener, false);
		
		assertTrue(listener.circuitCountUpdated);
	}
	
	
	@Test
	void findNodesInCycles()
	{
		List<GraphNode> nodesInCycles = finder.findNodesInCycles(null);
		
		Collections.sort(nodesInCycles, new Comparator<GraphNode>()
		{
			@Override
			public int compare(GraphNode o1, GraphNode o2)
			{
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		GraphNode[] expected = new GraphNode[]
				{
						nodes[1],
						nodes[2],
						nodes[3],
						nodes[4],
						nodes[5]
				};
		
		assertArrayEquals(expected, nodesInCycles.toArray(new GraphNode[4]));
	}
	
	
	@Test
	void findNodesInCycles_circuitListenerCalled()
	{
		finder.findNodesInCycles(listener);
		
		assertTrue(listener.vertexUpdated);
	}
	
	
	@Test
	void getCycles_beforeFindCycles_illegalStateException()
	{
		Executable executable = () -> finder.getCycles();
		
		assertThrows(IllegalStateException.class, executable, "getCycles() should throw exception if called before findCycles()");
	}


	private String[] createSortedCycleArray(List<GraphNode[]> cycles)
	{
		List<String> cycleStrings = new ArrayList<>();

		for (GraphNode[] cycleNodes : cycles)
		{
			String cycle = "";
			for (GraphNode node : cycleNodes)
				cycle += node;
			cycleStrings.add(cycle);
		}

		Collections.sort(cycleStrings);
		return cycleStrings.toArray(new String[cycleStrings.size()]);
	}


	private static class Node implements GraphNode
	{
		final int index;
		List<Node> successors = new ArrayList<>();


		public Node(int index)
		{
			this.index = index;
		}


		@Override
		public GraphNode[] getSuccessorNodes()
		{
			return successors.toArray(new GraphNode[successors.size()]);
		}


		@Override
		public String toString()
		{
			return String.valueOf(index);
		}
	}
	
	
	private static class Listener implements CircuitListener
	{
		boolean circuitCountUpdated;
		boolean vertexUpdated;
		
		@Override
		public void onNewCircuitCount(int newCount)
		{
			circuitCountUpdated = true;
		}

		@Override
		public void onNextVertex(int vertex, int processedVertices, int vertexCount)
		{
			vertexUpdated = true;
		}
	}
}
