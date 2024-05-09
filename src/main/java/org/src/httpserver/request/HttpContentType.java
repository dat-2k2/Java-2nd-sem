package org.src.httpserver.request;

public enum HttpContentType {
    APPLICATION,
    MESSAGE,
    MULTIPART;
    @Override
    public String toString(){
        return super.toString().toLowerCase();
    }
}
