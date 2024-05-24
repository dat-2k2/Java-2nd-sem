package org.src.httpserver.bases;

import java.util.Arrays;

public enum HttpContentType {
    APPLICATION,
    MESSAGE,
    MULTIPART,

    PLAINTEXT("text/plain");
    final String type;

    HttpContentType() {
        this.type = super.toString().toLowerCase();
    }

    HttpContentType(String type) {
        this.type = type;
    }

    public static HttpContentType getType(String type){
        for (var value: HttpContentType.values()){
            if (value.type.equals(type))
                return value;
        }
        return null;
    }
    @Override
    public String toString() {
        return this.type;
    }
}
