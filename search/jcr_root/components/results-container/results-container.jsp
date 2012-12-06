<%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %>
<%@page import="com.day.cq.wcm.api.WCMMode"%>
<%@page import="rh.search.SolrQuery"%>
<%@include file="/libs/foundation/global.jsp"%><%
boolean isEdit = WCMMode.fromRequest(request).equals(WCMMode.EDIT);
%>

<% if(isEdit) { %>
<hr/>
<u>Result container</u>
<%
if(!properties.containsKey("search-id")) {
	%>This component cannot work without the search-id would be specified on the edit dialog. Please set it up!<%
}
%>
<% } %>


<%
if(properties.containsKey("search-id")) {
    SolrQuery solrQuery = sling.getService(SolrQuery.class);

    SolrQuery.Result result = solrQuery.query(properties, request.getParameterMap());
    Session session = resourceResolver.adaptTo(Session.class);

    for(String path : result.getPaths()) { 

       if(session.itemExists(path)) {
	 
	       %><cq:include path="<%=path %>" resourceType="<%=properties.get("viewResource", "/apps/search/components/results-container/simple-page-view")%>"/><%
	   } else {
		   if(isEdit) {
		     %>The resource [<%= path %>] is removed but still indexed yet<%
		   }
	   }
    } 
}
%>

<% if(isEdit) { %>
<hr/>
<% } %>