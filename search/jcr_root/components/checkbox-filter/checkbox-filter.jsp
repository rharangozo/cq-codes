<%@page import="com.day.cq.wcm.api.WCMMode"%></br>
<%@include file="/libs/foundation/global.jsp"%><%
boolean isEdit = WCMMode.fromRequest(request).equals(WCMMode.EDIT);
%>

<% if(isEdit) { %>
CHECKBOX FILTER - START </br>
<% } %>

<input type="checkbox" name="cf-<%= properties.get("./filterId")%>_b">
    
    <%= properties.get("./label", "N/A")%>
    
</input>

<% if(isEdit) { %>
</br> CHECKBOX FILTER - END </br>
<% } %>