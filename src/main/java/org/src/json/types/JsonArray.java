package org.src.json.types;

import java.util.Arrays;


/**
 * Immutable array
 *
 * @param data
 */
public record JsonArray(JsonValue[] data) implements JsonValue {

    @Override
    public ValueType getValueType() {
        return ValueType.ARRAY;
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }

}
