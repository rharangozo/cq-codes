package cqday.search.impl;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import cqday.search.SolrIndexer;

/** 
 * @scr.component immediate="true" metatype="no"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="sling.servlet.paths" value="/system/index" 
 */  
public class IndexerServlet extends SlingSafeMethodsServlet  {

	/** @scr.reference */  
	private SolrIndexer indexer;
	
	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
		
		response.getOutputStream().print("The index process will be started soon...");

		indexer.makeDirty();
	}
}
