package cqday.search;

public interface SolrIndexer {

	void index();
	
	void makeDirty();
}
