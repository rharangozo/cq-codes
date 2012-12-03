<%@page import="com.day.cq.wcm.api.WCMMode"%></br>
<%@include file="/libs/foundation/global.jsp"%><%
boolean isEdit = WCMMode.fromRequest(request).equals(WCMMode.EDIT);
%>

<% if(isEdit) { %>
</br> Resource path : <%= resource.getPath() %> </br>
<% } %>


<% 
   Page thisPage = resource.adaptTo(Page.class);

   if(thisPage != null) {
%>
    <a href="<%=thisPage.getPath() %>.html"><%= thisPage.getTitle() %></a> </br>
    <%= thisPage.getDescription() == null ? "" : thisPage.getDescription() %>

<% } %>

