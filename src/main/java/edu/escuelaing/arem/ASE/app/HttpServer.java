package edu.escuelaing.arem.ASE.app;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class HttpServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(35000)) {
            System.out.println("Server started. Listening on port 35000...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            if (request != null && request.startsWith("GET")) {
                String uri = request.split(" ")[1];
                String response;
                if ("/".equals(uri)) {
                    response = getIndexResponse();
                } else if (uri.startsWith("/search")) {
                    String fileName = uri.substring("/search?fileName=".length());
                    response = getFileResponse(fileName);
                } else {
                    response = "HTTP/1.1 404 Not Found\r\n\r\n404 Not Found";
                }
                out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private static String getIndexResponse() {
        return "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Buscar Archivo</title>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h2>Buscar Archivo</h2>\n" +
                "        <form action=\"/search\" method=\"GET\">\n" +
                "            <label for=\"fileName\">Nombre del Archivo:</label>\n" +
                "            <input type=\"text\" id=\"fileName\" name=\"fileName\">\n" +
                "            <input type=\"submit\" value=\"Buscar\">\n" +
                "        </form>\n" +
                "    </body>\n" +
                "</html>";
    }

    private static String getFileResponse(String fileName) {
        String basePath = "src/main/resources/public";
        Path filePath = Paths.get(basePath, fileName);

        try {
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String contentType = Files.probeContentType(filePath);
                String content = new String(Files.readAllBytes(filePath));
                return "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\n\r\n" + content;
            } else {
                return "HTTP/1.1 404 Not Found\r\n\r\n404 Not Found";
            }
        } catch (IOException e) {
            return "HTTP/1.1 500 Internal Server Error\r\n\r\n500 Internal Server Error";
        }
    }
}
