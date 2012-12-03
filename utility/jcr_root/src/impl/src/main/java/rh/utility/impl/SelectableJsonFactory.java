package rh.utility.impl;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;

/** 
 * @scr.component immediate="true" metatype="no"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="sling.servlet.paths" value="/system/list/selectable" 
 */  
public class SelectableJsonFactory extends SlingSafeMethodsServlet  {

	private static final String[] ANCESTORS = new String[] { "/content",
			"/apps" };

	/** @scr.reference */  
	private SlingRepository repository;
	
	private Session session;
	
	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) 
		throws ServletException, IOException {
		
		ServletOutputStream out = response.getOutputStream();		 
		String category = request.getParameter("category");
		
		if(category == null) {
			throw new IllegalArgumentException("The 'category' property is mandatory!");
		}
		
		out.println("[");
		
		try {
			for(String ancestor : ANCESTORS) {
				
				NodeIterator iterator = listResources(ancestor, category);
				
				boolean hasNext = iterator.hasNext();
				while(hasNext) {
					
					print(out, iterator.nextNode());
					
					hasNext = iterator.hasNext();
					if(hasNext) {
						out.println(",");
					}
				}				
			}
			
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
		
		out.println("]");
	}

	private void print(ServletOutputStream out, Node node) throws IOException,
			RepositoryException {
		
		out.print("{");
		
		//print out the value property
		
		if(node.hasProperty("selectable-value")) {
			out.print("\"value\" : \"" + node.getProperty("selectable-value").getString() + "\"");
		} else {
			out.print("\"value\" : \"" + node.getPath() + "\"");
		}
		
		out.println(",");
		//print out the text property
		
		if(node.hasProperty("selectable-text")) {
			out.print("\"text\" : \"" + node.getProperty("selectable-text").getString() + "\"");
		} else {
			out.print("\"text\" : \"" + node.getPath() + "\"");		
		}

		out.println(",");
		//print out the qtip property
		
		String qtip = (String) (node.hasProperty("selectable-qtip") == true ? node.getProperty("selectable-qtip").getString() : "");
		out.print("\"qtip\" : \"" + qtip + "\"");		
		
		out.println("}");
	}

	private NodeIterator listResources(String ancestor, String category) throws RepositoryException,
			InvalidQueryException, UnsupportedRepositoryOperationException {
		
		QueryObjectModelFactory qomFactory = session.getWorkspace().getQueryManager().getQOMFactory();
		
		Selector selector = qomFactory.selector("nt:base", "selector");
		
		PropertyValue categoryValue = qomFactory.propertyValue(selector.getSelectorName(), "selectable-category");
		Literal categoryLiteral = qomFactory.literal(session.getValueFactory().createValue(category));
		Comparison categoryComparison = qomFactory.comparison(categoryValue, 
				QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, categoryLiteral);

		Constraint constraint = qomFactory.descendantNode(selector.getSelectorName(), ancestor);
		
		return qomFactory.createQuery(selector, qomFactory.and(categoryComparison, constraint), null, null).execute().getNodes();
	}
	
	protected void activate(ComponentContext context) throws RepositoryException {
		this.session = repository.loginAdministrative(repository.getDefaultWorkspace());
	}
	
	protected void deactivate(ComponentContext context) {
		this.session.logout();
	}
}
