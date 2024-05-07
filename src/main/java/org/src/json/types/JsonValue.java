package org.src.json.types;


/**
 * JsonValue represents an immutable JSON value. Recommend to implement with 'record'.
 */
public interface JsonValue {
    JsonValue NULL = () -> ValueType.NULL;
    JsonValue TRUE = () -> ValueType.TRUE;
    JsonValue FALSE = () -> ValueType.FALSE;

    ValueType getValueType();

    String toString();

    enum ValueType {
        ARRAY,
        FALSE,
        NULL,
        NUMBER,
        OBJECT,
        STRING,
        TRUE;

        @Override
        public String toString() {
            return switch (this) {
                case NULL -> "null";
                case TRUE -> "true";
                case FALSE -> "false";
                default -> null;
            };
        }

    }
}