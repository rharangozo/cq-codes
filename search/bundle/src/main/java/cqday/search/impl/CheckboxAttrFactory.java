package cqday.search.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import cqday.search.Attribute;
import cqday.search.AttributeFactory;
import cqday.search.AttributeImpl;

/**
 * @scr.service
 * @scr.component immediate="true"
 */
public class CheckboxAttrFactory implements AttributeFactory {

	public Attribute[] createAttributes(Node filter) {
		try {
			return new AttributeImpl[] { 
					new AttributeImpl(
							filter.getProperty("filterId").getString(),
							Boolean.class, 
							filter.getProperty("propertyPath").getString())
					};
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	public String getResourceType() {
		return "search/components/checkbox-filter";
	}

}
