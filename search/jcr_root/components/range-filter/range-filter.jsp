<%@page import="com.day.cq.wcm.api.WCMMode"%></br>
<%@include file="/libs/foundation/global.jsp"%>

<cq:includeClientLib categories="search.jquery" />


<%

//TODO: clean up this code. both the java and the javascript

String[] values = null;
try{
    String v = request.getParameter("cf-" + properties.get("./filterId") + "_l").substring(1);
    v = v.substring(0, v.length()-1);
    
    values = v.split(" TO ");
    
    if(values.length != 2) {
    	throw new RuntimeException();
    }
    
    if(values[0].equals("*")){
    	values[0] = properties.get("minValue", "0");
    }
    if(values[1].equals("*")){
        values[1] = properties.get("maxValue", "10");
    }
    
} catch(Exception e) {
	values = new String[] {properties.get("minValue", "0"), properties.get("maxValue", "10")};
}
%>

<script>
    var minValue = <%= properties.get("minValue", 0) %>
    var maxValue = <%= properties.get("maxValue", 10) %>

    $(function() {
        $( "#slider-range" ).slider({
            range: true,
            min: minValue,
            max: maxValue,
            values: [<%=values[0]%>, <%=values[1]%>],
            slide: function( event, ui ) {

                var min = ui.values[ 0 ] == minValue ? '*' : ui.values[ 0 ];
                var max = ui.values[ 1 ] == maxValue ? '*' : ui.values[ 1 ];
                
                var val = min == max && max == '*' ? "" : "[" + min + " TO " + max + "]";
                
                $( "#<%= properties.get("./filterId")%>" ).val(val);
            }
        });

        var min = $( "#slider-range" ).slider( "values", 0 ) == minValue ? '*' : $( "#slider-range" ).slider( "values", 0 );
        var max = $( "#slider-range" ).slider( "values", 1 ) == maxValue ? '*' : $( "#slider-range" ).slider( "values", 1 );

        var val = min == max && max == '*' ? "" : "[" + min + " TO " + max + "]";
        
        $( "#<%= properties.get("./filterId")%>" ).val( val );
    });
    
</script>

<%
boolean isEdit = WCMMode.fromRequest(request).equals(WCMMode.EDIT);
%>

<% if(isEdit) { %>
RANGE FILTER - START </br>
<% } %>

<label for="<%= properties.get("./filterId")%>"><%= properties.get("./label", "N/A")%></label>
<input type="text" id="<%= properties.get("./filterId")%>" name="cf-<%= properties.get("./filterId")%>_l"" readonly="true"/>

<div id="slider-range"></div>

<% if(isEdit) { %>
</br> RANGE FILTER - END </br>
<% } %>

