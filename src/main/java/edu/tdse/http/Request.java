package edu.tdse.http;

import java.util.HashMap;
import java.util.Map;

public class Request {

    private final String path;
    private final Map<String, String> queryParams = new HashMap<>();

    public Request(String fullPath) {
        String[] parts = fullPath.split("\\?", 2);
        this.path = parts[0];

        if (parts.length > 1) {
            String query = parts[1];
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }
}