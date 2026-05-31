# DAL-extension-jdbc

[简体中文](README.zh-CN.md)

`DAL-extension-jdbc` turns relational tables into DAL data. Once connected, tables become fields, rows become objects, and relationships can be traversed with the same structural syntax you already use for ordinary Java data.

That said, this module is best treated as a **table-oriented integration tool**, not automatically the first persistence layer tool to reach for.

## Add it to a project

```groovy
testImplementation "org.test-charm:DAL-extension-jdbc:<version>"
```

## Prefer a domain model first when you have one

For application-level testing, the recommended path is usually:

1. model the data with proper domain relationships
2. persist it through JPA or your repository layer
3. prepare and query it through [jfactory-repo-jpa](../jfactory-repo-jpa/README.md) and [DAL-extension-jfactory](../DAL-extension-jfactory/README.md)
4. assert through Java objects or spec-backed DAL data

That path keeps tests aligned with the same object relationships the application itself uses.

Use `DAL-extension-jdbc` when you specifically need the **table view** of the world, for example:

- validating a legacy schema that does not map cleanly to a domain model
- checking raw persistence details such as join tables or denormalized reporting tables
- asserting migration results or ETL output
- working in projects where JDBC access is the reliable integration point

In short: if a clean domain model and repository abstraction already exist, start there first. Reach for `DAL-extension-jdbc` when the test really needs to speak in tables.

## Quick start

```java
Connection connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
DataBaseBuilder builder = new DataBaseBuilder();

expect(builder.connect(connection)).should("""
  : {
    products: | id | name   |
              | p1 | iPhone |
              | p2 | MBP    |
  }
""");
```

This is the core idea of the module in one example:

- the database connection is wrapped as the DAL root
- a table such as `products` becomes a DAL property
- rows can be matched with tables, objects, and nested relationship traversal

That makes database verification look much closer to ordinary data verification than to imperative SQL assertion code.

## The data model

After `connect(...)`:

- the database is the DAL root object
- each configured table is exposed as a property such as `products` or `orders`
- each row behaves like an object whose columns are DAL properties

So these expressions read naturally:

```dal
products::size = 2

products: [{
  id= 10L
  name= MBP
}]
```

The first checks cardinality. The second checks row content structurally. Both stay within the same DAL language you already use for objects, collections, and payloads elsewhere.

## Filtering and projection

Two especially useful clauses are:

- `::where[...]`
- `::select[...]`

Example:

```dal
: {
  products::where[name='MBP']: [{ name= MBP }]
  products::select[name as n].n[]: [MBP iPod]
}
```

Why these matter:

- `::where[...]` keeps small database filters close to the assertion instead of forcing a separate SQL string
- `::select[...]` lets you project exactly the columns or derived values you want to compare

This is one of the places where the module feels most productive: a database check can stay short without becoming unreadable.

## Associations

The extension supports common relational traversals:

- `::belongsTo[...]`
- `::hasOne[...]`
- `::hasMany[...]`
- many-to-many relationships

Example:

```dal
orders: [{
  customer= Tom
  ::hasMany[order_lines]: | id | quantity | ::belongsTo[products].name |
                          | 1  | 1        | iPhone                     |
                          | 2  | 100      | MBP                        |
}]
```

This reads the database in business terms instead of raw foreign-key plumbing:

- start from one order row
- follow its order lines
- from each order line, follow the product row
- verify the joined result as one nested structure

That is the best way to think about the module: not "SQL inside DAL", but "table relationships expressed as data navigation".

## Row methods

If you want the DAL side to use more domain-shaped names, register row methods on table strategies:

```java
DataBaseBuilder builder = new DataBaseBuilder();

builder.tableStrategy("orders").registerRowMethod("orderLines",
    row -> row.hasMany("order_lines"));

builder.tableStrategy("order_lines").registerRowMethod("product",
    row -> row.belongsTo("products"));
```

Then the assertion becomes:

```dal
orders: [{
  orderLines: | id | quantity | product.name |
              | 1  | 1        | iPhone       |
              | 2  | 100      | MBP          |
}]
```

This is usually worth doing when the raw table names are technical, but the test wants to stay readable in domain vocabulary.

## When to use it

`DAL-extension-jdbc` is a good fit when you want database assertions to stay:

- structural rather than stringly typed
- close to the actual persisted shape
- readable without writing lots of SQL and result-set traversal code

But if your test already has a strong domain model, a JPA repository, and JFactory specs, keep that higher-level model as the default and use JDBC only for the places where the relational shape itself is the thing under test.

## Repository examples

Useful repository examples live here:

- [`src/test/resources/features/single-table.feature`](src/test/resources/features/single-table.feature)
- [`src/test/resources/features/belongs-to.feature`](src/test/resources/features/belongs-to.feature)
- [`src/test/resources/features/has-one.feature`](src/test/resources/features/has-one.feature)
- [`src/test/resources/features/has-many.feature`](src/test/resources/features/has-many.feature)
- [`src/test/resources/features/many-to-many.feature`](src/test/resources/features/many-to-many.feature)

## Related projects

- [DAL-java](../DAL-java/README.md): core syntax and structural assertions.
- [DAL-extension-jfactory](../DAL-extension-jfactory/README.md): query repository-backed JFactory data with DAL.
- [jfactory-repo-jpa](../jfactory-repo-jpa/README.md): the higher-level persistence path that is often the better default for domain-oriented tests.
