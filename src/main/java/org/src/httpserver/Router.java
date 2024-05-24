package org.src.httpserver;

import org.src.httpserver.bases.HttpMethod;
import org.src.httpserver.bases.HttpStatus;

import java.util.*;

public class Router {
    Response respond(Request request){
        if (!this.handlers.containsKey(request.requestURI))
            return new Response(HttpStatus.NotFound);
        if (!this.handlers.get(request.requestURI).containsKey(request.method))
            return new Response(HttpStatus.MethodNotAllowed);
        return this.handlers.get(request.requestURI)
                            .get(request.method)
                            .apply(request);
    }

    private final Map<String, Map<HttpMethod, Handler>> handlers;

    public Router() {
        this.handlers = new HashMap<>();
    }

    public void register(String uri, HttpMethod method, Handler handler){
        if (handler == null)
            throw new NullPointerException("Handler must be not null");
        if (!handlers.containsKey(uri)){
            handlers.put(uri, new HashMap<>());
        }

        if (!handlers.get(uri).containsKey(method))
            handlers.get(uri).put(method, handler);
    }
}
