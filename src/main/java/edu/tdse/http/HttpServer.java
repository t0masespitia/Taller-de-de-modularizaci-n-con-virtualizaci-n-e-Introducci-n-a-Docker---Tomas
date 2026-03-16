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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class HttpServer {

    private final int port;
    private final Map<String, Function<Map<String, String>, String>> services;
    private final StaticFileService staticFileService;

    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public HttpServer(int port, Map<String, Function<Map<String, String>, String>> services) {
        this.port = port;
        this.services = services;
        this.staticFileService = new StaticFileService();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Servidor corriendo en http://localhost:" + port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> {
                    try {
                        handleClient(clientSocket);
                    } finally {
                        try {
                            if (clientSocket != null && !clientSocket.isClosed()) {
                                clientSocket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        running = false;
        System.out.println("Apagando servidor de forma elegante...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Servidor detenido correctamente.");
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
            if (parts.length < 2) {
                writeResponse(output, "400 Bad Request", "text/plain", "Solicitud inválida");
                return;
            }

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
                String contentType = (body != null && body.trim().startsWith("<")) ? "text/html" : "text/plain";
                writeResponse(output, "200 OK", contentType, body != null ? body : "");
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
        String response = "HTTP/1.1 " + status + "\r\n"
                + "Content-Type: " + contentType + "; charset=UTF-8\r\n"
                + "Content-Length: " + bodyBytes.length + "\r\n"
                + "\r\n";

        output.write(response.getBytes(StandardCharsets.UTF_8));
        output.write(bodyBytes);
        output.flush();
    }

    private void writeBinaryResponse(OutputStream output, String status, String contentType, byte[] body) throws IOException {
        String headers = "HTTP/1.1 " + status + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "\r\n";

        output.write(headers.getBytes(StandardCharsets.UTF_8));
        output.write(body);
        output.flush();
    }
}