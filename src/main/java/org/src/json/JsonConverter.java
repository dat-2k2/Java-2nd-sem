package org.src.json;

import org.src.json.types.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonConverter {
    static byte parseByte(JsonValue o) {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            throw new NullPointerException("Null value when parsing to integer");

        if (o.getValueType() != JsonValue.ValueType.NUMBER)
            throw new ClassCastException("Value is not a JSON Number");

        JsonNumber numVal = (JsonNumber) o;
        return numVal.byteValue();
    }

    static short parseShort(JsonValue o) {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            throw new NullPointerException("Null value when parsing to integer");

        if (o.getValueType() != JsonValue.ValueType.NUMBER)
            throw new ClassCastException("Value is not a JSON Number");

        JsonNumber numVal = (JsonNumber) o;
        return numVal.shortValue();
    }

    static int parseInt(JsonValue o) {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            throw new NullPointerException("Null value when parsing to integer");

        if (o.getValueType() != JsonValue.ValueType.NUMBER)
            throw new ClassCastException("Value is not a JSON Number");

        JsonNumber numVal = (JsonNumber) o;
        return numVal.intValue();
    }

    static double parseDouble(JsonValue o) {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            throw new NullPointerException("Null value when parsing to double");
        if (o.getValueType() != JsonValue.ValueType.NUMBER)
            throw new ClassCastException("Value is not a JSON Number");

        JsonNumber numVal = (JsonNumber) o;
        return numVal.doubleValue();
    }

    static float parseFloat(JsonValue o) {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            throw new NullPointerException("Null value when parsing to float");
        if (o.getValueType() != JsonValue.ValueType.NUMBER)
            throw new ClassCastException("Value is not a JSON Number");

        JsonNumber numVal = (JsonNumber) o;
        return numVal.floatValue();
    }

    static long parseLong(JsonValue o) {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            throw new NullPointerException("Null value when parsing to long");
        if (o.getValueType() != JsonValue.ValueType.NUMBER)
            throw new ClassCastException("Value is not a JSON Number");

        JsonNumber numVal = (JsonNumber) o;
        return numVal.longValue();
    }

    /**
     * Parse a JsonObject to a new Object with specified Class.
     * The class is required to have a visible (non-private) non-argument constructor.
     * There must be a setter for each parsing field (as POJO).
     * Currently does not support inner class fields.
     */

    static <T> T parseObject(JsonValue v, Class<T> c) throws InvocationTargetException {
        //TODO: construct object

        if (v.getValueType() == JsonValue.ValueType.NULL)
            return null;
        if (v.getValueType() != JsonValue.ValueType.OBJECT)
            throw new ClassCastException("Value is not a JSON Number");

        var o = (JsonObject) v;

        T result;
        try {
            result = c.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No non-argument constructor of class " + c.getName());
        } catch (InstantiationException e) {
            throw new RuntimeException("Cannot init new object of class " + c.getName());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Non-argument constructor of class " + c.getName() + " is inaccessible");
        }


        for (var fname : o.keySet()) {
            try {
                var field = c.getDeclaredField(fname);
                field.setAccessible(true);

                if (o.isNull(fname)) {
                    field.set(result, null);
                } else if (field.getType() == Boolean.class | field.getType() == boolean.class) {
                    if (o.getBoolean(fname))
                        field.set(result, true);
                    else
                        field.set(result, false);
                } else if (field.getType() == Character.class | field.getType() == char.class) {
                    var str = parseString(o.getJsonString(fname));
                    if (str == null)
                        field.set(result, null); // if field is char this gonna invoke error but so far couldn't find any solution
                    else
                        field.set(result, str.charAt(0));
                } else if (field.getType() == Byte.class | field.getType() == byte.class) {
                    field.set(result,
                            parseByte(o.getJsonNumber(fname))
                    );
                } else if (field.getType() == Short.class | field.getType() == short.class) {
                    field.set(result,
                            parseShort(o.getJsonNumber(fname))
                    );
                } else if (field.getType() == Integer.class | field.getType() == int.class) {
                    field.set(result,
                            parseInt(o.getJsonNumber(fname))
                    );
                } else if (field.getType() == Long.class | field.getType() == long.class) {
                    field.set(result,
                            parseLong(o.getJsonNumber(fname))
                    );
                } else if (field.getType() == Float.class | field.getType() == float.class) {
                    field.set(result,
                            parseFloat(o.getJsonNumber(fname))
                    );
                } else if (field.getType() == Double.class | field.getType() == double.class) {
                    field.set(result,
                            parseDouble(o.getJsonNumber(fname))
                    );
                } else if (field.getType() == String.class) {
                    field.set(result,
                            parseString(o.getJsonString(fname))
                    );
                } else if (field.isEnumConstant()) {
                    field.set(result,
                            Enum.valueOf((Class) field.getType(), o.getJsonString(fname).toString()));
                } else if (field.getType().isArray()) {
                    field.set(result,
                            parseArray(o.getJsonArray(fname), field.getType().getComponentType())
                    );
                } else {
                    field.set(result,
                            parseObject(o.getJsonObject(fname), field.getType())
                    );
                }
            } catch (NoSuchFieldException e) {
                throw new ClassCastException("Couldn't find any field with name as " + fname);
            } catch (IllegalArgumentException e) {
                throw new ClassCastException("Value is not an instance of the field " + fname);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Not a POJO: field " + fname + " does not have setter");
            }
        }
        // if c fields are not the same with keys of object, throw
        return result;
    }

    static String parseString(JsonValue o) throws ClassCastException {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            return null;
        if (o.getValueType() != JsonValue.ValueType.STRING)
            throw new ClassCastException("Value is not a JSON Number");

        return ((JsonString) o).value();
    }

    //    require class to have proper setters.
    static <T> T[] parseArray(JsonValue o, Class<T> c) throws ClassCastException {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            return null;
        if (o.getValueType() != JsonValue.ValueType.ARRAY)
            throw new ClassCastException("Not an array: " + o);

        return Objects.requireNonNull(
                parseCollection(o, c, ArrayList.class)
        ).toArray((T[]) Array.newInstance(c, 0));
    }

    static <T> List<T> parseCollection(JsonValue o, Class<T> c, Class<? extends List> collectionType) {
        if (o.getValueType() == JsonValue.ValueType.NULL)
            return null;
        if (o.getValueType() != JsonValue.ValueType.ARRAY)
            throw new ClassCastException("Not an array: " + o);

        var jsonArray = ((JsonArray) o).data();

        try {
            var result = collectionType.getConstructor().newInstance();
            var add = collectionType.getMethod("add", Object.class);
            for (var value : jsonArray) {
                try {
                    if (c == boolean.class || c == Boolean.class) {
                        add.invoke(result, parseBoolean(value));
                    } else if (c == byte.class || c == Byte.class) {
                        add.invoke(result, (byte) parseInt(value));
                    } else if (c == char.class || c == Character.class) {
                        var str = parseString(o);
                        if (str == null)
                            add.invoke(result, null);
                        else
                            add.invoke(result, parseString(o).charAt(0));
                    } else if (c == int.class || c == Integer.class) {
                        add.invoke(result, parseInt(value));
                    } else if (c == long.class || c == Long.class) {
                        add.invoke(result, parseLong(value));
                    } else if (c == double.class || c == Double.class) {
                        add.invoke(result, parseDouble(value));
                    } else if (c == float.class || c == Float.class) {
                        add.invoke(result, parseFloat(value));
                    } else if (c == String.class) {
                        add.invoke(result, parseString(value));
                    } else if (c.isArray()) {
                        add.invoke(result, (Object) parseArray(value, c.getComponentType()));
                    } else {
                        add.invoke(result, parseObject(value, c));
                    }
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("When constructing array of class " + c.getName() + ", an error occurred:\n" + e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot add element");
                }
            }
            return result;

        } catch (InstantiationException e) {
            throw new RuntimeException("Abstract list cannot be initialized: " + collectionType.getName());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access to the non-argument constructor of list class " + collectionType.getName());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("When initializing the " + collectionType.getName() + " object, an error occurred: \n" +
                    e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot get the constructor or method add of the list class: " + collectionType.getName());
        }
    }

    static boolean parseBoolean(JsonValue o) {
        return switch (o.getValueType()) {
            case FALSE -> false;
            case TRUE -> true;
            default -> throw new ClassCastException("Not a boolean: " + o);
        };
    }

    static Object parseNull(JsonValue o) {
        return switch (o.getValueType()) {
            case NULL -> null;
            default -> throw new ClassCastException("Not a null value: " + o);
        };
    }

    public static void main(String[] args) throws Exception {
        // Example class with fields
        String[] arr = new String[]{"abc", "bcd"};
    }
}
