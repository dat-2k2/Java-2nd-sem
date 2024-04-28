package org.src.json;

import org.src.json.types.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;


public class JsonParser {
    static final Predicate<Character> isCharacter = character ->
            character.compareTo('\u0020') >= 0
                    && character.compareTo('"') != 0
                    && character.compareTo('\\') != 0;
    static final Predicate<Character> isWhiteSpace = character -> {
        char[] WS = {' ', '\n', '\r', '\t'};
        for (char ws : WS) {
            if (character == ws) {
                return true;
            }
        }
        return false;
    };
    static final Predicate<Character> isHex = character ->
            character.compareTo('0') >= 0
                    && character.compareTo('9') <= 0
                    || character.compareTo('a') >= 0
                    && character.compareTo('f') <= 0
                    || character.compareTo('A') >= 0
                    && character.compareTo('F') >= 0;
    static final Predicate<Character> isDigit = character ->
            character.compareTo('0') >= 0
                    && character.compareTo('9') <= 0;
    static final Predicate<Character> isOneNine = character ->
            character.compareTo('1') >= 0
                    && character.compareTo('9') <= 0;
    static final Predicate<Character> isSign = character ->
            character.compareTo('-') == 0
                    || character.compareTo('+') == 0;
    static final Predicate<Character> isExponent = character ->
            character.compareTo('e') == 0
                    || character.compareTo('E') == 0;
    static final Predicate<Character> isEscape = character ->
            character.compareTo('"') == 0
                    || character.compareTo('\\') == 0
                    || character.compareTo('/') == 0
                    || character.compareTo('b') == 0
                    || character.compareTo('f') == 0
                    || character.compareTo('n') == 0
                    || character.compareTo('r') == 0
                    || character.compareTo('t') == 0;
    static final char OPEN_CURLY_BRACKET = '{';
    static final char CLOSED_CURLY_BRACKET = '}';
    static final char OPEN_SQUARE_BRACKET = '[';
    static final char CLOSED_SQUARE_BRACKET = ']';
    static final char PUNCTUATION_MARK = ':';
    static final char QUOTATION_MARK = '"';
    static final char COMMA = ',';
    static final char DECIMAL_POINT = '.';
    static final char NEGATIVE_MARK = '-';
    static final char BACKSLASH = '\\';


    private StringBuffer buffer;
    private int cursor;

    public JsonParser(StringBuffer buffer) {
        this.buffer = buffer;
        this.cursor = 0;
    }

    public static void main(String[] args) {
        JsonParser testParser = new JsonParser(new StringBuffer("{\"ab\": \"bc\"}"));
        try {
            testParser.readJsonObject();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //supporting functions for the buffer
    private char step() throws IndexOutOfBoundsException {
        return buffer.charAt(cursor++);
    }

    private char peek() throws IndexOutOfBoundsException {
        return buffer.charAt(cursor);
    }

    private void skip() {
        this.cursor++;
    }

    private boolean hasNext() {
        return cursor < buffer.length();
    }

    /**
     * Read until the condition is not true anymore.
     *
     * @param predicate
     * @return lexeme matched the given condition
     */
    String read(Predicate<Character> predicate){
        StringBuilder result = new StringBuilder();
        while (hasNext() && predicate.test(peek())) {
            result.append(step());
        }
        return result.toString();
    }

    /**
     * Look-ahead for pattern.
     * @param pattern
     * @return lexeme matched the given pattern
     */
    String read(StringBuffer pattern) { // synchronized
        StringBuilder result = new StringBuilder();

        int patternCursor = 0;
        int oldCursor = this.cursor;
        try {
            while (hasNext() && patternCursor < pattern.length() && peek() == pattern.charAt(patternCursor++)) {
                result.append(step());
            }
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

        if (result.toString().contentEquals(pattern))
            return result.toString();
        else
        {
            this.cursor = oldCursor; // reset
            return "";
        }

    }

    public JsonObjectImpl readJsonObject() throws ParseException {
        consumeWhiteSpace();
        Map<String, JsonValue> result = new HashMap<>();

        if (peek() != OPEN_CURLY_BRACKET)//use peek to set the thrown position correctly
            throw new ParseException(buffer.toString(), cursor);
        skip();

        consumeWhiteSpace();

        try {
            while (peek() != CLOSED_CURLY_BRACKET) {
//            read members
                // read name
                consumeWhiteSpace();
                String name = readJsonString().value();
                consumeWhiteSpace();

                if (name.isEmpty() && peek() != CLOSED_SQUARE_BRACKET)
                    // empty object but not ending with closed bracket
                    throw new ParseException(buffer.toString(), cursor);

                if (peek() != PUNCTUATION_MARK) {  // no :
                    throw new ParseException(buffer.toString(), cursor);
                }

                //все нормально
                read(new StringBuffer(String.valueOf(PUNCTUATION_MARK)));

//                read value
                JsonValue value = readJsonValue(); // not null !
                result.put(name, value);

                if (peek() == COMMA)
                    skip();
                else if (peek() != CLOSED_CURLY_BRACKET) {
                    throw new ParseException(buffer.toString(), cursor);
                }

            }
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException(buffer.toString(), cursor);
        }

        skip(); // closed bracket
        return new JsonObjectImpl(result);
    }

    public JsonArray readJsonArray() throws ParseException {
        consumeWhiteSpace();

        ArrayList<JsonValue> result = new ArrayList<>();
        if (peek() != OPEN_SQUARE_BRACKET)//use peek to set the thrown position correctly
            throw new ParseException(buffer.toString(), cursor);
        skip();

        consumeWhiteSpace();
        try {
            while (peek() != CLOSED_SQUARE_BRACKET) {
                result.add(readJsonValue()); //remember: JsonValue includes whitespace
                if (peek() != CLOSED_SQUARE_BRACKET) {
                    if (peek() != COMMA)
                        throw new ParseException(buffer.toString(), cursor);
                    else
                        skip(); // skip the comma
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException(buffer.toString(), cursor);
        }
        skip();

        return new JsonArray(result.toArray(new JsonValue[0]));
    }

    //    Remember that value allows whitespace

    public JsonValue readJsonValue() throws ParseException {
        consumeWhiteSpace();
        if (!read(new StringBuffer("true")).isEmpty()) { // look-ahead for true
            consumeWhiteSpace();
            return JsonValue.TRUE;
        } else {
            if (!read(new StringBuffer("false")).isEmpty()) {// look-ahead for false
                consumeWhiteSpace();
                return JsonValue.FALSE;
            } else {
                if (!read(new StringBuffer("null")).isEmpty()) {// look-ahead for null
                    consumeWhiteSpace();
                    return JsonValue.NULL;
                } else {
                    JsonValue result = switch (peek()) {
                        case QUOTATION_MARK -> readJsonString();
                        case OPEN_SQUARE_BRACKET -> readJsonArray();
                        case OPEN_CURLY_BRACKET -> readJsonObject();
                        default -> readJsonNumber();
                    };
                    consumeWhiteSpace();
                    return result;
                }
            }
        }
    }

    public static JsonValue readValue(StringBuffer b) throws ParseException {
        JsonParser reader = new JsonParser(b);
        return reader.readJsonValue();
    }

    public JsonNumber readJsonNumber() throws ParseException {
        StringBuilder result = new StringBuilder();
        // integer part
        consumeWhiteSpace();

        if (!(peek() == NEGATIVE_MARK || isDigit.test(peek())))
            throw new ParseException("Reading decimal part: Not a number value at position "+ cursor +": "
                    + buffer.toString(), cursor);

        if (peek() == NEGATIVE_MARK){
            result.append(step());
        }
        result.append(read(isDigit));

        if (!(hasNext() &&
                (peek() == DECIMAL_POINT || isExponent.test(peek())))
        )
          return new JsonNumber(result.toString()); // return an int

        // fractional part
        try{
            if (peek() == DECIMAL_POINT) {
                result.append(step());
                result.append(read(isDigit));
            }
        } catch (IndexOutOfBoundsException e){
            throw new ParseException("Uncompleted fractional part at position "+ cursor +": "
                    + buffer.toString(), cursor);
        }

        if (!(hasNext() && isExponent.test(peek())))
            return new JsonNumber(result.toString()); // return a float/double

        // exponential part
        try{
            if (isExponent.test(peek())) {
                result.append(step());
                if (isSign.test(peek())) { //exp sign
                    result.append(step());
                } else if (!isDigit.test(peek())) {//must be digit next
                    throw new ParseException("Reading exponential part at position "+ cursor +": "
                            + buffer.toString(), cursor);
                }
                result.append(read(isDigit));
            }
        }
        catch (IndexOutOfBoundsException e){
            throw new ParseException("Uncompleted exponential part:"+ cursor +": "
                    + buffer.toString(), cursor);
        }
        // exponent part

        return new JsonNumber(result.toString());
    }

    private void consumeWhiteSpace() {
        read(isWhiteSpace);
    }

    public JsonString readJsonString() throws ParseException {
        StringBuilder resultBuilder = new StringBuilder();
        if (peek() != QUOTATION_MARK) //use peek to set the thrown position correctly
            throw new ParseException(buffer.toString(), cursor);
        skip();

        try {
            while (peek() != QUOTATION_MARK) {
                if (peek() != BACKSLASH) { //not escape symbol
                    resultBuilder.append(read(isCharacter));
                } else { //escape symbol
                    skip(); // backslash

                    if (peek() == 'u') { //hex
                        skip();

                        for (int i = 0; i < 4; i++) { // 4 digits for hex
                            if (!isHex.test(peek()))
                                throw new ParseException(buffer.toString(), cursor);
                            resultBuilder.append(step());
                        }

                    } else { //other escape
                        if (!isEscape.test(peek()))
                            throw new ParseException(buffer.toString(), cursor);

                        resultBuilder.append(step());
                    }
                }
            }
            skip(); //closed quotation mark
        } catch (IndexOutOfBoundsException | ParseException e) {
            throw new ParseException(buffer.toString(), cursor);
        }

        return new JsonString(resultBuilder.toString());
    }
}
