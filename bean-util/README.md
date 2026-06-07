# bean-util

[简体中文](README.zh-CN.md)

`bean-util` is the low-level reflection and property-access library shared by [DAL-java](../DAL-java/README.md) and [jfactory](../jfactory/README.md). It is not usually the first module a user adopts, but it explains an important part of the Test Charm stack: data can be read and populated with fewer assumptions than in many stricter bean helper libraries.

This README intentionally stays compact. `bean-util` is a supporting library, not the main surface of the stack, so the goal here is to clarify its conventions and the places where it behaves differently from more familiar Java bean tools.

## Add it to a project

```groovy
testImplementation "org.test-charm:bean-util:<version>"
```

## What it provides

- `BeanClass<T>`: wraps a Java type and exposes constructors, generic metadata, property readers, property writers, and type checks.
- `PropertyReader` / `PropertyWriter`: low-level adapters for reading and writing one property at a time.
- `Classes`: classpath scanning and assignable-type lookup helpers used by other Test Charm modules.

If you are using DAL or JFactory, you are already relying on these behaviors indirectly.

## Bean access rules

The most important thing to understand is **what counts as a property**.

### Read support

`bean-util` reads:

- public fields
- standard JavaBean getters such as `getName()` and `isEnabled()`

It does **not** treat every public no-argument method as a property. That broader method-style access belongs to DAL's higher-level expression model, not to `bean-util` itself.

### Write support

`bean-util` writes through:

- public fields
- setters such as `setName(...)`

This also works for collection and array slots when the target shape is writable.

## Direct property API vs. dotted paths

`BeanClass#getPropertyValue(...)` and `BeanClass#setPropertyValue(...)` operate on **one property name at a time**. They are not the same thing as DAL-style dotted navigation.

That distinction matters because users often meet `bean-util` through higher-level libraries:

- DAL builds a navigation language on top of property readers.
- JFactory builds nested object creation on top of property readers and writers.

Those higher layers make dotted paths feel natural, but `bean-util` itself is intentionally lower level.

## Minimal example

```java
public class Customer {
    public String name;
}

public class Order {
    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}

BeanClass<Order> orderType = BeanClass.create(Order.class);
BeanClass<Customer> customerType = BeanClass.create(Customer.class);

Order order = orderType.newInstance();
Customer customer = customerType.newInstance();

customerType.setPropertyValue(customer, "name", "Alice");
orderType.setPropertyValue(order, "customer", customer);

String name = (String) customerType.getPropertyValue(order.getCustomer(), "name");
```

The example is deliberately direct:

1. `BeanClass` is created per type.
2. Each `setPropertyValue(...)` call writes one property on one object.
3. Nested navigation is done by traversing objects explicitly, not by passing `"customer.name"` into the direct getter/setter API.

That is the level `bean-util` works at. If you want `"customer.name"` style authoring, that is normally the job of JFactory or DAL.

## Why this library feels looser than many bean helpers

Even though the property API is direct and explicit, the underlying type handling is intentionally permissive:

- generic type information is preserved where possible
- collections and arrays are treated as structured data, not opaque containers
- interfaces, proxies, and generated shapes can still participate when they expose readable or writable bean-style properties

That is why the higher-level modules can create and verify object graphs with relatively little boilerplate.

## When end users usually need to care

Most users only look at `bean-util` directly when one of these questions comes up:

- Why can JFactory populate this object graph?
- Why does DAL see this field but not that method?
- How does type discovery or package scanning work for specs and extensions?

If those questions do not come up, you can usually treat `bean-util` as infrastructure.

## Repository examples

If you want to see the exact behavior in the repository, these files are the most useful starting points:

- [`src/test/java/org/testcharm/util/property/BeanClassTest.java`](src/test/java/org/testcharm/util/property/BeanClassTest.java)
- [`src/test/java/org/testcharm/util/property/PropertyReaderTest.java`](src/test/java/org/testcharm/util/property/PropertyReaderTest.java)
- [`src/test/java/org/testcharm/util/property/PropertyWriterTest.java`](src/test/java/org/testcharm/util/property/PropertyWriterTest.java)
- [`src/test/java/org/testcharm/util/PropertyChainTest.java`](src/test/java/org/testcharm/util/PropertyChainTest.java)

## Related projects

- [DAL-java](../DAL-java/README.md): builds a full data language on top of these bean rules.
- [jfactory](../jfactory/README.md): uses the same property model to create and populate data.

## Third-party notices

`bean-util` includes `org.json` (JSON-java) via shading and relocation (`org.testcharm.shaded.org.json`).
The original copyright notice and full license text are bundled in:

- [`src/main/resources/META-INF/NOTICE`](src/main/resources/META-INF/NOTICE)
- [`src/main/resources/META-INF/LICENSE-THIRD-PARTY`](src/main/resources/META-INF/LICENSE-THIRD-PARTY)
