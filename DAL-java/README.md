# DAL-java

[简体中文](README.zh-CN.md)

`DAL-java` is the core Data Assertion Language implementation in Test Charm. DAL sits between JSON and a general-purpose programming language: it is expressive enough to describe, navigate, transform, and verify data, but small enough to stay readable in hand-written automation code.

The design goal is practical: let people describe test data and expected results in a notation that reads like a compact, human conversation about data. That is why DAL works well with objects, lists, tables, text blocks, schemas, and extension-backed data sources such as files, databases, or UI elements.

It is important to keep one boundary clear from the start: **DAL is not a programming language**. It is a data description language. It intentionally borrows a few computation-like forms only when they help people read or verify data more naturally.

## Add it to a project

```groovy
testImplementation "org.test-charm:DAL-java:<version>"
```

## How most users start

There are two common entry points, and they correspond to two common jobs.

### 1. Assertion entry point

Use `Assertions.expect(...)` when the main purpose is verification:

```java
import static org.testcharm.dal.Assertions.expect;

expect(order).should("""
  : {
    customer: {
      name: 'Alice'
    }
    items: | name   | quantity |
           | Pencil | 2        |
  }
""");
```

This is usually the first style users adopt. It is compact, but it is doing several things at once:

- taking one Java object as the root value
- navigating nested properties
- matching only the fields that matter
- using a table for the collection because that is the most readable shape here

That combination is the essence of DAL.

### 2. Evaluation entry point

Use `DAL` directly when you want a return value:

```java
DAL dal = DAL.dal();

String customer = dal.evaluate(order, "customer.name");
List<Integer> ids = dal.evaluate(order, "items.id[]");
```

`evaluateAll(...)` executes multiple expressions and returns the results as a list. Use this form when DAL is acting more like a compact data query language than like an assertion DSL.

## The mental model

Every DAL expression runs against one root value:

- a Java object
- a `Map`, `List`, or array
- any custom type registered through DAL accessors

From there, DAL lets you:

- access properties and elements
- compare values
- match partial structures
- project lists
- verify tabular data
- validate against schemas
- delegate to extensions for domain-specific data sources

If you keep "one root value, one structural language" in mind, the rest of DAL is much easier to read.

## Core syntax

### Property access

DAL reads:

- public getters
- public fields
- public no-arg methods
- `Map` entries
- custom property accessors
- registered static method extensions

Examples:

```dal
customer.name
items[0].quantity
['display name']
'hello'.length
```

The leading `.` is optional, so `name` and `.name` mean the same thing at the root.

The important point here is not only that DAL can follow these access patterns, but that it treats them as one uniform navigation language. That is why the same style works across objects, maps, lists, and extension-backed types.

### Limited method currying for data access

DAL does support a small amount of method invocation, but this should be understood as **data access**, not as "you can program in DAL now".

For example, if an image-like type exposes a method such as `getPixel(int x, int y)`, DAL can verify it in a curried property style:

```dal
img.getPixel: {
  '0': {
    '0': '#ffffff'
    '1': '#ffff00'
  }
  '1': {
    '0': '#00ffff'
    '1': '#00ff00'
  }
}
```

Read this expression as:

- get the `getPixel` accessor from `img`
- apply the first coordinate
- apply the second coordinate
- compare the resulting pixel values

That is the original purpose of DAL method calls: some useful data-access APIs are naturally method-shaped instead of getter-shaped, especially when they need coordinates, indexes, or other dimensions.

The feature is intentionally limited. It is there so DAL can reach data that lives behind a parameterized accessor, not so business logic is moved into DAL expressions.

### List access and mapping

```dal
items[0]
items[-1]
items.size
items.id[]
products.catalog[].sub.value.string
```

`[]` after a property means "map this property over the current list". DAL intentionally does not do implicit list mapping; you opt into it with `[]`.

That explicitness is important. It keeps expressions readable when the data becomes deeply nested, because the place where list projection happens is visible in the code.

### Equality and matching

- `=` exact equality
- `:` relaxed or structural matching

Typical usage:

```dal
= 42
: { id: 1, name: Alice }
items: [1 2 3]
```

Use `=` when extra fields should fail. Use `:` when the assertion should focus only on the fields that carry meaning for this scenario.

That distinction is one of the reasons DAL stays readable in large suites: strictness is explicit instead of being hidden in assertion helper conventions.

### Tables

Tables are one of DAL's most useful forms for collection assertions:

```dal
: | name   | age |
  | 'Tom'  | 10  |
  | 'Lucy' | 15  |
```

This reads naturally for collection-shaped data and is especially effective when the data is already mentally tabular.

In practice, tables are one of the big reasons DAL feels more hand-written and communicative than raw JSON-like assertion payloads.

### Schemas

Schemas let you verify both structure and value constraints with Java types:

```dal
is OrderSchema
```

Schema fields can define expected types, default values, nested structures, and nullability rules. Use schemas when the assertion is really describing a reusable contract, not just one example object.

### Literals and text blocks

DAL supports:

- numeric literals with Java-oriented suffixes such as `L`, `F`, `BD`, `BI`
- single-quoted and double-quoted strings
- fenced or backtick-style text blocks for multi-line content
- regex matching in verification contexts

These forms matter because real automation assertions need to describe more than plain scalar JSON values.

## Java API surface

The most common entry points are:

```java
<T> T evaluate(Object input, String expression)
<T> List<T> evaluateAll(Object input, String expressions)
Assertions.expect(input).should(expression)
Assertions.expectRun(supplier).should(expression)
```

`Assertions.expectRun(...)` is useful when the value is produced lazily or may throw during evaluation.

## Extending DAL

DAL is designed to unify different data sources behind the same language.

You can register:

- custom property accessors
- custom list accessors
- static method extensions
- schema types
- dumpers for better error output

You can also auto-load extension modules through `dal.extend(...)`, which is how companion modules add file, JDBC, JFactory, and UI support.

This extension model is a big part of the overall stack design. The goal is not only to assert Java objects, but to make many different data sources feel like one coherent language.

## Why DAL is useful in practice

DAL is strongest when you want one notation for all of these tasks:

- verify an in-memory Java object
- read a nested JSON-like payload
- describe a dataset as a table
- assert a database snapshot
- inspect files or ZIP contents
- validate a UI state

That "one language for all data" model is the main reason the rest of the Test Charm stack is built around DAL.

## Repository examples

If you want to learn the language by reading repository examples, these files are good starting points:

- [`src/test/resources/features/doc/access/2-access-property.feature`](src/test/resources/features/doc/access/2-access-property.feature)
- [`src/test/resources/features/dal/method/currying.feature`](src/test/resources/features/dal/method/currying.feature)
- [`src/test/resources/features/dal/property/list-mapping.feature`](src/test/resources/features/dal/property/list-mapping.feature)
- [`src/test/resources/features/dal/verification/object.feature`](src/test/resources/features/dal/verification/object.feature)
- [`src/test/resources/features/dal/verification/list.feature`](src/test/resources/features/dal/verification/list.feature)
- [`src/test/resources/features/dal/verification/table/basic.feature`](src/test/resources/features/dal/verification/table/basic.feature)
- [`src/test/resources/features/dal/verification/schema/single.feature`](src/test/resources/features/dal/verification/schema/single.feature)

## Related projects

- [DAL-extension-basic](../DAL-extension-basic/README.md): adds file, text, async, string, binary, ZIP, and related helpers.
- [DAL-extension-jdbc](../DAL-extension-jdbc/README.md): exposes database tables and associations as DAL data.
- [DAL-extension-jfactory](../DAL-extension-jfactory/README.md): exposes JFactory repository data through spec names.
- [jfactory](../jfactory/README.md): often uses DAL syntax to describe what to create, not only what to verify.
