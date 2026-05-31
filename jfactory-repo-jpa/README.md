# jfactory-repo-jpa

[简体中文](README.zh-CN.md)

`jfactory-repo-jpa` provides a JPA-backed `DataRepository` for [jfactory](../jfactory/README.md). It lets JFactory-created entities be persisted and queried through a standard repository abstraction, which is especially useful when test setup needs to touch a real persistence layer.

The point of this module is not to replace your application's repository layer. The point is to give JFactory a persistence target that speaks in the same terms as your entity model.

## Add it to a project

```groovy
testImplementation "org.test-charm:jfactory-repo-jpa:<version>"
```

## Quick start

```java
EntityManager entityManager = entityManagerFactory.createEntityManager();
JFactory jFactory = new JFactory(new JPADataRepository(entityManager));

Order order = jFactory.create(Order.class);
Collection<Order> orders = jFactory.type(Order.class).queryAll();
```

This short example already shows the full loop:

- JFactory creates entity data
- the repository persists it through JPA
- the same repository can later return entities for JFactory queries

That is what makes it useful in integration tests: setup and lookup can stay inside the same JFactory flow.

## What the repository does

`JPADataRepository` implements the small repository contract JFactory needs:

- `save(object)`: persists an entity inside its own transaction
- `queryAll(type)`: loads all rows of an entity type through Criteria API
- `clear()`: clears the `EntityManager` persistence context

This is intentionally small, but enough for the JFactory flow of "create data, save it, then query it back by type/spec".

## Important behavior

### `clear()` does not delete database rows

`clear()` only clears the JPA first-level cache. It does **not** truncate tables or delete rows.

This is a very important operational detail. If a scenario needs a clean database, cleanup remains the responsibility of the surrounding test infrastructure.

### Embeddables are not persisted as top-level entities

Embeddable types are skipped by repository save/query operations. That matches how JPA treats them: they are part of an entity graph, not a standalone repository root.

### Ignore-save types are supported

You can pass a list of classes that should not be saved or queried through this repository:

```java
new JPADataRepository(entityManager, List.of(IgnoreSaving.class))
```

This is useful when part of an object graph should stay in-memory only while the rest is persisted.

## When to use it

Use this module when:

- JFactory is your test-data creator
- your test exercises JPA-backed persistence
- you want repository-backed creation and querying without writing custom persistence glue

It is often the best default companion for domain-oriented persistence tests, especially when you want to stay at the entity/spec layer instead of dropping immediately to table-level JDBC assertions.

## Repository examples

The main implementation-oriented example is:

- [`src/test/java/org/testcharm/jfactory/repo/JPADataRepositoryTest.java`](src/test/java/org/testcharm/jfactory/repo/JPADataRepositoryTest.java)

## Related projects

- [jfactory](../jfactory/README.md): creates the objects that this repository persists and re-queries.
- [DAL-extension-jfactory](../DAL-extension-jfactory/README.md): query repository-backed JFactory data through DAL.
- [DAL-extension-jdbc](../DAL-extension-jdbc/README.md): use DAL directly against relational tables when the table shape itself is what you need to verify.
