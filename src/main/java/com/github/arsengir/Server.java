package com.github.arsengir;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int COUNT_THREADS = 64;

    public void listen (int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            final ExecutorService threadPool = Executors.newFixedThreadPool(COUNT_THREADS);
            while (true) {
                final Socket socket = serverSocket.accept();
                threadPool.submit(new SocketHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
