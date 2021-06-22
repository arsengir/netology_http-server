package com.github.arsengir;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Request {

    private final String method;
    private final String path;
    private List<NameValuePair> queryParams;
    private List<String> headers;
    private String body;
    private List<NameValuePair> postParams;

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

    public void setQueryParams(List<NameValuePair> params) {
        this.queryParams = params;
    }

    public void setPostParams(List<NameValuePair> postParams) {
        this.postParams = postParams;
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

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public List<NameValuePair> getPostParams() {
        return postParams;
    }

    public Optional<String> getHeader(String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public List<String> getQueryParam(String param) {
        return queryParams.stream()
                .filter(o -> o.getName().startsWith(param))
                .map(NameValuePair::getValue)
                .collect(Collectors.toList());
    }

    public List<String> getPostParam(String param) {
        return postParams.stream()
                .filter(o -> o.getName().startsWith(param))
                .map(NameValuePair::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", queryParams=" + queryParams +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                ", postParams=" + postParams +
                '}';
    }
}
