package rh.search;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

public class AttributeImpl implements Attribute {

	@SuppressWarnings("unchecked")
	private static Set<Class> SUPPORTED_CLASSES;
	private String name;
	@SuppressWarnings("unchecked")
	private Class clazz;
	private String[] paths;

	static {
		Set<Class> set = new HashSet<Class>();
		set.add(String.class);
		set.add(Long.class);
		set.add(Boolean.class);
		set.add(Calendar.class);
		set.add(BigDecimal.class);
		set.add(Double.class);
		SUPPORTED_CLASSES = Collections.unmodifiableSet(set);
	}
	
	/**
	 * Create new instance for single attribute
	 * @param name Name of the attribute
	 * @param clazz Class of the attribute (String, Boolean, Long, Calendar, BigDecimal, Double)
	 * @param path Relative path to the property
	 */
	public AttributeImpl(String name, Class clazz, String path) {

		validate(name, clazz);
		
		this.name = name + "_" + clazz.getSimpleName().toLowerCase().charAt(0);
		this.clazz = clazz;
		this.paths = new String[] { path };
	}

	/**
	 * Create new instance for multiple attributes
	 * @param name Name of the attribute
	 * @param clazz Class of the element of the attributes (String, Boolean, Long, Calendar, BigDecimal, Double)
	 * @param path Relative paths to the properties 
	 */
	public AttributeImpl(String name, Class clazz, String[] path) {

		validate(name, clazz);		

		this.name = name + "_" + clazz.getSimpleName().toLowerCase().charAt(0)
				+ "s";
		this.clazz = clazz;
		this.paths = path;
	}

	@SuppressWarnings("unchecked")
	public Object getAttributeValueOf(Node node) {

		if (paths.length == 1) {
			return toObj(node, paths[0]);
		} else {
			List values = new ArrayList();
			for (String path : paths) {
				Object o = toObj(node, path);
				if (o != null) {
					values.add(o);
				}
			}
			return values;
		}
	}
	
	private Object toObj(Node node, String path) {
		try {
			if (node.hasProperty(path)) {
				Property property = node.getProperty(path);

				if (clazz.equals(Boolean.class)) {
					return property.getBoolean();
				} else if (clazz.equals(Long.class)) {
					return property.getLong();
				} else if (clazz.equals(String.class)) {
					return property.getString();
				} else if (clazz.equals(Calendar.class)) {
					return property.getDate();
				} else if (clazz.equals(BigDecimal.class)) {
					return property.getDecimal();
				} else if (clazz.equals(Double.class)) {
					return property.getDouble();
				} else {
					throw new UnsupportedOperationException();
				}
			}
			return null;
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	public Class getType() {
		return clazz;
	}

	@SuppressWarnings("unchecked")
	private void validate(String name, Class clazz) {
		if(name == null || name.isEmpty()) {
			throw new IllegalArgumentException("The name of the attribute must be not empty or null");
		}
		if(!SUPPORTED_CLASSES.contains(clazz)) {
			throw new IllegalArgumentException("The class of the attribute is not supported");
		}
	}

}
