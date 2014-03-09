package cqday.search;

import java.util.List;

import javax.jcr.NodeIterator;

public interface Container {

	String getId();
	
	NodeIterator targetNodes();
	
	List<Attribute> listAttributes();
}
