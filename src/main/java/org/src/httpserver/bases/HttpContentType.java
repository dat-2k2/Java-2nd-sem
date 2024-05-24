package org.src.httpserver.bases;

public enum HttpContentType {
    APPLICATION_JSON("application/json"),
    MESSAGE,
    MULTIPART_FORM_DATA("multipart/form-data"),
    PLAINTEXT("text/plain");
    final String type;

    HttpContentType() {
        this.type = super.toString().toLowerCase();
    }

    HttpContentType(String type) {
        this.type = type;
    }

    public static HttpContentType getType(String type) {
        for (var value : HttpContentType.values()) {
            if (value.type.equalsIgnoreCase(type))
                return value;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
