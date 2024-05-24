package org.src.httpserver.bases;

public enum HttpMethod {
    /**
     * The GET method means retrieve whatever information (in the form of an entity) is identified by the Request-URI.
     */
    GET,
    /**
     * The POST method is used to request that the destination server accept the entity enclosed in the request
     * as a new subordinate of the resource identified by the Request-URI in the Request-Line
     */
    POST,
    /**
     * The PUT method requests that the enclosed entity be stored under the supplied Request-URI.
     */
    PUT,
    /**
     * The PATCH method is similar to PUT except that the entity contains a list of differences between
     * the original version of the resource identified by the Request-URI
     * and the desired content of the resource after the PATCH action has been applied.
     */
    PATCH,

    /**
     * The DELETE method requests that the origin server delete the resource identified by the Request-URI.
     */
    DELETE
}
