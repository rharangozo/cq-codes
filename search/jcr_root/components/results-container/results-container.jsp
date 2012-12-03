<%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %>
<%@page import="com.day.cq.wcm.api.WCMMode"%>
<%@page import="rh.search.SolrQuery"%>
<%@include file="/libs/foundation/global.jsp"%><%
boolean isEdit = WCMMode.fromRequest(request).equals(WCMMode.EDIT);
%>

<% if(isEdit) { %>
<hr/>
<u>Result container</u>
<% } %>


<%
SolrQuery solrQuery = sling.getService(SolrQuery.class);

SolrQuery.Result result = solrQuery.query(properties, request.getParameterMap());
Session session = resourceResolver.adaptTo(Session.class);

for(String path : result.getPaths()) { 
	
	if(session.itemExists(path)) {
	  
		%><cq:include path="<%=path %>" resourceType="<%=(String)properties.get("viewResource")%>"/><%
	} else {
		if(isEdit) {
			%>The resource [<%= path %>] is removed but still indexed yet<%
		}
	}
} 
%>

<% if(isEdit) { %>
<hr/>
<% } %>