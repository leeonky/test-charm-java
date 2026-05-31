# jfactory-cucumber

[简体中文](README.zh-CN.md)

`jfactory-cucumber` is the Cucumber glue layer for [jfactory](../jfactory/README.md). Most teams adopt this module because they want to **reuse ready-made steps**, not because they want another Java API surface. That is the right way to think about it.

Its job is to let feature files describe test data in business terms while JFactory handles the actual object creation, association setup, repository persistence, and later querying.

The central type is `JData`, and all built-in steps are thin, reusable wrappers around it.

## Add it to a project

```groovy
testImplementation "org.test-charm:jfactory-cucumber:<version>"
```

## Before you use it

There are a few prerequisites that matter a lot in practice.

### 1. Your specs must already be organized

This module does not invent the data model for you. It assumes you already have:

- JFactory spec classes
- meaningful spec names
- traits for named business variants
- a repository strategy suitable for your test style

If those pieces are messy, the feature files will also become messy.

### 2. `JFactory` must be injected into the Cucumber runtime

`JData` is constructed from a `JFactory` instance:

```java
JData jData = new JData(jFactory);
```

So in a real project, the step runtime must provide a shared `JFactory` through your Cucumber dependency-injection mechanism.

A typical Spring Boot setup looks like this:

```java
@Configuration
public class FactoryConfiguration {
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public JFactory createJFactory() {
        return new JFactory(new JPADataRepository(entityManagerFactory.createEntityManager()))
            .register(Products.Product.class)
            .register(Orders.Order.class);
    }
}
```

This example matters for two reasons:

- it shows that the step layer depends on **one configured JFactory**, not ad hoc local instances
- it shows where spec registration usually happens

### 3. Repository cleanup is still your responsibility

If your repository writes to a database or another persistent store, you still need per-scenario cleanup. `jfactory-cucumber` makes data preparation concise, but it does not replace your environment isolation strategy.

## How to read the step syntax

Three expression forms appear repeatedly:

- `traitsSpec`: a trait/spec expression such as `WrongPassword LoginRequest`
- `queryExpression`: a query such as `Order.code[S01]`
- `beanProperty`: a query plus a property path such as `Order.code[S01].items`

These short strings are what keep the steps compact while still targeting rich object graphs.

## The built-in step families

### 1. Create data with explicit values

```gherkin
Given Exists data "Product":
  | name | color |
  | book | red   |
```

What this does:

- selects the `Product` spec
- creates one object per row
- maps each column onto JFactory properties
- saves the objects through JFactory's repository

This is usually the best starting point for newcomers because it looks close to ordinary business data.

### 2. Create hierarchical data in one table

```gherkin
Given Exists data "Cart":
  | customer | products[0](Product).name | products[0].stocks[0](Inventory).size | products[0].stocks[0].count |
  | Tom      | book                      | A4                                    | 100                         |
```

This example is worth studying carefully because it shows the style the module is aiming for:

- the root object is `Cart`
- nested child objects are created inline
- spec names such as `(Product)` and `(Inventory)` keep nested creation explicit
- one table row can express a small object graph

That is the main productivity gain of the module: the feature file stays declarative even when the data shape is not flat.

### 3. Create data with defaults

```gherkin
Given Exists 1 data "Red Product"
```

This asks JFactory to create one object from the spec/trait combination using defaults only.

Use this form when the data shape is already well encoded in the spec and the scenario does not gain anything from repeating property values inline.

### 4. Create data from a DAL-style document

```gherkin
Given Exists data "Product":
  """
  name: 'book'.toUpperCase
  color: red
  """
```

This form matters because it lets feature files stay in the same data language family used elsewhere in Test Charm. It is especially useful when:

- tabular shape is awkward
- values are nested
- computed values read more naturally in DAL form

### 5. Attach data to an existing object

```gherkin
And exists "Cart.customer[Tom].products" as data "Product":
  | name    |
  | bicycle |
```

The intent here is: locate an existing `Cart`, go to its `products` property, then attach new `Product` data there.

This step is important because acceptance tests often need to grow an existing object graph instead of creating the whole graph in one shot.

### 6. Create related data through a reverse association

```gherkin
And exists following data "Inventory", and its "product" is "Product.name[book]":
  | size | count |
  | A3   | 10    |
```

This is the inverse style:

- first find the parent object
- then create child data whose reverse association points back to that parent

Use this when the natural ownership direction in the code is the reverse of the direction that is easiest to read in the scenario.

### 7. Verify one object

```gherkin
Then Data "Product.name[book]" should be:
  """
  .name='book'
  and .color='red'
  """
```

This queries one object and verifies it with DAL.

### 8. Verify a collection

```gherkin
Then All data "Product" should be:
  """
  .size=2
  and [0].name='book'
  and [1].name='bicycle'
  """
```

This queries all matching objects and verifies the result as a collection.

### 9. Verify the whole JFactory data world

```gherkin
Then should be:
  """
  : {
    Product: [{ name: book }]
  }
  """
```

This form is useful when the scenario wants to assert several spec groups together from the JFactory repository view.

## A fuller end-to-end example

```gherkin
Scenario: create and verify cart data
  Given Exists data "Cart":
    | customer | products[0](Product).name |
    | Tom      | book                      |
  And Exists "Cart.customer[Tom].products" as data "Product":
    | name    |
    | bicycle |
  Then Data "Cart.customer[Tom]" should be:
    """
    .customer='Tom'
    and .products.size=2
    and .products[0].name='book'
    and .products[1].name='bicycle'
    """
```

Why this example is representative:

- it creates root data
- it grows the graph later
- it queries through a business-meaningful expression
- it verifies the result in DAL

That is the normal flow this module is trying to optimize.

## English and Chinese steps

The module ships with both English and Chinese step definitions. Teams usually pick one language per suite and stay consistent; the library supports both so that the step vocabulary can match the team's feature-writing style.

## Repository examples

Good starting points in the repository are:

- [`src/test/resources/features/en/prepare-table.feature`](src/test/resources/features/en/prepare-table.feature)
- [`src/test/resources/features/en/assert.feature`](src/test/resources/features/en/assert.feature)
- [`src/test/resources/features/cn/prepare-dal.feature`](src/test/resources/features/cn/prepare-dal.feature)
- [`src/test/resources/features/cn/prepare-table.feature`](src/test/resources/features/cn/prepare-table.feature)

## Related projects

- [jfactory](../jfactory/README.md): the underlying data creation engine and spec system.
- [DAL-java](../DAL-java/README.md): the assertion language used in verification steps.
- [RESTful-cucumber](../RESTful-cucumber/README.md): often used together when scenarios need both API calls and JFactory-backed data setup.
