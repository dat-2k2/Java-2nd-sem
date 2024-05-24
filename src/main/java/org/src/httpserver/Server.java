package org.src.httpserver;

import org.src.httpserver.bases.HttpConstant;
import org.src.httpserver.bases.HttpMethod;
import org.src.httpserver.bases.HttpStatus;
import org.src.httpserver.bases.HttpVersion;
import org.src.httpserver.exceptions.InvalidHeader;
import org.src.httpserver.exceptions.MethodNotAllowed;
import org.src.httpserver.exceptions.UnsupportedMediaType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Server {

    // Max data size for a single HTTP/1.1 request header
    public static int MAX_REQUEST_HEADERS_LENGTH = 8190; //Apache
    public static int MAX_BODY_LENGTH = 2 * 1024 * 1024; // 2MB
    private final Selector selector;
    private final ServerSocketChannel listener;
    private final Set<SocketChannel> connections;
    private final Router router;
    public final InetSocketAddress address;


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
     * @param s the buffer which token is extracted from
     * @return the next token
     */
    private static String nextToken(ByteBuffer s) throws BufferUnderflowException{
        StringBuilder token = new StringBuilder();
        byte reg1 = 0;
        byte reg2 = 0;
        while (reg1 != HttpConstant.CR || reg2 != HttpConstant.LF){
            reg1 = reg2;
            reg2 = s.get();
            token.append((char) reg2);
        }
        token.delete(token.length()-2,token.length());
        return token.toString();
    }

    public static void main(String[] args) {
        var str = new StringBuilder()
                .append("abc")
                .append(HttpConstant.CRLF)
                .append(HttpConstant.CRLF)
                .append("abc").toString();
        byte[] test = str.getBytes();
        System.out.println(HttpStatus.LengthRequired);
        System.out.println(Arrays.toString(test));
        ByteBuffer t = ByteBuffer.allocate(10);
        byte[] bts = new byte[]{1,2,3};
        t.get(bts);
        System.out.println(t.position());
        System.out.println(t.position());
        System.out.println(t.limit());
    }

    private void serve(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer headerBuffer = ByteBuffer.allocate(MAX_REQUEST_HEADERS_LENGTH);

        int headerRead = client.read(headerBuffer);
        // If client suddenly closes connection
        if (headerRead == -1) {
            System.out.println("No data");
            this.disconnectClient(key);
        }

        // TODO: Read until the header buffer is full to avoid information loss.

        //-----------Headers-------------------------------------
        
        var headerParsingBuffer = ByteBuffer.allocate(headerRead);
        headerParsingBuffer.put(0,headerBuffer,0,headerRead);
        var currentHeader = nextToken(headerParsingBuffer);

        Request request = new Request();

        //get request line
        String[] field = currentHeader.split(String.valueOf(HttpConstant.SP));
        if (field.length < 3){
            this.log(client, null, HttpStatus.BadRequest);
            client.write(ByteBuffer.wrap(new Response(HttpStatus.BadRequest).toString().getBytes()));
            this.disconnectClient(key);
            return;
        }
        try{
            request.setMethod(HttpMethod.valueOf(field[0]));
        } catch (IllegalArgumentException e){
            this.log(client, null, HttpStatus.MethodNotAllowed);
            client.write(ByteBuffer.wrap(new Response(HttpStatus.MethodNotAllowed).toString().getBytes()));
            this.disconnectClient(key);
            return;
        }
        request.setRequestURI(field[1]);
        var version = HttpVersion.get(field[2]);
        if (version == null){
            this.log(client, null, HttpStatus.HTTPVersionNotSupported);
            client.write(ByteBuffer.wrap(new Response(HttpStatus.HTTPVersionNotSupported).toString().getBytes()));
            this.disconnectClient(key);
            return;
        }


        // get each header
        while (!currentHeader.isEmpty()){
            try{
                currentHeader = nextToken(headerParsingBuffer);
                if (currentHeader.isEmpty())
                    break;
                try{
                    var pair = currentHeader.split(":");
                    if (currentHeader.length() < 2){
                        throw new InvalidHeader(currentHeader);
                    }
                    request.header(pair[0].replaceAll(" ","")
                            ,pair[1].replaceAll(" ",""));

                } catch (InvalidHeader e) {
                    this.log(client, request, HttpStatus.BadRequest);
                    client.write(ByteBuffer.wrap(HttpStatus.BadRequest.toString().getBytes()));
                    this.disconnectClient(key);
                    return;
                } catch (UnsupportedMediaType e) {
                    this.log(client, request, HttpStatus.UnsupportedMediaType);
                    client.write(ByteBuffer.wrap(HttpStatus.UnsupportedMediaType.toString().getBytes()));
                    this.disconnectClient(key);
                    return;
                }
            } catch (BufferUnderflowException e){ //end without double CRLF
                if (headerParsingBuffer.limit() == MAX_REQUEST_HEADERS_LENGTH){ // overflow
                    this.log(client, request, HttpStatus.RequestHeaderFieldsTooLarge);
                    client.write(ByteBuffer.wrap(HttpStatus.RequestHeaderFieldsTooLarge.toString().getBytes()));
                    this.disconnectClient(key);
                    return;
                }

                // wrong syntax
                this.log(client, request, HttpStatus.BadRequest);
                client.write(ByteBuffer.wrap(HttpStatus.BadRequest.toString().getBytes()));
                this.disconnectClient(key);
                return;
            }
        }

//        ------------------------Body---------------------------------------------------
        var bodyBufferSize = MAX_BODY_LENGTH;
        if (request.contentLength > 0){
            bodyBufferSize = request.contentLength;
        }
        ByteBuffer bodyBuffer = ByteBuffer.allocate(bodyBufferSize);

        // Retrieve remaining content from the header buffer and push it to the body.
        bodyBuffer.put(headerBuffer);
        try {
            client.read(bodyBuffer);
        } catch (BufferOverflowException e){
            this.log(client, request, HttpStatus.RequestEntityTooLarge);
            client.write(ByteBuffer.wrap(HttpStatus.RequestEntityTooLarge.toString().getBytes()));
            this.disconnectClient(key);
            return;
        }

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
     * @param key is select key contains socket channel from client
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
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                try {
                    if (key.isAcceptable())
                        this.accept(key);
                    if (key.isReadable())
                        this.serve(key);
                } catch (IOException error) {
                    // Cancelled key
                    continue;
                }
                iter.remove();
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
            return;
        }
    }

    /**
     * Shortcut of log for response.
     */
    private void log(SocketChannel client, Request request, Response response) {
        this.log(client, request, response.status);
    }

    /**
     * Log server action.
     * @param client socket channel
     * @param request parsed from client
     * @param responseStatus status generated from application
     */

    private void log(SocketChannel client, Request request, HttpStatus responseStatus) {
        String uri = "?";
        String method = "?";
        if (request != null) {
            uri = request.requestURI;
            method = request.method.toString();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        try {
            System.out.printf("%s %s - %s %s > %s%n",
                    formatter.format(now),
                    client.getRemoteAddress().toString().substring(1),
                    method,
                    uri,
                    responseStatus);
        } catch (IOException error) {

        }
    }

}