package rh.search;

public interface SolrIndexer {

	void index();
	
	void makeDirty();
}
