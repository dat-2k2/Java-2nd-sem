package org.src.httpserver;

import org.junit.jupiter.api.Test;
import org.src.httpserver.bases.HttpMethod;
import org.src.httpserver.bases.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    @Test
    public void testServer() throws InterruptedException, IOException {
        var router = new Router();
        router.register("/hello", HttpMethod.GET, request -> {
            var res = new Response(HttpStatus.OK);
            var name =  request.getHeaders().get("name");
            res.body(String.format("Hello %s!", name).getBytes());
            return res;
        });

        Server server = new Server(router, "localhost", 6969);
        new Thread(server::start).start();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:6969/hello"))
                .header("name", "abc")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(response.body(), "Hello abc!");
        server.close();
    }

}