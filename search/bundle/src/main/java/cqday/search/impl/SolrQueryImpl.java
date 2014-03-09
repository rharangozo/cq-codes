package cqday.search.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import cqday.search.SolrConnector;
import cqday.search.SolrQuery;

/**
 * @scr.service
 * @scr.component metatype="false"
 */
public class SolrQueryImpl implements SolrQuery {

	/** @scr.reference */
	private SolrConnector solrConnector;

	@SuppressWarnings("unchecked")
	public Result query(ValueMap componentProps, Map<String, String[]> parameters) {

		if(!componentProps.containsKey("search-id")) {
			throw new RuntimeException("The index cannot be identified by search id. Specify it on the dialog editor!");
		}
		
		Map<String, String[]> searchParams = new HashMap<String, String[]>();
		
		for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
			if (entry.getKey().startsWith("cf-") && entry.getValue().length != 0 && !entry.getValue()[0].isEmpty()) {
				
				searchParams.put(entry.getKey().substring(3), 
						(entry.getKey().endsWith("_b") == true ? new String[] { "true" } : entry.getValue()));
			}
		}

		return ResultImpl.create(solrConnector.query((String) componentProps.get("search-id"), searchParams));
	}
}

class ResultImpl implements SolrQuery.Result {

	private List<String> paths;

	public static SolrQuery.Result create(QueryResponse queryResponse) {
		
		ResultImpl result = new ResultImpl();
		
		SolrDocumentList results = queryResponse.getResults();
		result.paths = new ArrayList<String>(results.size());
		
		for (SolrDocument doc : results) {
			result.paths.add((String) doc.getFieldValue("path_s"));
		}
		
		return result;
	}

	private ResultImpl() {
	}

	public List<String> getPaths() {
		return paths;
	}

}