package org.src.json.types;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record JsonObjectImpl(Map<String, JsonValue> data) implements JsonObject {

    @Override
    public boolean getBoolean(String name) {
        if (!this.data.containsKey(name))
            throw new NullPointerException("Key not found");

        return switch (this.data.get(name).getValueType()) {
            case TRUE -> true;
            case FALSE -> false;
            default -> throw new ClassCastException("Value is not a JSON boolean");
        };
    }


    @Override
    public JsonNumber getJsonNumber(String name) throws ClassCastException, NullPointerException {
        if (!this.data.containsKey(name))
            throw new NullPointerException("Key not found");

        if (this.data.get(name).getValueType() != ValueType.NUMBER)
            throw new ClassCastException("Value is not a JSON number");

        return (JsonNumber) this.data.get(name);
    }

    @Override
    public JsonObject getJsonObject(String name) throws ClassCastException, NullPointerException {
        if (!this.data.containsKey(name))
            throw new NullPointerException("Key not found");

        if (this.data.get(name).getValueType() != ValueType.OBJECT)
            throw new ClassCastException("Value is not a JSON object");

        return (JsonObject) this.data.get(name);
    }

    @Override
    public JsonString getJsonString(String name) throws ClassCastException, NullPointerException {
        if (!this.data.containsKey(name))
            throw new NullPointerException("Key not found");

        if (this.data.get(name).getValueType() != ValueType.STRING)
            throw new ClassCastException("Value is not a JSON string");

        return (JsonString) this.data.get(name);
    }

    @Override
    public JsonArray getJsonArray(String name) throws ClassCastException, NullPointerException {
        if (!this.data.containsKey(name))
            throw new NullPointerException("Key not found");

        if (this.data.get(name).getValueType() != ValueType.ARRAY)
            throw new ClassCastException("Value is not a JSON array");

        return (JsonArray) this.data.get(name);
    }

    @Override
    public boolean isNull(String name) throws NullPointerException {
        if (!this.data.containsKey(name))
            throw new NullPointerException("Key not found");

        return this.data.get(name).getValueType() == ValueType.NULL;
    }

    @Override
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public ValueType getValueType() {
        return ValueType.OBJECT;
    }

    @Override
    public String toString(){
        StringBuffer repr = new StringBuffer("{");
        data.forEach(
                (key, value) -> repr.
                        append("\"").append(key).append("\"")
                        .append(": ")
                        .append(
                        (value.getValueType() == ValueType.TRUE) ? "true" : (
                                value.getValueType() == ValueType.FALSE ? "false" :(
                                        value.getValueType() == ValueType.NULL ? "null" :
                                                value.getValueType() == ValueType.STRING ? ("\"" + value + "\""):
                                                        value
                                        )
                                )
                        )
                        .append(",")
        );
        repr.deleteCharAt(repr.length()-1);
        repr.append("}");
        return repr.toString();
    }

    public static void main(String[] args) {

    }
}
