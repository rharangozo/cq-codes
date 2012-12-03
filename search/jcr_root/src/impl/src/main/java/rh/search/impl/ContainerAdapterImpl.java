package rh.search.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;

import rh.search.Attribute;
import rh.search.Container;

/**
 * @scr.service
 * @scr.component immediate="true"
 * @scr.property name="adaptables" value="org.apache.sling.api.resource.Resource"
 * @scr.property name="adapters" value="rh.search.Container"
 */
public class ContainerAdapterImpl implements AdapterFactory {

	static final String TARGET = "target";
	static final String ID = "search-id";
	
	/** @scr.reference */  
	private SlingRepository repository;
	/** @scr.reference */
	private ResourceResolverFactory resourceResolverFactory;

	private ResourceResolver resolver;
	private Session session;

	class ContainerImpl implements Container {

		private Node container;

		public ContainerImpl(Node container) {
			this.container = container;
		}

		public List<Attribute> listAttributes() {
			
			try {
				NodeIterator filterIterator = container.getNode("filters").getNodes();
				
				List<Attribute> attributes = new ArrayList<Attribute>((int) filterIterator.getSize());
				
				while(filterIterator.hasNext()) {
					Node filter = filterIterator.nextNode();
					
					Attribute[] atts = resolver.getResource(filter.getPath()).adaptTo(Attribute[].class);

					if((atts != null) && (atts.length != 0)) {						
						attributes.addAll(Arrays.asList(atts));
					}
				}
				
				return attributes;				
			} catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		}

		public NodeIterator targetNodes() {
			
			try {
				Node target = resolver.getResource(container.getProperty(ContainerAdapterImpl.TARGET).getString()).adaptTo(Node.class);
				if(target.getPrimaryNodeType().equals(NodeType.NT_QUERY)) {					
					
					return session.getWorkspace().getQueryManager().getQuery(target).execute().getNodes();
				} else {
					return target.getNodes();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public String getId() {
			try {
				return container.getProperty(ContainerAdapterImpl.ID).getString();
			} catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		}

	}
	
	public <AdapterType> AdapterType getAdapter(Object adaptable,
			Class<AdapterType> type) {

		Resource res = (Resource) adaptable;
		Node node = res.adaptTo(Node.class);

		try {
			verify(node);
		} catch (RepositoryException r) {
			throw new RuntimeException(r);
		}

		return (AdapterType) new ContainerImpl(node);
	}

	private void verify(Node node) throws RepositoryException {
		if (!node.hasProperty(ContainerAdapterImpl.TARGET)) {
			throw new RepositoryException("The container node must own 'target' property. Node : " + node);
		}
		if (!node.hasProperty(ContainerAdapterImpl.ID)) {
			throw new RepositoryException("The container node must own 'id' property. Node : " + node);
		}		
	}
	
	protected void activate(ComponentContext context) throws RepositoryException, LoginException {
		this.session = repository.loginAdministrative(repository.getDefaultWorkspace());
		this.resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
	}
	
	protected void deactivate(ComponentContext context) {
	}
}

