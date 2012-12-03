<%@page import="com.day.cq.wcm.api.WCMMode"%>
<%@include file="/libs/foundation/global.jsp"%>

<%
boolean isEdit = WCMMode.fromRequest(request).equals(WCMMode.EDIT);
%>

<% if(isEdit) { %>
CONTAINER - START 
</br>
<% } %>

<form method="GET">
    <cq:include path="filters" resourceType="foundation/components/parsys"/>
    <input type="submit"/>
</form>

<% if(isEdit) { %>
</br> CONTAINER - END </br>
<% } %>