# jfactory

[简体中文](README.zh-CN.md)

`jfactory` is one of the two core libraries in Test Charm, alongside [DAL-java](../DAL-java/README.md). It is a data-creation library built around reusable specs, traits, repository-aware querying, and concise data descriptions.

The key idea is not "write constructors faster". The key idea is:

- define the default shape of useful domain data once
- override only the parts a scenario cares about
- reuse existing repository data when appropriate
- keep creation and verification equally data-oriented

That is why JFactory feels different from many factory or fixture libraries: it is not only an object generator, but also a bridge between **data design**, **data storage**, and **data reuse**.

## Add it to a project

```groovy
testImplementation "org.test-charm:jfactory:<version>"
```

## First mental model

There are four concepts worth keeping in your head from the beginning:

1. **Type builder**: `jFactory.type(Order.class)` creates data directly from a Java type.
2. **Spec**: `Spec<T>` names and reuses a data shape.
3. **Trait**: a named variation layered on top of a spec.
4. **Repository**: created data can be saved and queried, so later objects can reuse earlier data instead of blindly creating duplicates.

Once those four ideas click, most of the library starts to feel natural.

## Quick start

```java
JFactory jFactory = new JFactory();

Order order = jFactory.create(Order.class);
Order customized = jFactory.type(Order.class)
    .property("customer.name", "Alice")
    .property("total", 100)
    .create();
```

Why this is useful:

- `create(Order.class)` gives you a usable object immediately, with generated defaults
- `.property(...)` overrides only the data that matters here
- nested paths such as `customer.name` keep setup compact

So the "quick start" is already showing the intended style: do not build everything manually, and do not repeat defaults in every test.

## `type(...)`: the direct builder

Use `type(...)` when you want to describe data directly from a Java type:

```java
jFactory.type(Order.class)
    .property("customer.name", "Alice")
    .property("items[0].name", "Pencil")
    .property("items[0].quantity", 2)
    .create();
```

This is the shortest path for one-off setup.

The important thing to notice is not only the syntax, but the intention:

- nested object creation happens for you
- collections can be addressed structurally
- the test stays focused on meaningful data rather than constructor choreography

## `Spec<T>`: reusable data design

When a shape of data deserves a name, define a `Spec<T>`:

```java
public class PaidOrder extends Spec<Order> {
    @Override
    public void main() {
        property("status").value("PAID");
        property("total").value(100);
    }
}
```

Then create it with:

```java
Order order = jFactory.spec(PaidOrder.class).create();
```

Specs are the backbone of readable JFactory usage. They let the code say "create a paid order" instead of spelling out every field of a paid order every time.

## Traits: named variations

Traits are named adjustments layered on top of a spec:

```java
public class UserSpec extends Spec<User> {
    @Override
    public void main() {
        property("enabled").value(true);
    }

    @Trait
    public UserSpec admin() {
        property("role").value("ADMIN");
        return this;
    }
}
```

Then:

```java
User user = jFactory.spec(UserSpec.class).traits("admin").create();
```

This is one of the reasons JFactory reads well in large test suites: traits let the scenario name the kind of data it wants instead of spelling every value inline.

## DAL-powered creation

In real Test Charm usage, JFactory is often driven by DAL-style input instead of only Java builder calls:

```java
jFactory.useDAL().create("PaidOrder", """
  {
    customer: {
      name: Alice
    }
    items: [{
      name: Pencil
      quantity: 2
    }]
  }
""");
```

This matters because it keeps creation and verification in the same general language family:

- JFactory creates data from compact data descriptions
- DAL verifies data with compact data descriptions

That shared style is a major part of the Test Charm approach.

## DataRepository: far more than "where created objects are stored"

This is one of the most important parts of JFactory, and it is also one of the places where the library differs from many similar tools.

`JFactory` always works with a `DataRepository`:

- the default constructor uses an in-memory repository
- you can plug in your own repository implementation

```java
JFactory jFactory = new JFactory(repository);
```

### `save(object)`

Whenever JFactory creates an object, the repository can persist it through `save(object)`.

That persistence target is entirely up to the repository implementation. It can be:

- a database
- a file-backed store
- a cache
- a mock server side store
- an in-memory list for tests

That means JFactory is not limited to "temporary objects in one JVM". It can create data into the place where the rest of the test actually expects to find it.

### `queryAll(type)`

`queryAll(type)` returns all objects of a given type from the repository. JFactory then applies property criteria **in memory** on top of that result.

That design is deliberate. It keeps the repository contract small and easy to implement:

- the repository only needs to know how to load all objects of a type
- JFactory keeps the richer matching logic for property filters and nested criteria

This is why a repository implementation can stay lightweight even while JFactory offers expressive query behavior.

### `query()`

When you call `query()`, JFactory expects a single match:

- one match -> return it
- no match -> return `null`
- multiple matches -> fail fast

That strictness is important because it prevents ambiguous reuse of existing data.

## Nested property creation can reuse existing related data

This is a major differentiator of JFactory.

A nested child object does not always need to be freshly created. A very common case is:

1. you are creating an `Order`
2. you know the identifying property of `Order.customer`, such as `email`
3. JFactory should first look for an existing `Customer`
4. only if it cannot find one should it create a new `Customer`

JFactory supports that pattern directly even from the ordinary nested property API.

### Scenario 1: no matching related object exists yet

```java
public class Customer {
    public String email;
}

public class Order {
    public Customer customer;
}

JFactory jFactory = new JFactory();

Order order = jFactory.type(Order.class)
    .property("customer.email", "new@example.com")
    .create();
```

What happens here:

- JFactory sees that `Order.customer` must exist in order to set `customer.email`
- it looks in the repository for a matching `Customer`
- there is no match yet
- it creates a new `Customer`
- the resulting `order.customer.email` is `new@example.com`

So even though you only set one nested property, JFactory can materialize the needed object graph for you.

### Scenario 2: a matching related object already exists

```java
JFactory jFactory = new JFactory();

Customer existing = jFactory.type(Customer.class)
    .property("email", "exist@example.com")
    .create();

Order order = jFactory.type(Order.class)
    .property("customer.email", "exist@example.com")
    .create();
```

What this example is showing:

- the final `order.customer.email` is still exactly the value you asked for
- but JFactory first tries to satisfy that nested relation from repository data
- because a matching `Customer` already exists, JFactory reuses it instead of creating another one

This behavior matters because it lets one short property path express a very practical rule:

- "I need an order"
- "its customer must have this identifying value"
- "reuse an existing customer if possible; otherwise create one"

Why this is important in practice:

- it keeps related data consistent across a scenario
- it avoids accidental duplication
- it lets object graphs connect naturally to earlier created repository data
- it makes JFactory useful for integration tests, not only isolated unit-style object generation

This "query first, then create if absent" behavior is one of the most valuable features in the library.

## How to organize specs in practice

A practical pattern that works well is:

- one spec class per meaningful domain data shape
- traits for named business variations
- use nested identifying properties such as `customer.email` when a relation should connect to previously created data if it already exists
- DAL input for high-verbosity creation scenarios where Java builder code would become too noisy

That combination keeps a suite readable as it grows.

## Repository examples

These repository files are especially useful if you want to study real usage:

- [`src/test/resources/features/4-data-repository/1-save-to-repo.feature`](src/test/resources/features/4-data-repository/1-save-to-repo.feature)
- [`src/test/resources/features/4-data-repository/2-query-from-repo.feature`](src/test/resources/features/4-data-repository/2-query-from-repo.feature)
- [`src/test/resources/features/legacy/bug/builder-value-producer.feature`](src/test/resources/features/legacy/bug/builder-value-producer.feature)
- [`src/test/resources/features/input-property/nested-object.feature`](src/test/resources/features/input-property/nested-object.feature)

## Related projects

- [DAL-java](../DAL-java/README.md): the matching data language used heavily for creation input and assertions.
- [jfactory-cucumber](../jfactory-cucumber/README.md): ready-made Cucumber glue around JFactory.
- [jfactory-repo-jpa](../jfactory-repo-jpa/README.md): JPA-backed repository implementation.
- [DAL-extension-jfactory](../DAL-extension-jfactory/README.md): query JFactory repository data from DAL.
