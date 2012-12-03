package rh.utility.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rh.utility.ComponentListener;
import rh.utility.WhiteboardUtil;

/**
 * @scr.component metatype="false" immediate="true"
 */
public class ComponentObserver {
	
	private static final Logger LOG = LoggerFactory.getLogger(ComponentObserver.class);
	
	private SlingRepository repository;
	private Session session;
	private ObservationManager observationManager;
	
	private WhiteboardUtil whiteboardUtil;	
	
	private Map<String, List<ComponentListener>> componentListeners = new ConcurrentHashMap<String, List<ComponentListener>>();
	private Map<String, List<ComponentListener>> propertyListeners = new ConcurrentHashMap<String, List<ComponentListener>>();
	
	private EventListener eventListener = new EventListener() {
		
		public void onEvent(EventIterator iter) {
			while(iter.hasNext()) {
				Event event = iter.nextEvent();
				
				try {
					
					if (event.getType() == Event.NODE_ADDED) {
						
						if(!session.itemExists(event.getPath())) { //if the node gets removed meanwhile
							continue;
						}
						Node node = session.getNode(event.getPath());
						
						if(node.hasProperty("sling:resourceType")) {
							String resourceType = node.getProperty("sling:resourceType").getString();
							
							if (componentListeners.containsKey(resourceType)) {
								
								for(ComponentListener listener : componentListeners.get(resourceType)) {
									listener.postConstruct(node);
								}
							}							
						}
					} else if((event.getType() == Event.PROPERTY_ADDED) || (event.getType() == Event.PROPERTY_CHANGED)) {

						Property property = (Property) session.getItem(event.getPath());
						Node node = property.getParent();						
						
						if (node.hasProperty("sling:resourceType")) {
							String resourceType = node.getProperty("sling:resourceType").getString();
							if (componentListeners.containsKey(resourceType)) {
								
								if (propertyListeners.containsKey(property.getName())) {
									
									for (ComponentListener listener : propertyListeners.get(property.getName())) {
										listener.changed(node, property);
									}
								}
							}
						}

					} else if(event.getType() == Event.PROPERTY_REMOVED) {
						
						Node node = session.getNodeByIdentifier(event.getIdentifier());

						if (node.hasProperty("sling:resourceType")) {
							String resourceType = node.getProperty("sling:resourceType").getString();
							if (componentListeners.containsKey(resourceType)) {
								String propertyName = event.getPath().substring(node.getPath().length() + 1);
								
								if (propertyListeners.containsKey(propertyName)) {

									for (ComponentListener listener : propertyListeners.get(propertyName)) {
										listener.removed(node, propertyName);
									}
								}
							}
						}
					}
					
					session.save();
				} catch (PathNotFoundException pnfe) {
					LOG.info("Probably the node which contains the property fired the event has been removed", pnfe);
				} catch (RepositoryException re) {
					LOG.error("Error occurred when trying to route the events", re);
				}
			}
		}
	};
	
	protected void activate(ComponentContext context) throws RepositoryException {
		
		whiteboardUtil = WhiteboardUtil.create(context.getBundleContext(),
				new WhiteboardUtil.Callback<ComponentListener>() {

					public void register(ComponentListener listener) {
						
						//maintain the component listeners
						String componentType = listener.getObservervedComponentType();
						if(componentListeners.containsKey(componentType)) {
							componentListeners.get(componentType).add(listener);
						} else {
							List<ComponentListener> list = new ArrayList<ComponentListener>();
							list.add(listener);
							componentListeners.put(componentType, list);
						}
						
						//maintain the propertyListeners
						Set<String> observedProperties = listener.getObservedProperties();
						if (observedProperties != null) {
							for (String observedProperty : observedProperties) {

								if (propertyListeners.containsKey(observedProperty)) {
									propertyListeners.get(observedProperty).add(listener);
								} else {
									List<ComponentListener> list = new ArrayList<ComponentListener>();
									list.add(listener);
									propertyListeners.put(observedProperty,list);
								}
							}
						}						
					}

					public void unregister(ComponentListener listener) {
						
						//maintain the component listeners
						String componentType = listener.getObservervedComponentType();
						List<ComponentListener> list = componentListeners.get(componentType);
						if(list.size() <= 1) {
							//only this listener is registered. The key will be removed.
							componentListeners.remove(componentType);
						} else {
							list.remove(listener);
						}
						
						//maintain the propertyListeners
						Set<String> observedProperties = listener.getObservedProperties();
						if (observedProperties != null) {
							for (String observedProperty : observedProperties) {
								list = propertyListeners.get(observedProperty);
								if(list.size() <= 1) {
									propertyListeners.remove(observedProperty);
								} else {
									list.remove(listener);
								}
							}
						}
					}
				}, ComponentListener.class.getName());
		
		
		BundleContext bc = context.getBundleContext();
		
		ServiceReference sr = bc.getServiceReference(SlingRepository.class.getName());
		repository = (SlingRepository) bc.getService(sr);
		
		session = repository.loginAdministrative(repository.getDefaultWorkspace());
		observationManager = session.getWorkspace().getObservationManager();
		
		observationManager.addEventListener(eventListener, Event.NODE_ADDED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED,
				"/content", true, null, new String[]{"nt:unstructured"}, true);
	}
	
	protected void deactivate(ComponentContext context) throws RepositoryException {

		observationManager.removeEventListener(eventListener);
		whiteboardUtil.close();
		session.logout();
	}
	
}
