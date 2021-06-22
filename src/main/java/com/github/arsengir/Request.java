package com.github.arsengir;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Request {

    private final String method;
    private final String path;
    private List<NameValuePair> params;
    private List<String> headers;
    private String body;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public List<NameValuePair> getParams() {
        return params;
    }

    public Optional<String> getHeader(String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public List<String> getParam(String param) {
        return params.stream()
                .filter(o -> o.getName().startsWith(param))
                .map(NameValuePair::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", params=" + params +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
