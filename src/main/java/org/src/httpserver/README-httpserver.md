# HTTP 1.1 Server implementation
This is a simple implementation of HTTP 1.1 protocol using ServerSocketChannel. 

## Request and Response
Request and Response are implemented in 2 classes with that names. They all have the first line, the headers and the body.

There are several grammar for headers in the original docs, here only implemented the Entity-Header with structure _(field:value CRLF)\*_

The body is only a sequence of bytes. For structural data like JSON the JSON package in this repos can be used.


## Server
The Server serves clients in turn: iterates over each listened client (key). Each Request coming in a channel is responded by a Response by _serve()_, then the key will be removed.
The Request and Response act like converters, which extracts information from the binary data sequence read from each channel. The data from the Request is passed to a functional object called Handler, which was registered by the Router of the server.

## Limitation
When the connection is unstable, headers may not be fully transferred and error will be invoked. In other word, this server only works well when the connection is stable for the headers to be sent a whole in one time. 
## Using
This server is adapted with the java.net HttpClient, HttpResponse and HttpRequest. The Server is initialized with a host, a port and a Router object. Handling functions are registered with the Router. 

Examples are written in the test package.

## References
[Hypertext Transfer Protocol -- HTTP/1.1](https://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html)