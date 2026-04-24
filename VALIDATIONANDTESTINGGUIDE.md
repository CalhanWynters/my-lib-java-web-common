# VALIDATION
- Existence & Nullability: (Is the data present?)
- Origin: Is the data from a legitimate sender?
- Size: Is it reasonably big?
- Lexical content: Does it contain the right characters and encoding?
- Syntax: Is the format right?
- Semantics: Does the data make sense?
- Cross-Field Consistency: (Do multiple values within this object contradict each other?)

The Improved AI Prompt
"Act as a Senior Software Architect specializing in Java 25. Audit the attached code using the 2025 Domain Validation Rubric. Please evaluate the code and implement any missing logic based on these specific criteria:

    Existence & Nullability: Ensure the object immediately rejects null references and blank strings before processing.
    Size & Boundary: Implement strict maximum length limits for Strings (to prevent DoS) and logical ranges for numeric values.
    Lexical Content: Use static final pre-compiled Pattern constants to whitelist allowed characters and prevent injection.
    Syntax: Ensure format-heavy strings (URLs, UUIDs, Dates) are validated using standard Java parsers like java.net.URI or java.time.
    Semantics & Security: Implement host-blocking for URLs to prevent SSRF (blocking private/local IPs) and verify that the data makes logical sense in a domain context.
    Cross-Field Consistency: In records/entities with multiple fields, ensure they do not contradict each other (e.g., startDate must be before endDate).
    Defensive Copying (Immutable Snapshots): If the object contains collections (Lists, Sets, Maps), use List.copyOf() or similar methods in the constructor to ensure the object is a truly immutable snapshot that cannot be modified by the caller.
    Origin Separation: Identify any 'Origin' or 'Sender' validation logic that should be removed from the Domain Object and moved to the Security Layer (API Gateway/Filter).

Output Requirements:

    Provide the corrected Java 25 code using Compact Constructors.
    Explain which validations were added and why they are relevant for 2025 security standards."





# TESTING
- Normal Input Testing
- Boundary Input Testing
- Invalid Input Testing
- Extreme Input Testing






* Consider Taint Analysis from input sources
* Encourage the use of read-once format


