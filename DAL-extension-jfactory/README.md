# DAL-extension-jfactory

[简体中文](README.zh-CN.md)

`DAL-extension-jfactory` bridges [DAL-java](../DAL-java/README.md) and [jfactory](../jfactory/README.md). It exposes repository-backed JFactory data as DAL properties keyed by spec name, so repository queries participate in the same assertion language as everything else.

The module is small, but strategically important: it closes the loop between "create data with JFactory" and "verify data with DAL".

## Add it to a project

```groovy
testImplementation "org.test-charm:DAL-extension-jfactory:<version>"
```

## Before using it

Two prerequisites matter:

1. the relevant specs must be registered in `JFactory`
2. the `JFactory` instance must have a repository that can actually return data

Without those two pieces, DAL will have no meaningful spec-backed collections to expose.

## What it does

Once the extension is active, a `JFactory` instance behaves like a DAL object whose properties are registered spec names.

If `Orders` is a registered spec, you can write:

```dal
: {
  Orders: [{
    id= 1
    code= SN1
  }]
}
```

Under the hood, the extension runs the equivalent of `jFactory.spec("Orders").queryAll()` and exposes the result as a DAL list.

That is why this module feels so natural in practice: it does not invent a second query language. It simply lets DAL see JFactory repository data as ordinary structured data.

## Why this is useful

This module makes JFactory repository data feel native in DAL:

- query by spec name instead of imperative repository code
- dump repository state with ordinary DAL tooling
- use list assertions, tables, and structural matching on repository data
- combine naturally with `::eventually` when repository state changes asynchronously

Because the result is exposed as an adaptive list, async assertions work naturally:

```dal
Orders::eventually: {
  id= 1
  code= SN1
}
```

An adaptive list is DAL's "list that still behaves usefully while the real result is being adapted or re-read" abstraction. In practice, that means:

- DAL can treat it like a collection when you want list-style assertions
- DAL can treat it like a single object when there is exactly one matching element
- meta operations such as `::eventually` can re-check the list naturally

That is why both `Orders: [{...}]` and the shorter `Orders: {...}` style work, and why asynchronous repository assertions stay concise.

That makes the module especially helpful in integration-style tests where data appears after asynchronous processing or background jobs.

## Typical setup

```java
JFactory jFactory = new JFactory(repository);
jFactory.register(Orders.class);

expect(jFactory).should("""
  : {
    Orders: [{
      code= SN1
    }]
  }
""");
```

What this example is doing:

- register the spec so JFactory can resolve `Orders`
- make repository data available through that spec
- let DAL treat the spec name like a property on the `JFactory` root object

This is the core workflow of the module.

## Best fit

Use this module when:

- JFactory is already your data creation and repository abstraction
- DAL is already your assertion language
- you want repository verification to look like every other data verification in the stack

If the repository-backed spec view is already the language your suite uses, this module removes a lot of glue code.

## Repository examples

The main repository example is:

- [`src/test/resources/features/query-data.feature`](src/test/resources/features/query-data.feature)

## Related projects

- [jfactory](../jfactory/README.md): source of specs, traits, and repository access.
- [DAL-java](../DAL-java/README.md): core syntax and assertion model.
- [jfactory-repo-jpa](../jfactory-repo-jpa/README.md): a common repository implementation used with this extension.
