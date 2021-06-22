package com.github.arsengir;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int COUNT_THREADS = 64;

    private final Map<String, Map<String, Handler>> mapHandlers = new HashMap<>();

    public void listen(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            final ExecutorService threadPool = Executors.newFixedThreadPool(COUNT_THREADS);
            while (true) {
                final Socket socket = serverSocket.accept();
                threadPool.submit(new SocketHandler(socket, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (mapHandlers.containsKey(method)) {
            mapHandlers.get(method).put(path, handler);
        } else {
            Map<String, Handler> mapPath = new HashMap<>();
            mapPath.put(path, handler);
            mapHandlers.put(method, mapPath);
        }
    }

    public void runHandler(Request request, BufferedOutputStream responseStream) {

        if (mapHandlers.containsKey(request.getMethod())
                && mapHandlers.get(request.getMethod()).containsKey(request.getPath())) {
            mapHandlers.get(request.getMethod()).get(request.getPath()).handle(request, responseStream);
        } else {
            try {
                responseStream.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}
