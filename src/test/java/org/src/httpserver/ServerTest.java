package org.src.httpserver;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.src.A;
import org.src.httpserver.bases.HttpContentType;
import org.src.httpserver.bases.HttpMethod;
import org.src.httpserver.bases.HttpStatus;
import org.src.json.JsonBuilder;
import org.src.json.JsonConverter;
import org.src.json.JsonParser;
import org.src.json.JsonWriter;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    static final int PORT = 6969;
    static Server server;
    HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void init() throws IOException {
        server = new Server(new Router(), "localhost", PORT);
        new Thread(server::start).start();
    }

    @Test
    public void testGetRequest() throws IOException, InterruptedException {
        server.router.register("/hello", HttpMethod.GET, request -> {
            var res = new Response(HttpStatus.OK);
            var name =  request.getHeaders().get("name");
            res.body(String.format("Hello %s!", name).getBytes());
            return res;
        });

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:6969/hello"))
                .header("name", "abc")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(response.body(), "Hello abc!");
    }


    @Test
    public void testPostRequest() throws InterruptedException, IOException, ParseException {
        var testObject = new A();
        server.router.register("/jsonTest", HttpMethod.POST, request -> {
            var body = request.body;
            try {
                var object = JsonConverter.parseObject(
                        JsonParser.readValue(new StringBuffer(new String(body))),
                        A.class);
                assert object != null;
                if (object.equals(testObject)){
                    return new Response(HttpStatus.OK);
                }
                else
                    return new Response(HttpStatus.InternalServerError);
            } catch (ParseException e) {
                return new Response(HttpStatus.InternalServerError);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:6969/jsonTest"))
                .header("Content-Type", HttpContentType.APPLICATION_JSON.toString())
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(
                        JsonWriter.writeJsonValue(JsonBuilder.buildJsonObject(testObject))
                    )
                )
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(response.statusCode(), HttpStatus.OK.code);
    }

    @AfterAll
    static void destruct(){
        server.close();
    }

}