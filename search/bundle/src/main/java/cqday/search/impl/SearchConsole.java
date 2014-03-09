package cqday.search.impl;

import cqday.search.SearchHistoryRecorder;
import cqday.search.SearchHistoryRecorder.Entry;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.felix.webconsole.AbstractWebConsolePlugin;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

/**
 * @scr.component immediate="true" metatype="no"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="felix.webconsole.label" value="searchadmin"
 */
public class SearchConsole extends AbstractWebConsolePlugin {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * @scr.reference
     */
    private SearchHistoryRecorder recorder;

    @Override
    public String getServletName() {
        return "Search Admin";
    }

    @Override
    public String getTitle() {
        return "Search Admin Console";
    }

    @Override
    public String getLabel() {
        return "searchadmin";
    }

    @Override
    protected void renderContent(HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        List<SearchHistoryRecorder.Entry> entries = recorder.getHistory();

        PrintWriter writer = response.getWriter();
        writer.print("<table border=\"0\" bordercolor=\"#FFCC00\" style=\"background-color:#FFFFFF\" width=\"100%\" cellpadding=\"3\" cellspacing=\"3\">");
        printHeader(writer);

        for (SearchHistoryRecorder.Entry entry : entries) {
            printRow(writer, entry);
        }
    }

    private void printRow(PrintWriter writer, Entry entry) {
        writer.print("<tr>");
        writer.print("<td>" + format(entry.getStart()) + "</td>");
        writer.print("<td>" + format(entry.getEnd()) + "</td>");

        writer.print("<td>");
        SearchHistoryRecorder.Entry.Status status = entry.getStatus();

        if (status.equals(SearchHistoryRecorder.Entry.Status.FAILED)) {
            writer.print("Failed : " + entry.getError());
        } else if (status.equals(SearchHistoryRecorder.Entry.Status.INPROGRESS)) {
            writer.print("Inprogress");
        } else {
            writer.print("Successful");
        }
        writer.print("</td>");

        writer.print("</tr>");
    }

    private String format(Date date) {
        if (date == null) {
            return "N/A";
        }
        return DateFormatUtils.format(date, DATE_FORMAT_PATTERN);
    }

    private void printHeader(PrintWriter writer) {
        writer.print("<tr>");
        writer.print("<th>History</th>");
        writer.print("</tr>");

        writer.print("<tr>");
        writer.print("<th>Start</th>");
        writer.print("<th>End</th>");
        writer.print("<th>Status</th>");
        writer.print("</tr>");
    }
}
