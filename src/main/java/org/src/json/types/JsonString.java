package org.src.json.types;

public record JsonString(String value) implements JsonValue {
    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }

    @Override
    public String toString() {
        return value;
    }
}
