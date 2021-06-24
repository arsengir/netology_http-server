package com.github.arsengir;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SocketHandler implements Runnable {

    private final Socket socket;
    private final Server server;

    public SocketHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (final BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
             final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = getRequest(in, out);
            if (request == null) return;
            server.runHandler(request, out);

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Request getRequest(BufferedInputStream in, BufferedOutputStream out) throws IOException, URISyntaxException {
        // лимит на request line + заголовки
        final int limit = 4096;

        in.mark(limit);
        final byte[] buffer = new byte[limit];

        final int read = in.read(buffer);

        //ищем request line
        final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
        final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            server.badRequest(out);
            return null;
        }
        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

        if (requestLine.length != 3) {
            server.badRequest(out);
            return null;
        }

        if (!requestLine[1].startsWith("/")) {
            server.badRequest(out);
            return null;
        }

        final URI uri = new URI(requestLine[1]);
        Request request = new Request(requestLine[0], uri.getPath());
        request.setQueryParams(URLEncodedUtils.parse(uri, StandardCharsets.UTF_8));

        //ищем заголовки
        final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final int headersStart = requestLineEnd + requestLineDelimiter.length;
        final int headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            server.badRequest(out);
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
        final List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        request.setHeaders(headers);

        //для GET тела нет
        if (!request.getMethod().equals("GET")) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final Optional<String> contentLength = request.getHeader("Content-Length");
            if (contentLength.isPresent()) {
                final int length = Integer.parseInt(contentLength.get());
                final byte[] bodyBytes = in.readNBytes(length);
                request.setBody(new String(bodyBytes));
            }
            // вычитываем Content-Type, чтобы понять есть ли в body параметры
            final Optional<String> contentType = request.getHeader("Content-Type");
            if (contentType.isPresent()) {
                final String type = contentType.get();
                if (type.equals("application/x-www-form-urlencoded")) {
                    request.setPostParams(URLEncodedUtils.parse(request.getBody(), StandardCharsets.UTF_8));
                }
            }

        }
        System.out.println(request);
        return request;
    }

    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
