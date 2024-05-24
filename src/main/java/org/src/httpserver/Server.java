package org.src.httpserver;

import org.src.httpserver.bases.HttpConstant;
import org.src.httpserver.bases.HttpMethod;
import org.src.httpserver.bases.HttpStatus;
import org.src.httpserver.bases.HttpVersion;
import org.src.httpserver.exceptions.InvalidHeader;
import org.src.httpserver.exceptions.UnsupportedMediaType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Server {

    // Max data size for a single HTTP/1.1 request header
    public static int MAX_REQUEST_HEADERS_LENGTH = 8190; //Apache
    public static int MAX_BODY_LENGTH = 2 * 1024 * 1024; // 2MB
    public final InetSocketAddress address;
    final Router router;
    private final Selector selector;
    private final ServerSocketChannel listener;
    private final Set<SocketChannel> connections;


    public Server(Router router, String address, Integer port) throws IOException {
        this.router = router;
        this.listener = ServerSocketChannel.open();
        this.address = new InetSocketAddress(address, port);
        this.listener.bind(this.address);
        this.listener.configureBlocking(false);
        this.selector = Selector.open();
        this.listener.register(selector, SelectionKey.OP_ACCEPT);
        this.connections = new HashSet<>();
    }

    /**
     * A token is a substring which is followed by a CRLF.
     * Read the next token from a char buffer, also move the cursor of the char buffer to right after the crlf
     * (also the head of next token)
     * If an invalid token is at the end of the buffer, it will throw a BufferUnderflowException.
     *
     * @param s the buffer which token is extracted from
     * @return the next token
     */
    private static String nextToken(ByteBuffer s) throws BufferUnderflowException {
        StringBuilder token = new StringBuilder();
        byte reg1 = 0;
        byte reg2 = 0;
        while (reg1 != HttpConstant.CR || reg2 != HttpConstant.LF) {
            reg1 = reg2;
            reg2 = s.get();
            token.append((char) reg2);
        }
        token.delete(token.length() - 2, token.length());
        return token.toString();
    }

    public static void main(String[] args) {
        var str = "abc" +
                HttpConstant.CRLF +
                HttpConstant.CRLF +
                "abc";
        byte[] test = str.getBytes();
        System.out.println(HttpStatus.LengthRequired);
        System.out.println(Arrays.toString(test));
        ByteBuffer t = ByteBuffer.allocate(10);
        byte[] bts = new byte[]{1, 2, 3};
        t.get(bts);
        System.out.println(t.position());
        System.out.println(t.position());
        System.out.println(t.limit());
    }

    private void serve(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(MAX_REQUEST_HEADERS_LENGTH + MAX_BODY_LENGTH);

        int headerRead = client.read(readBuffer);
        // If client suddenly closes connection
        if (headerRead == -1) {
            System.out.println("No data");
            this.disconnectClient(key);
        }

        // TODO: Read until the header buffer is full to avoid information loss.

        //-----------Headers-------------------------------------

        // headerBuffer is allocated with the number of bytes in the first transaction
        // the buffer may LARGER THAN the real headers.
        var headerBuffer = ByteBuffer.allocate(headerRead);
        headerBuffer.put(0, readBuffer, 0, headerRead);
        var currentHeader = nextToken(headerBuffer);

        Request request = new Request();

        //get request line
        String[] field = currentHeader.split(String.valueOf(HttpConstant.SP));
        if (field.length < 3) {
            var badRequest = new Response(HttpStatus.BadRequest);
            this.log(client, null, badRequest);
            client.write(ByteBuffer.wrap(badRequest.toString().getBytes()));
            this.disconnectClient(key);
            return;
        }
        try {
            request.setMethod(HttpMethod.valueOf(field[0]));
        } catch (IllegalArgumentException e) {
            var methodNotAllowed = new Response(HttpStatus.MethodNotAllowed);
            this.log(client, null, methodNotAllowed);
            client.write(ByteBuffer.wrap(methodNotAllowed.toString().getBytes()));
            this.disconnectClient(key);
            return;
        }
        request.setRequestURI(field[1]);
        var version = HttpVersion.get(field[2]);
        if (version == null) {
            var httpVersionNotSupported = new Response(HttpStatus.HTTPVersionNotSupported);
            this.log(client, null, httpVersionNotSupported);
            client.write(ByteBuffer.wrap(httpVersionNotSupported.toString().getBytes()));
            this.disconnectClient(key);
            return;
        }


        // get each header
        while (!currentHeader.isEmpty()) {
            try {
                currentHeader = nextToken(headerBuffer);
                if (currentHeader.isEmpty())
                    break;
                try {
                    var pair = currentHeader.split(":");
                    if (currentHeader.length() < 2) {
                        throw new InvalidHeader(currentHeader);
                    }
                    request.header(pair[0].replaceAll(" ", "")
                            , pair[1].replaceAll(" ", ""));

                } catch (InvalidHeader e) {
                    var badRequest = new Response(HttpStatus.BadRequest);
                    this.log(client, null, badRequest);
                    client.write(ByteBuffer.wrap(badRequest.toString().getBytes()));
                    this.disconnectClient(key);
                    return;
                } catch (UnsupportedMediaType e) {
                    var unsupportedMediaType = new Response(HttpStatus.UnsupportedMediaType);
                    this.log(client, request, unsupportedMediaType);
                    client.write(ByteBuffer.wrap(unsupportedMediaType.toString().getBytes()));
                    this.disconnectClient(key);
                    return;
                }
            } catch (BufferUnderflowException e) { //end without double CRLF
                if (headerBuffer.limit() == MAX_REQUEST_HEADERS_LENGTH) { // overflow
                    var requestHeaderFieldsTooLarge = new Response(HttpStatus.RequestHeaderFieldsTooLarge);
                    this.log(client, request, requestHeaderFieldsTooLarge);
                    client.write(ByteBuffer.wrap(requestHeaderFieldsTooLarge.toString().getBytes()));
                    this.disconnectClient(key);
                    return;
                }

                var badRequest = new Response(HttpStatus.BadRequest);
                this.log(client, null, badRequest);
                client.write(ByteBuffer.wrap(badRequest.toString().getBytes()));
                this.disconnectClient(key);
                return;
            }
        }
        // From this position in the readBuffer counts the bodyBuffer
        int bodyOffset = headerBuffer.position();
//        ------------------------Body---------------------------------------------------

        client.read(readBuffer); // maybe some more
        var bodyBufferSize = readBuffer.position() - bodyOffset;
        if (request.contentLength > 0) {
            bodyBufferSize = request.contentLength;
            if (bodyBufferSize > MAX_BODY_LENGTH) {
                var requestEntityTooLarge = new Response(HttpStatus.RequestEntityTooLarge);
                this.log(client, request, requestEntityTooLarge);
                client.write(ByteBuffer.wrap(requestEntityTooLarge.toString().getBytes()));
                this.disconnectClient(key);
                return;
            }
        }

        ByteBuffer bodyBuffer = ByteBuffer.allocate(bodyBufferSize);
        bodyBuffer.put(0, readBuffer, bodyOffset, bodyBufferSize);

        // Attach body into request
        // Does it need to check backing array existence ?
        request.body(bodyBuffer.array());
        // Response
        Response response = this.router.respond(request);

        try {
            this.log(client, request, response);
            client.write(ByteBuffer.wrap(response.toString().getBytes()));
        } catch (IOException error) {
            // cannot log
            this.disconnectClient(key);
        }
    }

    /**
     * Disconnect client socket and close connection on the given key.
     * @param key of current event from selector
     */
    private void disconnectClient(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            key.cancel();
            client.close();
        } catch (IOException error) {

        }
        this.connections.remove(client);
    }


    /**
     * Accept new connection from client.
     * @param key selection key of the client channel
     */
    private void accept(SelectionKey key) throws IOException {
        SocketChannel client = this.listener.accept();
        client.configureBlocking(false);
        client.register(this.selector, SelectionKey.OP_READ);
        this.connections.add(client);
    }

    /**
     * Start server and waiting events from selector.
     */
    public void start() {
        while (true) {
            try {
                this.selector.select();
            } catch (ClosedSelectorException | IOException error) {
                return;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                try {
                    if (key.isAcceptable())
                        this.accept(key);
                    if (key.isReadable())
                        this.serve(key);
                } catch (IOException error) {
                    // Cancelled key
                    continue;
                }
                it.remove();
            }
        }
    }

    /**
     * Close server and close all client connections.
     */
    public void close() {
        try {
            this.listener.close();
            for (SocketChannel connection : this.connections)
                connection.close();
        } catch (IOException error) {

        }
    }

    /**
     * Log server action.
     *
     * @param client socket channel
     * @param request incoming request, null if the response is BadRequest
     * @param response response
     */

    private void log(SocketChannel client, Request request, Response response) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        try {
            System.out.printf("%s %s > \n%s\n < %s\n\n",
                    formatter.format(now),
                    client.getRemoteAddress().toString().substring(1),
                    request,
                    response);
        } catch (IOException error) {

        }
    }

}