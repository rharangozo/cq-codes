package rh.search.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import rh.search.Attribute;
import rh.search.AttributeFactory;
import rh.search.AttributeImpl;

/**
 * @scr.service
 * @scr.component immediate="true"
 */
public class RangeFilterAttrFactory implements AttributeFactory {

	public Attribute[] createAttributes(Node filter) {
		try {
			return new AttributeImpl[] { 
					new AttributeImpl(
							filter.getProperty("filterId").getString(),
							Long.class, 
							filter.getProperty("propertyPath").getString())
					};
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	public String getResourceType() {
		return "search/components/range-filter";
	}
}
