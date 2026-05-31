# DAL-extension-basic

[简体中文](README.zh-CN.md)

`DAL-extension-basic` is the everyday extension pack for [DAL-java](../DAL-java/README.md). It adds the adapters and helpers that make DAL practical in real automation work: JSON and YAML parsing, file and path inspection, ZIP traversal, richer string and binary checks, list helpers, and async assertions such as `::eventually` and `::await`.

Without this module, DAL is already a capable data language. With this module, DAL becomes much closer to a "single language for the data you actually touch in tests".

## Add it to a project

```groovy
testImplementation "org.test-charm:DAL-extension-basic:<version>"
```

## How the extension becomes available

There are two common usage modes, and they are worth distinguishing because many users only need the first one.

### 1. Add the dependency and use the normal DAL entry points

If you use the standard DAL entry points such as:

- `DAL.dal()`
- `DAL.dal("SomeName")`
- `Assertions.expect(...)`

then the extension is discovered automatically once `DAL-extension-basic` is on the classpath.

In other words, for the normal "use the default DAL instance" workflow, **adding the dependency is usually enough**.

### 2. Build a DAL instance manually

If you create DAL in a more manual way, make the extension loading explicit:

```java
DAL dal = new DAL("manual").extend();
```

That form is useful when you want a dedicated named instance or you are assembling a custom runtime setup yourself.

## What it adds

| Area | What it adds in practice |
| --- | --- |
| Text formats | Parse JSON and YAML from strings, text blocks, and file content |
| Files and paths | Traverse directories, files, and path-like objects as DAL data |
| ZIP and binary | Inspect archive entries, bytes, strings, and hex output |
| Strings | Get better diffs and more readable string failures |
| Async | Retry assertions until a state settles with `::eventually` and `::await` |
| Lists | Use helpers such as filtering and top-N style operations |

## Common usage patterns

### Parse JSON or YAML inline

````text
'{"key":"value"}'.json = { key: value }

```yaml
key: value
```
= { key: value }
````

Why this matters:

- the left side is still just DAL input
- JSON and YAML can be embedded where they are most readable
- once parsed, they behave like ordinary DAL objects

That means a JSON payload, a YAML config fragment, and a Java object can all be verified with the same structural style.

### Assert files and directories as data

```java
Path root = Paths.get("src/test/resources/example-dir");

expect(root).should("""
  : {
    a.json: { key: value }
    b.yaml: { key: VALUE }
  }
""");
```

This example is more important than it first appears. The point is not only that DAL can read files; it is that a directory becomes a navigable data tree:

- file names become properties
- nested folders become nested objects
- recognized text formats can be parsed and checked structurally

So instead of mixing `Files.readString(...)`, JSON parsing, YAML parsing, and assertion code, you stay inside one language.

### Inspect ZIP files

```dal
unzip: {
  'file1.txt': { string: hello }
  'file2.txt': { string: world }
}
```

Here `unzip` is not just "extract bytes". It turns the archive into structured DAL data. That is especially useful when a test needs to verify the content and shape of generated bundles, reports, or exported artifacts.

### Wait for asynchronous state

```dal
::eventually::in(5s)::every(200ms): {
  status: READY
}
```

This is one of the most practical parts of the module. In automation code, many failures are not "wrong forever" failures but "not ready yet" failures. `::eventually` lets DAL express retry logic in the same place as the assertion itself.

Use `::await` when the natural wording is "wait for this sub-expression to become available, then keep asserting from there".

### Assert HTTP responses as structured data

When this module is used together with [RESTful-cucumber](../RESTful-cucumber/README.md), the response body can stay inside ordinary DAL verification:

```dal
: {
  code= 200
  body.json= {
    code: S01
    customer: {
      name: Alice
    }
  }
}
```

This style is useful because it removes a lot of manual plumbing:

- no separate JSON parser step
- no extra "extract body, then assert fields" helper code
- one DAL assertion describes both transport-level and payload-level expectations

So an HTTP response becomes just another structured value in the same language family as objects, maps, files, and directories.

### Verify a ZIP response body in one DAL assertion

The same idea works for binary payloads. If an API returns a ZIP file, DAL can unpack and verify it structurally:

```dal
: {
  code= 200
  body.unzip: {
    'orders/1.json'.json: {
      id: 1
      code: SN1
    }
    'orders/2.json'.json: {
      id: 2
      code: SN2
    }
  }
}
```

The value here is not only brevity. It also keeps the verification at the level people actually care about:

- the response succeeded
- the response body is a ZIP archive
- the archive contains the expected files
- each JSON file has the expected structure and values

Without this style, the same check usually expands into byte handling, archive traversal, file extraction, JSON parsing, and several layers of assertion code.

## When this module earns its keep

You will usually want `DAL-extension-basic` whenever your assertions need to leave plain in-memory objects and touch the things automation code really works with:

- request or response payloads
- config files
- exported ZIP files
- directories full of generated artifacts
- eventually consistent state

That is why this module is often one of the first DAL companions added to a project.

## Repository examples

For more real examples inside the repository, start here:

- [`src/test/resources/features/text/json.feature`](src/test/resources/features/text/json.feature)
- [`src/test/resources/features/text/yaml.feature`](src/test/resources/features/text/yaml.feature)
- [`src/test/resources/features/dir-files/file.feature`](src/test/resources/features/dir-files/file.feature)
- [`src/test/resources/features/dir-files/path.feature`](src/test/resources/features/dir-files/path.feature)
- [`src/test/resources/features/dir-files/zip.feature`](src/test/resources/features/dir-files/zip.feature)
- [`src/test/resources/features/async/async.feature`](src/test/resources/features/async/async.feature)

## Related projects

- [DAL-java](../DAL-java/README.md): core syntax and runtime.
- [DAL-extension-jdbc](../DAL-extension-jdbc/README.md): database-oriented DAL access.
- [page-flow](../page-flow/README.md): applies the same DAL style to UI state and UI operations.
