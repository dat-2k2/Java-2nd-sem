# Simple JsonParser
Parsing a JsonObject to a Java object requires access to its non-private non-argument constructor.
## Requirements
- Read JSON string
  - To Java Object
  - To Map<String, Object>
  - To specified class
- Convert Java object to JSON string
- Supports:
  - Classes with fields (primitives, boxing types, null, arrays, classes)
  - Arrays
  - Collections (Lists)
- Limitations:
  - Not handling cyclic dependencies.
  - Not handling non-representable in JSON types. 
    
## Structure
- JSON types: Json Value, include JsonObject, JsonString, JsonNumber, TRUE, FALSE, NULL
- JsonParser: a parser receiving a string and can read the whole string into a JsonValue (if the string is json-parsable)
- JsonWriter: utility class returning the string form of JsonValue 
- JsonBuilder: utility class constructing JsonValue from Object, Array, List, Number and String (including Primitives)
- JsonConverter: utility class converting JsonValue to Object (with specified class), Array, List, Number and String (including Primitives)
## References
https://www.json.org/json-en.html
