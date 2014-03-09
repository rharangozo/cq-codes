package cqday.search.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cqday.search.SolrConnector;

/**
 * @scr.service description="Connector for Solr 4.0.0"
 * @scr.component configurationFactory="true"
 */
public class SolrConnectorImpl implements SolrConnector {

	private static final Logger LOG = LoggerFactory.getLogger(SolrConnectorImpl.class);
	
	/** @scr.reference */  
	private ConfigurationAdmin configAdmin;
	
    /**
     * @scr.property value="http://localhost:8080/solr/filter-search" type="String"
     */
	private String solrURL = "http://localhost:8080/solr/filter-search";
	
	private SolrServer solr;
	
	public void addDoc(Map<String, Object> attributes) {
		
		SolrInputDocument doc = new SolrInputDocument();
		
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			doc.addField(entry.getKey(), entry.getValue());
		}
		
		try {
			solr.add(doc);
			
		} catch (Exception e) {
			
			LOG.error("Error occurred when trying to add document to Solr: ", e);
			throw new RuntimeException(e);
		}
	}

	public void commit() {
		try {
			solr.commit();
		} catch (Exception e) {
			
			LOG.error("Error occurred when trying to commit the changes: ", e);
			throw new RuntimeException(e);
		}
	}
	
	public void rollback() {
		try {
			solr.rollback();
		} catch (Exception e) {
			LOG.error("Error occurred when trying to rollback the changes: ", e);
			throw new RuntimeException(e);
		}
	}
	
	public void remove(String searchId) {
		
		try {			
			solr.deleteByQuery("search-id:\"" + searchId + "\"");
		} catch (Exception e) {
			LOG.error("Error occurred when trying to remove the entries related to search [ search id : " + searchId + "].", e);
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public QueryResponse query(String searchId, Map<String, String[]> parameters) {

		LOG.debug("Building solr query...");
		
		List<String> filterPieces = new ArrayList<String>();
		filterPieces.add("search-id:" + searchId);
		
		List<String> queryPieces = new ArrayList<String>();
		
		for(Map.Entry<String, String[]> entry : parameters.entrySet()) {
			if(!entry.getKey().endsWith("_s") && !entry.getKey().endsWith("_ss")){
				
				for(String value : entry.getValue()) {
					filterPieces.add(entry.getKey() + ":" + value);
				}
			} else {
				
				for(String value : entry.getValue()) {
					queryPieces.add(entry.getKey() + ":*" + value + "*");
				}
			}
		}
		
		String fq = StringUtils.join(filterPieces, " AND ");
		String queryStr = (queryPieces.isEmpty() ? "*:*" : StringUtils.join(queryPieces, " AND "));
		
		LOG.debug(" Solr query : " + queryStr + " filter : " + fq);		
		
		SolrQuery query = new SolrQuery(queryStr);
		query.addFilterQuery(fq);
		
		try {
			return solr.query(query);
			
		} catch (SolrServerException e) {
			
			LOG.error("Error occurred when trying to execute Solr query", e);
			throw new RuntimeException(e);
		}
	}
	
	protected void activate(ComponentContext context) {
		
		Dictionary<String, Object> configuration = context.getProperties();
		
		Object property = configuration.get("solrURL");
		if(property != null){
			solrURL = (String)property;
		}		
		solr = new HttpSolrServer(solrURL);
	}

	protected void deactivate(ComponentContext context) {
		solr.shutdown();
	}

}
