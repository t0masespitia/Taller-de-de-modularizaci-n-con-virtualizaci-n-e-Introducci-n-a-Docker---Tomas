package edu.tdse.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

public class HttpServer {

    private final int port;
    private final Map<String, Function<Map<String, String>, String>> services;
    private final StaticFileService staticFileService;

    public HttpServer(int port, Map<String, Function<Map<String, String>, String>> services) {
        this.port = port;
        this.services = services;
        this.staticFileService = new StaticFileService();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor corriendo en http://localhost:" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
                clientSocket.close();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input))
        ) {
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String fullPath = parts[1];

            Request request = new Request(fullPath);

            if (!"GET".equals(method)) {
                writeResponse(output, "405 Method Not Allowed", "text/plain", "Método no permitido");
                return;
            }

            Function<Map<String, String>, String> service = services.get(request.getPath());

            if (service != null) {
                String body = service.apply(request.getQueryParams());
                String contentType = body.trim().startsWith("<html>") ? "text/html" : "text/plain";
                writeResponse(output, "200 OK", contentType, body);
            } else if (staticFileService.exists(request.getPath())) {
                byte[] content = staticFileService.getFileBytes(request.getPath());
                String contentType = staticFileService.getContentType(request.getPath());
                writeBinaryResponse(output, "200 OK", contentType, content);
            } else {
                writeResponse(output, "404 Not Found", "text/plain", "Recurso no encontrado");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeResponse(OutputStream output, String status, String contentType, String body) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String response =
                "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + contentType + "; charset=UTF-8\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "\r\n";
        output.write(response.getBytes(StandardCharsets.UTF_8));
        output.write(bodyBytes);
    }

    private void writeBinaryResponse(OutputStream output, String status, String contentType, byte[] body) throws IOException {
        String headers =
                "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "\r\n";
        output.write(headers.getBytes(StandardCharsets.UTF_8));
        output.write(body);
    }
}