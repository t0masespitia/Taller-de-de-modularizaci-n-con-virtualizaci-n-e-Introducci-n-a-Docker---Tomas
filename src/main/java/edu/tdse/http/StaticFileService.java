package edu.tdse.http;

import java.io.IOException;
import java.io.InputStream;

public class StaticFileService {

    public boolean exists(String path) {
        String resourcePath = normalize(path);
        InputStream is = getClass().getResourceAsStream(resourcePath);
        return is != null;
    }

    public byte[] getFileBytes(String path) throws IOException {
        String resourcePath = normalize(path);
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Archivo no encontrado: " + path);
            }
            return is.readAllBytes();
        }
    }

    public String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        return "text/plain";
    }

    private String normalize(String path) {
        if ("/".equals(path)) {
            return "/public/index.html";
        }
        return "/public" + path;
    }
}
