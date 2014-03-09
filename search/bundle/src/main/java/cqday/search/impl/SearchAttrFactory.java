package cqday.search.impl;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import cqday.search.Attribute;
import cqday.search.AttributeFactory;
import cqday.search.AttributeImpl;

/**
 * @scr.service
 * @scr.component immediate="true"
 */
public class SearchAttrFactory implements AttributeFactory {

	public Attribute[] createAttributes(Node filter) {
		try {
			Property property = filter.getProperty("propertyPath");
			Value[] values = null;
			
			if(property.isMultiple()) {
				values = filter.getProperty("propertyPath").getValues();
			} else {
				values = new Value[] { filter.getProperty("propertyPath").getValue() };
			}
				
			String[] paths = new String[values.length];
			
			for (int i = 0; i < values.length; ++i) {
				paths[i] = values[i].getString();
			}
			
			return new AttributeImpl[] {
					new AttributeImpl(filter.getProperty("filterId").getString(), String.class, paths)
			};
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	public String getResourceType() {
		return "search/components/search-filter";
	}

}
