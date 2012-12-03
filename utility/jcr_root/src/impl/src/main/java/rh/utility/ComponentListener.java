package rh.utility;

import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;

public interface ComponentListener {

	String getObservervedComponentType();
	
	Set<String> getObservedProperties();
	
	void postConstruct(Node component);
	
	void removed(Node component, String propertyName);
	
	void changed(Node component, Property property);
	
	void created(Node component, Property property);
}
