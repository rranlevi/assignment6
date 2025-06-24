package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader in) throws IOException {
        // Parse the first line (HTTP method, URI, and version)
        String firstLine = in.readLine();
        if (firstLine == null || firstLine.isEmpty()) {
            return null;
        }

        String[] firstLineParts = firstLine.split(" ");
        if (firstLineParts.length < 2) {
            return null;
        }

        String httpCommand = firstLineParts[0];
        String uri = firstLineParts[1];

        // Parse URI segments
        String uriWithoutQuery = uri;
        if (uri.contains("?")) {
            uriWithoutQuery = uri.substring(0, uri.indexOf("?"));
        }
        String[] rawParts = uriWithoutQuery.split("/");
        List<String> cleanedParts = new ArrayList<>();

        for (String part : rawParts) {
            if (!part.isEmpty()) {
                cleanedParts.add(part);
            }
        }

        String[] uriParts = cleanedParts.toArray(new String[0]);

        // Parse query parameters
        Map<String, String> queryParams = new HashMap<>();
        if (uri.contains("?")) {
            String queryString = uri.substring(uri.indexOf("?") + 1);
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }        
        // Parse headers and look for Content-Length
        String line;
        int contentLength = 0;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("content-length:")) {
                String lengthStr = line.substring(line.indexOf(':') + 1).trim();
                try {
                    contentLength = Integer.parseInt(lengthStr);
                } catch (NumberFormatException e) {
                    contentLength = 0;
                }
            }
        }        
        byte[] content = new byte[0];
        // Only read body content if we have a content length or it's a POST request
        if (contentLength > 0 || "POST".equalsIgnoreCase(httpCommand)) {
            // Read form parameters
            line = in.readLine();
            if (line != null && line.contains("=")) {
                String[] bodyParams = line.split("&");
                for (String param : bodyParams) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2) {
                        queryParams.put(keyValue[0], keyValue[1]);
                    }
                }
                
                // Skip the empty line after form parameters
                in.readLine();
            } else if (line != null && !line.isEmpty()) {
                // If there's no form parameters but already content, put it back
                in = new BufferedReader(new java.io.StringReader(line + "\n" + in.readLine()));
            }        
            // Read the actual content
            StringBuilder contentBuilder = new StringBuilder();
            String contentLine = in.readLine();
            if (contentLine != null) {
                contentBuilder.append(contentLine).append("\n");
            }
            
            content = contentBuilder.toString().getBytes();
        }

        return new RequestInfo(httpCommand, uri, uriParts, queryParams, content);
    }

    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriParts;
        private final Map<String, String> queryParams;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriParts, Map<String, String> queryParams, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriParts = uriParts;
            this.queryParams = queryParams;
            this.content = content;
        }

        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String[] getUriSegments() {
            return uriParts;
        }

        public Map<String, String> getParameters() {
            return queryParams;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
