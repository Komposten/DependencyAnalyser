package komposten.analyser.backend;

import java.io.Serializable;

/**
 * This interface represents an edge in a graph.<br />
 * <b>Note: all implementing classes should have a default constructor!</b>
 */
public interface Edge extends Serializable
{
	public Vertex getSource();
	public Vertex getTarget();
}