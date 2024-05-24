package org.src.httpserver;

import org.src.httpserver.bases.HttpConstant;
import org.src.httpserver.bases.HttpContentType;
import org.src.httpserver.exceptions.InvalidHeader;
import org.src.httpserver.exceptions.UnsupportedMediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * The Request and Response share common in Entity Header and Entity Body.
 * This class implements the Entity Header and Entity Body. Some
 */
public abstract class Entity {
    protected int contentLength = 0;
    protected Map<String, String> headers = new HashMap<>();
    protected HttpContentType contentType = HttpContentType.PLAINTEXT;
    byte[] body;

    Entity() {
        this.headers.put("Content-Type", HttpContentType.PLAINTEXT.toString());
        this.headers.put("Content-Length", String.valueOf(0));
        body = new byte[]{};
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void body(byte[] body) {
        this.body = body;
        this.contentLength = body.length;
        headers.put("Content-Length", String.valueOf(this.contentLength));
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        headers.forEach(
                (key, value) -> builder.append(key)
                        .append(":")
                        .append(value)
                        .append(HttpConstant.CRLF));
        builder.append(HttpConstant.CRLF);
        builder.append(new String(body));
        return builder.toString();
    }

    public void header(String field, String value) throws UnsupportedMediaType {
        switch (field) {
            case "Content-Type": {
                var type = HttpContentType.getType(value);
                if (type == null) {
                    throw new UnsupportedMediaType(value);
                }
                this.contentType = type;
                this.headers.put("Content-Type", type.toString());
                break;
            }
            case "Content-Length": {
                this.contentLength = Integer.parseInt(value);
                this.headers.put("Content-Length", value);
                break;
            }
            default: {
                this.headers.put(field, value);
            }
        }
    }

    public void header(String s) throws InvalidHeader {
        var pair = s.split(":");
        if (s.length() < 2) {
            throw new InvalidHeader(s);
        }
        headers.put(pair[0].replaceAll(" ", "")
                , pair[1].replaceAll(" ", ""));
    }
}
