package rh.search;

import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * The implementation of this interface functions as a connector to the Solr. 
 * @author roland_harangozo
 *
 */
public interface SolrConnector {

	/**
	 * Remove the Solr entries related to the given container id.
	 * @param containerId Container id
	 */
	void remove(String containerId);
	
	/**
	 * Add a new entry to the solr.
	 * @param attributes
	 */
	void addDoc(Map<String, Object> attributes);
	
	void commit();
	
	void rollback();

	QueryResponse query(String searchId, Map<String, String[]> searchParams);
}
