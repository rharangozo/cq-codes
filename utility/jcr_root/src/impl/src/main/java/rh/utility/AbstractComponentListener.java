package rh.utility;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractComponentListener implements ComponentListener {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractComponentListener.class);
	
	private Set<String> observedProperties;
	private String observedComponentType;

	public AbstractComponentListener(String observedComponentType, String... observerdPropertyNames) {
		if(observerdPropertyNames != null && observerdPropertyNames.length > 0) {
			observedProperties = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(observerdPropertyNames)));
		}
		this.observedComponentType = observedComponentType;
	}

	public final Set<String> getObservedProperties() {
		return observedProperties;
	}

	public final String getObservervedComponentType() {
		return observedComponentType;
	}
	
	public final void changed(Node component, Property property) {
		try{
			propertyChanged(component, property);
		} catch(RepositoryException repositoryException){
			LOG.error("Error occurred during invoking the eventChanged", repositoryException);
		}
	}

	protected void propertyChanged(Node component, Property property) throws RepositoryException {
	}

	public final void created(Node component, Property property) {
		try{
			propertyCreated(component, property);
		} catch(RepositoryException repositoryException){
			LOG.error("Error occurred during invoking the eventCreated", repositoryException);
		}
	}

	protected void propertyCreated(Node component, Property property) throws RepositoryException {
	}

	public final void postConstruct(Node component) {
		try{
			doPostConstruct(component);
		} catch(RepositoryException repositoryException){
			LOG.error("Error occurred during invoking the eventPostConstruct", repositoryException);
		}		
	}

	protected void doPostConstruct(Node component) throws RepositoryException {
	}

	public final void removed(Node component, String propertyName) {
		try{
			propertyRemoved(component, propertyName);
		} catch(RepositoryException repositoryException){
			LOG.error("Error occurred during invoking the eventRemoved", repositoryException);
		}	
	}

	protected void propertyRemoved(Node component, String propertyName) throws RepositoryException {
	}


}
