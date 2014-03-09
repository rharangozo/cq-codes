package cqday.search;

import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;

public interface SolrQuery {

	public static interface Result {
		
		List<String> getPaths();
	}
	
	Result query(ValueMap componentProps, Map<String, String[]> parameters);
}
