package org.src.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.src.A;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JsonBuilderTest {
    @ParameterizedTest
    @ValueSource(ints = {2,3,4,-235,-216})
    void buildJsonNumberInt(int num) {
        JsonBuilder.buildJsonNumber(num);
    }
    @ParameterizedTest
    @ValueSource(longs = {2123123123143L,313122131231233123L,12312312312314L,5123123123131231L,-1L})
    void buildJsonNumberLong(long num) {
        JsonBuilder.buildJsonNumber(num);
    }
    @ParameterizedTest
    @ValueSource(floats = {2.23243f,3.23423f,4.12312f,5.2312312f,6.1231323f})
    void buildJsonNumberFloat(float num) {
        JsonBuilder.buildJsonNumber(num);
    }
    @ParameterizedTest
    @ValueSource(doubles = {2.2342,3.1231,4.1231231,5.1e6,6.123121})
    void buildJsonNumberDouble(double num) {
        JsonBuilder.buildJsonNumber(num);
    }
    @ParameterizedTest
    @ValueSource(bytes = {2,3,4,5,6})
    void buildJsonNumberByte(byte num) {
        assertEquals(JsonConverter.parseInt(
                JsonBuilder.buildJsonNumber(num)
        ), num);
    }
    @ParameterizedTest
    @ValueSource(strings = {"adbe","defa","deasda","dasdasd","asdsaa"})
    void buildJsonString(String string) {
        assertEquals(JsonConverter.parseString(
                JsonBuilder.buildJsonString(string)
        ), string);
    }
    @Test
    // this will broke if the constructor is not visible e.g. public constructor of nested class
    void buildJsonArray(){
        var o1 = new A();
        var o2 = new A(o1);
        var o3 = new A(o2);
        var o4 = new A();
        var arr = new A[]{o1,o2,o3,o4};
        A[] r = JsonConverter.parseArray(
                JsonBuilder.buildJsonArray(arr), A.class
        );
        assert (Arrays.equals(arr,r));
    }

    @Test
    void buildJsonObject() throws InvocationTargetException {
        var o2 = new A();
        var r = JsonConverter.parseObject(
                JsonBuilder.buildJsonObject(o2), A.class
        );
        System.out.println(JsonBuilder.buildJsonObject(o2));
        assertEquals(o2,r);
    }

    @Test
    void buildJsonArrayFromCollection() {
        var o1 = new A();
        var o2 = new A(o1);
        var o3 = new A(o2);
        var o4 = new A();
        var arr = new ArrayList<>(Arrays.asList(o1, o2, o3, o4));
        var r = JsonConverter.parseCollection(
                JsonBuilder.buildJsonArray(arr), A.class, ArrayList.class
        );
        assertEquals(arr, r);
    }
}