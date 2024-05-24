package org.src.json;

import org.src.json.types.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JsonBuilder {

    public static JsonNumber buildJsonNumber(Number number) {
        return new JsonNumber(number.toString());
    }

    public static JsonString buildJsonString(String string) {
        return new JsonString(string);
    }

    // Only accept
    public static JsonArray buildJsonArray(Collection<?> array) {
        return buildJsonArray(array.toArray());
    }

    /**
     * Receive C-style array (immutable data)
     *
     * @param array
     * @return
     */

    public static JsonArray buildJsonArray(Object array) {
        if (!array.getClass().isArray()) {
            throw new ClassCastException("Not a C-style array");
        }

        if (Array.getLength(array) == 0)
            return new JsonArray(new JsonValue[]{});
        else {
            var result = new ArrayList<JsonValue>(Array.getLength(array));
            var cType = array.getClass().getComponentType();

            for (int i = 0; i < Array.getLength(array); i++) {
                if (Array.get(array, i) == null)
                    result.add(JsonValue.NULL);
                else if (cType.isArray()) {
                    result.add(
                            buildJsonArray(Array.get(array, i))
                    );
                } else if (cType.equals(boolean.class) || cType.equals(Boolean.class)) {
                    if ((Boolean) Array.get(array, i))
                        result.add(JsonValue.TRUE);
                    else
                        result.add(JsonValue.FALSE);
                } else if (
                        cType.equals(byte.class) || cType.equals(Byte.class) ||
                                cType.equals(int.class) || cType.equals(Integer.class) ||
                                cType.equals(long.class) || cType.equals(Long.class) ||
                                cType.equals(float.class) || cType.equals(Float.class) ||
                                cType.equals(double.class) || cType.equals(Double.class)
                ) {
                    result.add(
                            buildJsonNumber((Number) Array.get(array, i))
                    );
                } else if (
                        cType.equals(char.class) || cType.equals(Character.class) ||
                                cType.equals(String.class)
                ) {
                    result.add(
                            buildJsonString((String) Array.get(array, i))
                    );
                } else result.add(
                            buildJsonObject(Array.get(array, i))
                    );
            }
            return new JsonArray(result.toArray(new JsonValue[0])); // JVM creates a new array with fit size.
        }
    }

    private static String getPossibleGetter(Field _f) {
        String name = _f.getName();
        return "get" + String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
    }

    public static JsonObject buildJsonObject(Object o) {

        Class<?> c = o.getClass();
        Field[] f = c.getDeclaredFields();
        for (Field field : f) {
            System.out.println(field.getName());
        }
        Map<String, JsonValue> data = new HashMap<>();

        for (var field : f) {
// firstly put Object as the field class. If the value can't be get, set as null (like in js).
            Object value;

            field.setAccessible(true);
            try {
                value = field.get(o);
            } catch (IllegalAccessException e) {
                System.out.println(field.getName() + " is inaccessible, value will not be written into json");
                continue;
            }

            var type = field.getType();
            if (value == null) {
                data.put(
                        field.getName(), JsonValue.NULL
                );
            } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                if ((boolean) value)
                    data.put(field.getName(), JsonValue.TRUE);
                else
                    data.put(field.getName(), JsonValue.FALSE);
            } else if (type.isArray()) { // C-style array (Object[])
                data.put(field.getName(), buildJsonArray(value));
            } else if (
                    type.equals(byte.class) || type.equals(Byte.class) ||
                            type.equals(int.class) || type.equals(Integer.class) ||
                            type.equals(long.class) || type.equals(Long.class) ||
                            type.equals(float.class) || type.equals(Float.class) ||
                            type.equals(double.class) || type.equals(Double.class)
            ) {
                data.put(field.getName(), buildJsonNumber((Number) value));
            } else if (
                    type.equals(char.class) || type.equals(Character.class) ||
                            type.equals(String.class) || type.isEnum()
            ) { // this is a final class so nothing to worry about
                data.put(field.getName(), buildJsonString(value.toString()));
            } else
                data.put(field.getName(), buildJsonObject(value));
        }
        return new JsonObjectImpl(data);
    }

    public static void main(String[] args) {

        class A {
            public final int[] x;

            public A(int[] x) {
                this.x = x;
            }
        }
        class B extends A {
            public B(int[] b) {
                super(b);
            }
        }

        System.out.println(buildJsonArray(new int[][]{{1, 2}, {3, 4}}));
    }

    static void testfunc(Object o) {
        System.out.println(o.getClass());
    }

    public JsonValue buildBoolean(boolean b) {
        return b ? JsonValue.TRUE : JsonValue.FALSE;
    }

}
