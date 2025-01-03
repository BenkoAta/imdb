package hu.benkoata.imdb.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Consumer;
@SuppressWarnings("unused")
public class Logger {
    public static final String GET_MAPPING = "GET(%s)[%s]: ";
    public static final String POST_MAPPING = "POST(%s)[%s]: ";
    public static final String PUT_MAPPING = "PUT(%s)[%s]: ";
    public static final String DELETE_MAPPING = "DELETE(%s)[%s]: ";

    public static void logRequest(Consumer<String> writeToLog,
                                  HttpServletRequest httpServletRequest,
                                  String mappingTemplate,
                                  UserDetails userDetails) {
        StringBuilder sb = new StringBuilder()
                .append(getLoggerMessage(httpServletRequest, mappingTemplate, userDetails))
                .append(" ")
                .append(httpServletRequest.getRequestURI());
        if (httpServletRequest.getQueryString() != null && !httpServletRequest.getQueryString().isEmpty()) {
            sb.append("?")
                    .append(httpServletRequest.getQueryString());
        }
        writeToLog.accept(sb.toString());
    }

    private static String getLoggerMessage(HttpServletRequest httpServletRequest,
                                           String mappingTemplate,
                                           UserDetails userDetails) {
        return String.format(mappingTemplate,
                httpServletRequest.getRemoteHost(),
                userDetails != null ? userDetails.getUsername() : "without user");
    }

    public static void logRequest(Consumer<String> writeToLog,
                                  HttpServletRequest httpServletRequest,
                                  String mappingTemplate,
                                  UserDetails userDetails,
                                  String command) {
        logRequest(writeToLog, httpServletRequest, mappingTemplate, userDetails);
        writeToLog.accept(getLoggerMessage(httpServletRequest, mappingTemplate, userDetails) + " " + command);
    }


    private Logger() {
    }
}
