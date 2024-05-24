package org.src.httpserver.bases;

import static org.src.httpserver.bases.HttpConstant.SP;

/**
 * https://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Status%20Code%20and%20Reason%20Phrase
 */
public enum HttpStatus {
    Continue(100, "Continue"),
    SwitchingProtocols(101, "Switching Protocols"),
    OK(200, "OK"),
    Created(201, "Created"),
    Accepted(202, "Accepted"),
    NonAuthoritativeInformation(203, "Non-Authoritative Information"),
    NoContent(204, "No Content"),
    ResetContent(205, "Reset Content"),
    PartialContent(206, "Partial Content"),
    MultipleChoices(300, "Multiple Choices"),
    MovedPermanently(301, "Moved Permanently"),
    MovedTemporarily(302, "Moved Temporarily"),
    SeeOther(303, "See Other"),
    NotModified(304, "Not Modified"),
    UseProxy(305, "Use Proxy"),
    BadRequest(400, "Bad Request"),
    Unauthorized(401, "Unauthorized"),
    PaymentRequired(402, "Payment Required"),
    Forbidden(403, "Forbidden"),
    NotFound(404, "Not Found"),
    MethodNotAllowed(405, "Method Not Allowed"),
    NoneAcceptable(406, "None Acceptable"),
    ProxyAuthenticationRequired(407, "Proxy Authentication Required"),
    RequestTimeout(408, "Request Timeout"),
    Conflict(409, "Conflict"),
    Gone(410, "Gone"),
    LengthRequired(411, "Length Required"),
    UnlessTrue(412, "Unless True"),
    RequestEntityTooLarge(413, "Request Entity Too Large"),
    UnsupportedMediaType(415, "Unsupported Media Type"),
    RequestHeaderFieldsTooLarge(431, "Request Header Fields Too Large"),
    InternalServerError(500, "Internal Server Error"),
    NotImplemented(501, "Not Implemented"),
    BadGateway(502, "Bad Gateway"),
    ServiceUnavailable(503, "Service Unavailable"),
    GatewayTimeout(504, "Gateway Timeout"),
    HTTPVersionNotSupported(506, "HTTP Version Not Supported");
    public final int code;
    public final String reason;

    HttpStatus(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "" + code + SP + reason;
    }
}
