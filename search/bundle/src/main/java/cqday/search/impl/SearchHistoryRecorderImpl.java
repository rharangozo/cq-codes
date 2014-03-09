package cqday.search.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cqday.search.SearchHistoryRecorder;

/**
 * @scr.service
 * @scr.component metatype="false"
 */
public class SearchHistoryRecorderImpl implements SearchHistoryRecorder {

	private static final Logger LOG = LoggerFactory.getLogger(SearchHistoryRecorderImpl.class);
	private static final String SEARCH_BASE_NAME = "search-history";
	private static final String SEARCH_BASE_PATH = "/var/" + SEARCH_BASE_NAME;

	/** @scr.reference */  
	private SlingRepository repository;

	private String recordBase;
	
	private Session session;
	
	private static class EntryImpl implements SearchHistoryRecorder.Entry {

		private Node node;

		public EntryImpl(Node node) {
			this.node = node;
		}

		public Date getEnd() {
			try {
				return node.getProperty("endDate").getDate().getTime();
			} catch (PathNotFoundException pnfe) {
				return null;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public String getError() {
			try {
				return node.getProperty("errorMessage").getString();
			} catch (PathNotFoundException pnfe) {
				return null;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public Date getStart() {
			try {
				return node.getProperty("startTime").getDate().getTime();
			} catch (PathNotFoundException pnfe) {
				return null;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public Status getStatus() {
			if(getEnd() != null) {
				return Status.SUCCESS;
			} else if(getError() != null) {
				return Status.FAILED;
			} else {
				return Status.INPROGRESS;
			}
		}

	}
	
	public void fail(Throwable throwable) {
		try {
			Node target = getCurrentTarget();
			
			target.setProperty("errorMessage", throwable.getMessage());
			session.save();
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Entry> getHistory() {
		
		try {
			
			QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();
			Selector selector = qomf.selector("nt:base", "records");
			
			Constraint startTimeProp = qomf.propertyExistence(selector.getSelectorName(), "startTime");
			
			Constraint descendant = qomf.descendantNode(selector.getSelectorName(), SEARCH_BASE_PATH);
			
			Query query = qomf.createQuery(selector, qomf.and(startTimeProp, descendant), 
					new Ordering[]{qomf.descending(qomf.propertyValue(selector.getSelectorName(), "startTime"))}, null);
			
			NodeIterator iterator = query.execute().getNodes();
			
			List<Entry> list = new ArrayList<Entry>((int) iterator.getSize());
			
			while(iterator.hasNext()) {
				list.add(new EntryImpl(iterator.nextNode()));
			}
			
			return list;
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

	}

	public void startIndex() {
		try {
			Node history = session.getNode(SEARCH_BASE_PATH);
			Calendar start = GregorianCalendar.getInstance();
			Node record = history.addNode(Long.toString(start.getTimeInMillis()));
			record.setProperty("startTime", start);
			session.save();
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	public void successful() {
		try {
			Node target = getCurrentTarget();
			
			target.setProperty("endDate", GregorianCalendar.getInstance());
			session.save();
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}		
	}

	private Node getCurrentTarget() throws RepositoryException,
			InvalidQueryException {
		QueryObjectModelFactory qomf = session.getWorkspace().getQueryManager().getQOMFactory();
		Selector selector = qomf.selector("nt:base", "records");
		
		Constraint descendant = qomf.descendantNode(selector.getSelectorName(), SEARCH_BASE_PATH);
		
		Constraint startTimeProp = qomf.propertyExistence(selector.getSelectorName(), "startTime");
		
		Constraint notExistEndDate = qomf.not(qomf.propertyExistence(selector.getSelectorName(), "endDate"));
		
		QueryObjectModel query = qomf.createQuery(selector, 
				qomf.and(descendant, qomf.and(startTimeProp, notExistEndDate)),
				new Ordering[] {qomf.descending(qomf.propertyValue(selector.getSelectorName(), "startTime"))}, null);
		
		NodeIterator iterator = query.execute().getNodes();
		if(iterator.getSize() > 1) {
			LOG.warn("The search history records are incorrect. It is necessary to fix them manually.");
		}
		Node target = iterator.nextNode();
		return target;
	}

	protected void activate(ComponentContext context) throws Exception {
		this.session = repository.loginAdministrative(repository.getDefaultWorkspace());
		
		try{
			session.getNode(SEARCH_BASE_PATH);
				
		}catch(PathNotFoundException pnfe){
			
			LOG.info("Creating new folder for search history : " + SEARCH_BASE_PATH);
			
			session.getNode("/var").addNode(SEARCH_BASE_NAME);
			session.save();
		}
	}
	
	protected void deactivate(ComponentContext context) throws UnsupportedRepositoryOperationException, RepositoryException {
		session.logout();
	}
}
