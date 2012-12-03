package rh.search;

import javax.jcr.Node;

/**
 * This interface describes an attribute which is interesting from the search perspective
 * @author roland_harangozo
 *
 */
public interface Attribute {

	/**
	 * Name of the attribute
	 * @return
	 */
	String getName();
	
	/**
	 * 
	 * @param node
	 * @return with the value of this attribute of the given node if any
	 */
	Object getAttributeValueOf(Node node);

	/**
	 * 
	 * @return with the type of this attribute
	 */
	Class getType();
}
