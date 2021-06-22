package com.github.arsengir;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.stream.Stream;

public class SocketHandler implements Runnable {

    private final Socket socket;
    private final Server server;

    public SocketHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final String requestLine = in.readLine();
            final String[] parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            StringBuilder headers = new StringBuilder();
            String line = in.readLine();
            while (line.length() > 0) {
                headers.append(line+"\r\n");
                line = in.readLine();
            }
            StringBuilder body = new StringBuilder();
            String bodyLine;
            while (in.ready()) {
                bodyLine = in.readLine();
                body.append(bodyLine+"\r\n");
            }

            Request request = new Request(parts[0], parts[1], headers.toString(), body.toString());
            server.runHandler(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
