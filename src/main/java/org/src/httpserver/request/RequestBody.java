package org.src.httpserver.request;

import org.src.json.JsonBuilder;
import org.src.json.JsonConverter;
import org.src.json.JsonParser;
import org.src.json.JsonWriter;
import org.src.json.types.JsonObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class RequestBody {
    HttpContentType contentType;
    int contentLength;
    byte[] serializedData;

    public RequestBody(HttpContentType contentType, byte[] serializedData) {
        this.contentType = contentType;
        this.serializedData = serializedData;
        this.contentLength = serializedData.length;
    }

    @Override
    public String toString(){
        try {
            return JsonWriter.writeJsonValue(
                    JsonBuilder.buildJsonObject(this)
            );
        } catch (ParseException e) {
            throw new RuntimeException("Cannot write the request body to string");
        }
    }

    public static void main(String[] args) throws ParseException {
        RequestBody test = new RequestBody(HttpContentType.MESSAGE, "hello world".getBytes());
        System.out.println(JsonParser.readValue(new StringBuffer(test.toString())));
    }
}
