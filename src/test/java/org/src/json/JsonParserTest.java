package org.src.json;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.src.A;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

class JsonParserTest {


    @ParameterizedTest
    @ValueSource(strings =
            {"{\"a1\": 5,\"a2\": -10000000000000,\"a10\": null,\"a3\": 1231.1232,\"a4\": -12312.123341231312,\"a5\": true,\"a6\": 122,\"a7\": \"c\",\"a8\": \"Hel lo\",\"a9\": [{\"x\": 10}, {\"x\": -20}, {\"x\": 10}],\"a0\": null}"})
    public void testReadJsonObject(String str) {
        try{
            JsonConverter.parseObject(JsonParser.readValue(new StringBuffer(str)), A.class);
        }
        catch (ParseException e){
            System.out.println(e.getErrorOffset());
            System.out.println(e.getMessage());
            throw new RuntimeException();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }
    @ParameterizedTest
    @ValueSource(strings = {"\"hello-world\"", "[\"hello-world\"]", "5.234234","232342","-3232","-23423.23423","0","-1E12312","1e12312"})
    public void testReadJsonValue(String str) throws ParseException{
        JsonParser.readValue(new StringBuffer(str));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[\"hello-world\"]"})
    public void testReadJsonArray(String str) throws ParseException {
        JsonConverter.parseArray(JsonParser.readValue(new StringBuffer(str)), String.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"hello-world\""})
    public void testReadJsonString(String str) throws ParseException {
        JsonConverter.parseString(JsonParser.readValue(new StringBuffer(str)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"5.234234","232342","-3232","-23423.23423","0","-1E12312","1.1231e12312"})
    public void testReadJsonNumber(String str) throws ParseException{
        JsonParser.readValue(new StringBuffer(str));
    }
}