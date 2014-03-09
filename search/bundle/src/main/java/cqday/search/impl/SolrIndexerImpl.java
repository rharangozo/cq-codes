package cqday.search.impl;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cqday.search.Attribute;
import cqday.search.Container;
import cqday.search.SearchHistoryRecorder;
import cqday.search.SolrConnector;
import cqday.search.SolrIndexer;

/**
 * This class produces the list of the nodes to be indexed by the Solr and creates the solr documents form the nodes containing
 * the properties are important from search perspective. These documents will be send to the implementation of the SolrConnector.
 * @scr.service
 * @scr.component metatype="false"
 */
public class SolrIndexerImpl implements SolrIndexer {

	private static final Logger LOG = LoggerFactory.getLogger(SolrIndexerImpl.class);
	private static final String SEARCH_COMPONENTS_FILTER_CONTAINER = "search/components/filter-container";
	
	/** @scr.reference */  
	private SlingRepository repository;
	//TODO: the implementation should be selected by the client because to be possible to use different solr version
	/** @scr.reference */
	private SolrConnector solr;
	
	/** @scr.reference */
	private ResourceResolverFactory resourceResolverFactory;

	//TODO: make it selectable by the client!
	/** @scr.reference */
	private SolrConnector solrConnector;
	
	/** @scr.reference */
	private Scheduler scheduler;
	
	/** @scr.reference */
	private SearchHistoryRecorder recorder;
	
	private ResourceResolver resolver;
	private Session session;
		
	private AtomicBoolean dirty = new AtomicBoolean(true);
	
    /**
     * @scr.property value="0/10 * * * * ?" type="String"
     */
	private String schedulingExpression = "0/10 * * * * ?";

    /**
     * @scr.property name="autoIndex" value="true" type="Boolean"
     */
	private Boolean autoIndex = true;
	
	Runnable indexerJob = new Runnable() {
		public void run() {
			if(dirty.get()) {			
				dirty.set(false);
				index();				
			}
		};
	};
	
	private EventListener dirtyFlagUpdater = new EventListener() {

		public void onEvent(EventIterator iter) {
			makeDirty();
		}
		
	};	
	
	public synchronized void index() {
		
		LOG.info("The index process is starting...");
		
		try {
			recorder.startIndex();
			
			NodeIterator iter = selectFilterContainers();
			while(iter.hasNext()) {
				
				Node node = iter.nextNode();				
				LOG.info("Container node is being processed : " + node.getPath());
				
				Resource res = resolver.getResource(node.getPath());
				Container searchCont = res.adaptTo(Container.class);
				
				LOG.info("Removing entries belonging to the current container [container id : " + searchCont.getId() + "]");
				solrConnector.remove(searchCont.getId());
				
				indexNodes(searchCont);
				
			}			
			solrConnector.commit();
			
			recorder.successful();
			LOG.info("The index process is finished successfully");
			
		} catch (Throwable e) {
			
			solrConnector.rollback();
			recorder.fail(e);
			
			LOG.error("Index process is failed. Cause: ", e);
		}
	}
	
	public void makeDirty() {
		dirty.set(true);
	}

	protected void activate(ComponentContext context) throws Exception {
		this.session = repository.loginAdministrative(repository.getDefaultWorkspace());
		this.resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
		
		Dictionary<String, Object> configuration = context.getProperties();
		
		Object property = configuration.get("autoIndex");		
		if(property != null) {
			autoIndex = (Boolean) property;
		}

		property = configuration.get("schedulingExpression");		
		if(property != null) {
			schedulingExpression = (String) property;
		}

		if(autoIndex) {
			LOG.debug("Registring listener which initiates the reindex process");
			session.getWorkspace().getObservationManager().addEventListener(dirtyFlagUpdater, 
					Event.NODE_ADDED | Event.NODE_MOVED | Event.NODE_REMOVED | 
					Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED,
					"/content", true, null, null, false);

			scheduler.addJob("indexer", indexerJob, null, schedulingExpression, true);
		}
	}
	
	protected void deactivate(ComponentContext context) throws UnsupportedRepositoryOperationException, RepositoryException {
		
		LOG.debug("Unregistring listener which initiates the reindex process");
		scheduler.removeJob("indexer");
		
		if(autoIndex) {
			session.getWorkspace().getObservationManager().removeEventListener(dirtyFlagUpdater);
			//solrConnector.removeAll();
			resolver.close();
		}
	}
		
	private NodeIterator selectFilterContainers() throws RepositoryException {
		
		QueryManager qm = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory qomf = qm.getQOMFactory();
		
		Selector selector = qomf.selector("nt:unstructured", "selector");
		
		Constraint descendant = qomf.descendantNode(selector.getSelectorName(), "/content");
		
		PropertyValue pv = qomf.propertyValue(selector.getSelectorName(), "sling:resourceType");
		Literal lit = qomf.literal(session.getValueFactory().createValue(SEARCH_COMPONENTS_FILTER_CONTAINER));

		Constraint comparison = qomf.comparison(pv, QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, lit);
		
		QueryObjectModel query = qomf.createQuery(selector,
				qomf.and(descendant, comparison), null, null);
		
		QueryResult results = query.execute();
		
		return results.getNodes();
	}

	private void indexNodes(Container searchCont) throws RepositoryException {
		
		List<Attribute> attrs = searchCont.listAttributes();		

		String searchId = searchCont.getId();
		NodeIterator iter = searchCont.targetNodes();
		
		while(iter.hasNext()) {
			Node node = iter.nextNode();

			Map<String, Object> doc = new HashMap<String, Object>();
			
			//The following commands add the mandatory fields
			doc.put("path_s", node.getPath());
			doc.put("search-id", searchId);
			
			for(Attribute attr : attrs) {
				doc.put(attr.getName(), attr.getAttributeValueOf(node));
			}
			
			LOG.info("Document is being added : " + doc);			
			solrConnector.addDoc(doc);
		}		
	}
}
