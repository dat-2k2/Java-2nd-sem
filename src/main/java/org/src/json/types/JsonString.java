package org.src.json.types;

import java.io.Serial;

public record JsonString(String value) implements JsonValue {
    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }

    @Override
    public String toString(){
        return "\"" + value + "\"";
    }
}
