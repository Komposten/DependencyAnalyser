package komposten.analyser.gui.views.cycles;

import java.util.ArrayList;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

public class CycleLayout extends mxCircleLayout
{

	public CycleLayout(mxGraph graph)
	{
		super(graph);
	}
	
	
	public CycleLayout(mxGraph graph, double radius)
	{
		super(graph, radius);
	}
	
	
	@Override
	public void circle(Object[] vertices, double r, double left, double top)
	{
		ArrayList<mxRectangle> vertexBodies = new ArrayList<mxRectangle>();
		
		r /= 3.5; //Moves everything closer together. Might cause some overlaps, which is why we call resolveOverlaps() at the end of this method.
		int vertexCount = vertices.length;
		double phi = 2 * Math.PI / vertexCount;

		for (int i = 0; i < vertexCount; i++)
		{
			if (isVertexMovable(vertices[i]))
			{
				mxRectangle body = setVertexLocation(vertices[i],
						left + r + r * Math.sin(i * phi), top + r + r
								* Math.cos(i * phi));
				
				vertexBodies.add(body);
			}
		}
		
		resolveOverlaps(vertices, vertexBodies, left + r, top + r);
		ensurePositiveCoordinatesForAllCells(vertexBodies);
	}


	private void resolveOverlaps(Object[] vertices, ArrayList<mxRectangle> vertexBodies, double centerX, double centerY)
	{
		mxRectangle calcRectangle = new mxRectangle();
		mxRectangle calcRectangle2 = new mxRectangle();
		
		for (int i = 0; i < vertexBodies.size(); i++)
		{
			mxRectangle body = vertexBodies.get(i);
			calcRectangle.setRect(body.getX(), body.getY(), body.getWidth(), body.getHeight());

			for (int j = 0; j < vertexBodies.size(); j++)
			{
				if (j != i)
				{
					mxRectangle body2 = vertexBodies.get(j);
					calcRectangle2.setRect(body2.getX()-20, body2.getY()-20, body2.getWidth()+40, body2.getHeight()+40);
					
					if (overlaps(calcRectangle, calcRectangle2))
					{
						resolveOverlap(calcRectangle, calcRectangle2, centerX, centerY);
						mxRectangle temp = setVertexLocation(vertices[i], calcRectangle.getX(), calcRectangle.getY());
						vertexBodies.remove(i);
						vertexBodies.add(i, temp);
						
						j = -1; //Since we moved body we must go back to the beginning of the list to make sure we didn't overlap an already checked vertex!
					}
				}
			}
		}
	}


	private void resolveOverlap(mxRectangle rectangle1, mxRectangle rectangle2,
			double circleCenterX, double circleCenterY)
	{
		while (overlaps(rectangle1, rectangle2))
		{
			double angle = Math.atan2(rectangle1.getCenterY() - circleCenterY, rectangle1.getCenterX() - circleCenterX);
			double stepX = 10 * Math.cos(angle);
			double stepY = 10 * Math.sin(angle);

			rectangle1.setX(rectangle1.getX() + stepX);
			rectangle1.setY(rectangle1.getY() + stepY);
		}
	}
	
	
	private boolean overlaps(mxRectangle r1, mxRectangle r2)
	{
		return r1.getX() < r2.getX() + r2.getWidth() &&
				r1.getX() + r1.getWidth() > r2.getX() &&
				r1.getY() < r2.getY() + r2.getHeight() &&
				r1.getY() + r1.getHeight() > r2.getY();
	}


	private void ensurePositiveCoordinatesForAllCells(ArrayList<mxRectangle> vertexBodies)
	{
		double lowestX = 0;
		double lowestY = 0;
		for (mxRectangle mxRectangle : vertexBodies)
		{
			lowestX = Math.min(mxRectangle.getX(), lowestX);
			lowestY = Math.min(mxRectangle.getY(), lowestY);
		}
		
		Object[] oldSelection = graph.getSelectionCells();
		graph.selectAll();
		graph.moveCells(graph.getSelectionCells(), lowestX < 0 ? -lowestX : 0, lowestY < 0 ? -lowestY : 0);
		graph.setSelectionCells(oldSelection);
	}
}