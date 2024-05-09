package org.src.httpserver.request;

/**
 * https://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Request
 * Only implement Full-Request
 */

public class Request {
    /**
     * Request-Line
     */
    Method method;
//    Using absolute path.
//    RequestURI requestURI;
    HttpVersion httpVersion = HttpVersion.HTTP11;

    RequestHeader requestHeader;
    RequestBody requestBody;

}
