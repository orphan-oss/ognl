# Response for Issue #142

## Summary
This issue is **not a bug**. The error occurs because OGNL interprets the forward slash (`/`) as a division operator, which is the correct and expected behavior for an expression language. The solution is to use bracket notation with quotes for map keys containing special characters.

---

## Detailed Response

Thank you for reporting this issue! However, this is not a bug in OGNL but rather a case of incorrect syntax usage.

### Why This Happens

OGNL is an **expression language**, similar to JavaScript or SpEL. The forward slash (`/`) is a division operator in OGNL, just as it is in Java and most programming languages. When you write:

```java
Ognl.parseExpression("receive/borrow_time")
```

OGNL interprets this as a mathematical expression: `receive ÷ borrow_time`, which attempts to divide one property by another. Since one or both properties don't exist or evaluate to zero, you get the `ArithmeticException: / by zero`.

### The Solution: Bracket Notation

When accessing map keys (or any properties) that contain special characters or operators, you must use **bracket notation with quotes**:

```java
Map<String, Object> root = new HashMap<>();
root.put("receive/borrow_time", "2022/01/05");
Map context = Ognl.createDefaultContext(root, new DefaultMemberAccess());

// ✅ Solution 1: Bracket notation with single quotes
Object tree = Ognl.parseExpression("['receive/borrow_time']");
Object value = Ognl.getValue(tree, context, root);
System.out.println(value); // Outputs: 2022/01/05

// ✅ Solution 2: Bracket notation with double quotes
Object tree = Ognl.parseExpression("[\"receive/borrow_time\"]");
Object value = Ognl.getValue(tree, context, root);

// ✅ Solution 3: Using #root reference
Object tree = Ognl.parseExpression("#root['receive/borrow_time']");
Object value = Ognl.getValue(tree, context, root);
```

### Why Bracket Notation?

This is the standard approach across all expression languages:

| Language | Special Characters in Keys | Syntax |
|----------|---------------------------|--------|
| JavaScript | `obj['my-key']` or `obj["my-key"]` | Bracket notation required |
| Python | `obj['my-key']` | Bracket notation required |
| OGNL | `['my-key']` or `["my-key"]` | Bracket notation required |
| SpEL (Spring) | `#obj['my-key']` | Bracket notation required |

### Documentation References

This behavior is documented in the [OGNL Language Guide](https://github.com/orphan-oss/ognl/blob/master/docs/LanguageGuide.md):

1. **Section: Referring to Properties**
   > "Maps treat all property references as element lookups or storage, with the property name as the key."

2. **Section: Indexing**
   > "As discussed above, the 'indexing' notation is actually just property reference, though a computed form of property reference rather than a constant one."
   >
   > Example: `array["length"]`

### Other Special Characters

This same approach applies to any property name containing OGNL operators or special characters:

```java
// Keys with other operators
map.put("price-discount", 100);     // Use: ['price-discount']
map.put("quantity*factor", 50);     // Use: ['quantity*factor']
map.put("rate+bonus", 75);          // Use: ['rate+bonus']

// Access them with bracket notation
Ognl.getValue("['price-discount']", context, map);
Ognl.getValue("['quantity*factor']", context, map);
Ognl.getValue("['rate+bonus']", context, map);
```

### Recommendation

While OGNL cannot change this behavior without breaking backward compatibility for thousands of existing applications, consider using property names without operators when possible:

```java
// Better naming alternatives
root.put("receiveBorrowTime", "2022/01/05");    // Access: receiveBorrowTime
root.put("receive_borrow_time", "2022/01/05");  // Access: receive_borrow_time (underscore is safe)
```

If you must use keys with special characters (perhaps from external data sources), always remember to use bracket notation.

### Closing

I'm closing this issue as **"working as intended"** since OGNL is functioning correctly according to its design as an expression language. The solution is to use the documented bracket notation syntax for map keys containing special characters.

If you have further questions about OGNL syntax or need clarification, please feel free to ask!
