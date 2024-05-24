package org.src.httpserver;
import org.src.httpserver.bases.HttpContentType;
import org.src.httpserver.bases.HttpStatus;
import org.src.httpserver.bases.HttpVersion;

import static org.src.httpserver.bases.HttpConstant.CRLF;
import static org.src.httpserver.bases.HttpConstant.SP;

/**
 * https://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Response
 */
public class Response extends Entity {
    HttpVersion version = HttpVersion.HTTP11;
    HttpStatus status;

    public Response(HttpStatus status) {
        this.status = status;
    }

    @Override
    public String toString(){
        return version.toString() + SP + status.toString() + CRLF //status-line
                + super.toString();
    }

}
