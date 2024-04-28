package org.src.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
class B{
    // cyclic dependency broke here
    int x;

    public B() {
        this.x = 10;
    }

    public B(int x) {
        this.x = x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public boolean equals(Object o){
        return B.class.equals(o.getClass()) && this.x == ((B) o).x;
    }

}
class A{
    Object a0 = null;
    int a1;
    long a2;
    float a3;
    double a4;
    boolean a5;
    byte a6;
    char a7;
    String a8;
    B[] a9;
    A a10;

    A() {
        this(null);
    }

    @Override
    public boolean equals(Object object){
        return(object != null &&
                object.getClass().equals(A.class)
                && ((A) object).a1 == this.a1
                && ((A) object).a2 == this.a2
                && ((A) object).a3 == this.a3
                && ((A) object).a4 == this.a4
                && ((A) object).a5 == this.a5
                && ((A) object).a6 == this.a6
                && ((A) object).a7 == this.a7
                && ((A) object).a8.equals(this.a8)
                && Arrays.equals(((A) object).a9, this.a9)
                && ( ((A) object).a10 == null && this.a10 == null || ((A) object).a10.equals(this.a10))
        );
    };

    public A(A a10) {
        this.a1 = 5;
        this.a2 = -10000000000000L;
        this.a3 = 1231.123123f;
        this.a4 = -12312.1233412313123131;
        this.a5 = true;
        this.a6 = 122;
        this.a7 = 'c';
        this.a8 = "Hel lo";
        this.a9 = new B[]{new B(), new B(-20), new B(10)};
        this.a10 = a10;
    }

    public void setA1(int a1) {
        this.a1 = a1;
    }

    public void setA2(long a2) {
        this.a2 = a2;
    }

    public void setA3(float a3) {
        this.a3 = a3;
    }

    public void setA4(double a4) {
        this.a4 = a4;
    }

    public void setA0(Object a0) {
        this.a0 = a0;
    }

    public void setA5(boolean a5) {
        this.a5 = a5;
    }

    public void setA6(byte a6) {
        this.a6 = a6;
    }

    public void setA7(char a7) {
        this.a7 = a7;
    }

    public void setA8(String a8) {
        this.a8 = a8;
    }

    public void setA9(B[] a9) {
        this.a9 = a9;
    }

    public void setA10(A a10) {
        this.a10 = a10;
    }
}
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