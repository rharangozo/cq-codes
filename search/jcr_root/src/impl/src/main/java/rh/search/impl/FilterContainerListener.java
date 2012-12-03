package rh.search.impl;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import rh.utility.AbstractComponentListener;

/**
 * @scr.service interface="rh.utility.ComponentListener"
 * @scr.component metatype="false"
 */
public class FilterContainerListener extends AbstractComponentListener {

	public FilterContainerListener() {		
		super("search/components/filter-container", "search-id");
	}

	@Override
	protected void doPostConstruct(Node component) throws RepositoryException {
		component.setProperty("selectable-category", "search-filter-container");
		component.setProperty("selectable-name", "N/A");
		component.setProperty("selectable-value", "N/A");
	}

	@Override
	protected void propertyChanged(Node component, Property property) throws RepositoryException {
		component.setProperty("selectable-value", property.getValue().getString());
		component.setProperty("selectable-name", property.getValue().getString());
	}

	@Override
	protected void propertyRemoved(Node component, String propertyName) throws RepositoryException {
		component.setProperty("selectable-value", "N/A");
		component.setProperty("selectable-name", "N/A");
	}

	
}
