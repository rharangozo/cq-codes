<%@page import="com.day.cq.wcm.api.WCMMode"%></br>
<%@include file="/libs/foundation/global.jsp"%><%
boolean isEdit = WCMMode.fromRequest(request).equals(WCMMode.EDIT);
%>

<% if(isEdit) { %>
SEARCH FILTER - START </br>
<% } %>

<%
String parameterName = "cf-" + properties.get("./filterId")+ "_ss"; 
String value = request.getParameter(parameterName);
if(value == null) {
	value = "";
}
%>

<label for="<%=parameterName%>"><%= properties.get("./label", "N/A")%></label>
<input type="text" id="<%=parameterName%>" name="<%=parameterName%>" value="<%=value%>"/>

<% if(isEdit) { %>
</br> SEARCH FILTER - END </br>
<% } %>