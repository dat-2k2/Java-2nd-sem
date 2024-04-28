package org.src.json;

import org.src.json.types.*;

import java.text.ParseException;

public class JsonWriter {
    public static String writeJsonValue(JsonValue jsonValue) throws ParseException {
        return jsonValue.toString();
    }
}
