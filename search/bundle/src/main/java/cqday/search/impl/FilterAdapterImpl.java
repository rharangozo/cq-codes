package cqday.search.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cqday.search.AttributeFactory;

/**
 * @scr.service
 * @scr.component immediate="true"
 * @scr.property name="adaptables" value="org.apache.sling.api.resource.Resource"
 * @scr.property name="adapters" value="[Lrh.search.Attribute;"
 */
public class FilterAdapterImpl implements AdapterFactory {

	private static final Logger LOG = LoggerFactory.getLogger(FilterAdapterImpl.class);
	private static final String SLING_RESOURCE_TYPE = "sling:resourceType";
	
	private Map<String, AttributeFactory> attributeMap = new ConcurrentHashMap<String, AttributeFactory>();

	@SuppressWarnings("unchecked")
	public <AdapterType> AdapterType getAdapter(Object adaptable,
			Class<AdapterType> arg1) {
		
		Resource res = (Resource) adaptable;
		Node filter = res.adaptTo(Node.class);

		if (!validate(filter)) {
			return null;
		}
		
		try {
			String type = filter.getProperty(SLING_RESOURCE_TYPE).getString();
			
			if(attributeMap.containsKey(type)) {
				return (AdapterType) attributeMap.get(type).createAttributes(filter);
			}else{
				LOG.warn("The filter type [" + type + "] is not supported yet. It is referenced at " + filter.getPath());
			}
			
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	protected void activate(ComponentContext context) {
		final BundleContext bc = context.getBundleContext();
		try {
			
			String filter = "(objectclass=" + AttributeFactory.class.getName() + ")";
			
			bc.addServiceListener(new ServiceListener() {
				
				public void serviceChanged(ServiceEvent event) {
					ServiceReference sr = event.getServiceReference();
					switch(event.getType()){
						case ServiceEvent.REGISTERED:
						{
							register((AttributeFactory)bc.getService(sr));
							break;
						}
						case ServiceEvent.UNREGISTERING:
						{
							unregister((AttributeFactory)bc.getService(sr));
							break;
						}
					}
				}
			}, filter);
			
			ServiceReference[] srs = bc.getServiceReferences(null, filter);
			if (srs != null) {
				for (ServiceReference sr : srs) {
					register((AttributeFactory) bc.getService(sr));
				}
			}
			
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}		
	}
	
	protected void unregister(AttributeFactory service) {
		attributeMap.remove(service.getResourceType());
	}

	protected void register(AttributeFactory service) {
		attributeMap.put(service.getResourceType(), service);
	}

	protected void deactivate(ComponentContext context) {
	}
	
	private boolean validate(Node filter) {
		try {
			return filter.hasProperty(SLING_RESOURCE_TYPE);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

}
