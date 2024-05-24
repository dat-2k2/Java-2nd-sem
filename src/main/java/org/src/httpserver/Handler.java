package org.src.httpserver;

import java.util.function.Function;

public interface Handler extends Function<Request, Response> {

}
