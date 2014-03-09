<%@page import="com.day.cq.wcm.api.WCMMode"%>
<%@include file="/libs/foundation/global.jsp"%>

<%
boolean isEdit = WCMMode.fromRequest(request).equals(WCMMode.EDIT);
if(isEdit) {
	%><hr/><%
}

String viewComponent = properties.get("./view-component", "selectable/components/list-example/simple-resource-view");

String target = properties.get("./target", null);
if((target == null) && isEdit) { 
	%>Please set up the target node on the design dialog!<%
} else {

Resource targetResource = resourceResolver.getResource(target);
Node targetNode = targetResource.adaptTo(Node.class);

NodeIterator iterator = targetNode.getNodes();
while(iterator.hasNext()) {
	Node node = iterator.nextNode();
	%><cq:include path="<%=node.getPath() %>" resourceType="<%=viewComponent%>"/><%
}}

if(isEdit) {
    %><hr/><%
}

%>