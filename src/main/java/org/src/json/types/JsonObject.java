package org.src.json.types;

import java.util.Set;


public interface JsonObject extends JsonValue {
    boolean getBoolean(String name) throws ClassCastException, NullPointerException;

    JsonNumber getJsonNumber(String name) throws ClassCastException, NullPointerException;

    JsonObject getJsonObject(String name) throws ClassCastException, NullPointerException;

    JsonString getJsonString(String name) throws ClassCastException, NullPointerException;

    JsonArray getJsonArray(String name) throws ClassCastException, NullPointerException;

    boolean isNull(String name) throws NullPointerException;

    Set<String> keySet();
}
