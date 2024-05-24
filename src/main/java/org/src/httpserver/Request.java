package org.src.httpserver;

import org.src.httpserver.bases.HttpMethod;
import org.src.httpserver.bases.HttpVersion;

/**
 * https://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Request
 * Only implement Full-Request
 */

public class Request extends Entity {
    /**
     * Request-Line
     */
    HttpMethod method;
    //    Using absolute path.
    String requestURI;
    HttpVersion version = HttpVersion.HTTP_1_1;

    public static void main(String[] args) {

    }

    public HttpMethod getHttpMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }
}
