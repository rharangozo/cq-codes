package cqday.search;

import javax.jcr.Node;

public interface AttributeFactory {

	/**
	 * Return with the resource type of the filter. The type must represent the component used as filter. e.g.: search/components/checkbox-filter
	 * @return
	 */
	String getResourceType();
	
	/**
	 * Return with the Attributes defined by the given node which refers to a filter node
	 * @param filter
	 * @return
	 */
	//TODO: does it make sense to return with an array of Attributes? Maybe it is enough return with only Attribute instance
	Attribute[] createAttributes(Node filter);

}
