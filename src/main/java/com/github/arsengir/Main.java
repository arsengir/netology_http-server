package com.github.arsengir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    private static final int PORT = 9999;

    public static void main(String[] args) {

        Server server = new Server();

        Handler defaultHandler = (request, responseStream) -> {
            final Path filePath = Path.of(".", "public", request.getPath());
            final String mimeType;
            try {
                mimeType = Files.probeContentType(filePath);
                final long length = Files.size(filePath);
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, responseStream);
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        final List<String> defaultPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/events.html", "/events.js");
        for (String validPath : defaultPaths) {
            if (!validPath.equals("")) {
                server.addHandler("GET", validPath, defaultHandler);
            }
        }

        server.addHandler("GET", "/classic.html",
                (request, responseStream) -> {
                    final Path filePath = Path.of(".", "public", request.getPath());
                    final String mimeType;
                    try {
                        mimeType = Files.probeContentType(filePath);
                        final String template = Files.readString(filePath);
                        final byte[] content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();

                        responseStream.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        responseStream.write(content);
                        responseStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        server.listen(PORT);
    }
}
