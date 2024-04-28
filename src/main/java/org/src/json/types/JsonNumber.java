package org.src.json.types;

import java.io.Serial;

//TODO: add BigDecimal, BigInt
public record JsonNumber(String value) implements JsonValue {
    @Override
    public ValueType getValueType() {
        return ValueType.NUMBER;
    }
    public String toString() {
        return value;
    }

    public byte byteValue() throws NumberFormatException{
        return Byte.parseByte(value);
    }
    public short shortValue() throws NumberFormatException{
        return Short.parseShort(value);
    }
    public int intValue() throws NumberFormatException{
        return Integer.parseInt(value);
    }

    public long longValue() throws NumberFormatException{
        return Long.parseLong(value);
    }

    public float floatValue() throws NumberFormatException{
        return Float.parseFloat(value);
    }

    public double doubleValue() {
        return Double.parseDouble(value);
    }
}
