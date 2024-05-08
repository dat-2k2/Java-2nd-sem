# Simple JsonParser
## Requirements

- Read JSON string
    - To Java Object
    - To Map<String, Object>
    - To specified class
- Convert Java object to JSON string
- Supports:
    - Classes with fields (primitives, boxing types, null, arrays, classes)
    - Arrays
    - Collections (Lists - for order consistency). 
- Limitations:
    - Not handling cyclic dependencies.
    - Not handling non-representable in JSON types.

## Structure

### Types
JSON has 7 main types: JsonObject, JsonArray, JsonString, JsonNumber, true, false, null and JsonValue (which is the supertype for others). For the grammar detail, see [ECMA-404](https://www.json.org/json-en.html).


### Utilities
Package _json_ contains 4 main classes for action with JSON:
  - JsonParser: parser receives a string buffer and parses it to a proper JsonValue. It is recommended to use the static method _readValue()_ for general parsing.
  - JsonWriter: (utility) writes a JsonValue into a String. May update file-writing and stream-writing later.
  - JsonBuilder: (utility) constructs JsonValue from Object, Array, List, Number and String (including Primitives).
  - JsonConverter: (utility) converts JsonValue to Object (with specified class), Array, List, Number and String (including Primitives)

### Limitations
  - Currently doesn't support inner class field.
