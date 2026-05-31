# RESTful-cucumber

[简体中文](README.zh-CN.md)

`RESTful-cucumber` packages common Cucumber steps for HTTP API testing and connects them with [DAL-java](../DAL-java/README.md) and [jfactory](../jfactory/README.md). The result is a compact workflow for sending requests, building bodies, checking responses, and reusing named test data shapes.

The module is most useful when you want API scenarios to stay declarative instead of becoming a pile of HTTP client code plus JSON assertion code.

## Add it to a project

```groovy
testImplementation "org.test-charm:RESTful-cucumber:<version>"
```

## What it gives you

At the center is `RestfulStep`, which supports:

- `GET`, `DELETE`, `POST`, `PUT`, `PATCH`
- query parameters from URLs or DAL/object-shaped doc strings
- headers as structured data
- request-body handling for JSON, plain text, octet-stream, and multipart form data
- response assertions through DAL
- optional JFactory-backed request body generation

## What setup usually looks like

In practice, projects normally configure three things around `RestfulStep`:

1. a base URL
2. optional `JFactory` integration for spec-backed request bodies
3. any custom body writers or default document type changes needed by the API under test

That is why the step layer stays concise: transport details are configured once, then scenarios mostly speak in request/response data.

## Basic request steps

```gherkin
When GET "/orders"

When GET "/orders":
  """
  {
    "customer": "Alice"
  }
  """

When POST "/orders":
  """json
  {
    "code": "S01"
  }
  """
```

These examples matter because they show the main style of the module:

- the HTTP method is visible in the step itself
- the request path stays short
- query parameters or bodies can be described as data instead of concatenated strings

That keeps the scenario focused on business input instead of low-level client setup.

## Response assertions

Responses are verified with DAL:

```gherkin
Then response should be:
  """
  : {
    status: 200
    body.json: {
      code: S01
    }
  }
  """
```

This is one of the strongest aspects of the module: request and response descriptions feel symmetrical.

Instead of parsing JSON in one helper, checking status in another helper, and comparing fields with ad hoc code, you stay in one assertion language.

## JFactory-backed request bodies

One of the most useful integrations is generating request bodies from JFactory specs and traits:

```gherkin
When POST "LoginRequest" "/login":
  """
  {
    "username": "admin"
  }
  """
```

Here the inline document overrides or supplements a spec-backed body. That is powerful because it combines:

- realistic defaults from JFactory
- small, scenario-specific overrides in the feature file

Traits work too:

```gherkin
When POST "WrongPassword LoginRequest" "/login":
  """
  {
    "username": "admin"
  }
  """
```

This keeps request payloads short without forcing you to repeat every field in every scenario.

## Multipart and files

The module can build multipart requests from structured data, including files and virtual files:

```gherkin
When POST form "/files":
  """
  : {
    key: value
    file(ATextFile): {
      name= u.txt
      content= hello-world
    }
  }
  """
```

That means even mixed form payloads can stay inside the same data description style instead of dropping into custom multipart helper code.

## Default content type behavior

`RestfulStep` supports a configurable default document type. That matters when the same doc string should be interpreted as:

- raw text
- DAL that evaluates to JSON-like data
- octet-stream bytes
- multipart form payload

This is what keeps the feature file concise without losing control over the actual HTTP payload semantics.

## Repository examples

Useful repository examples include:

- [`src/test/resources/features/module-safe/restful.feature`](src/test/resources/features/module-safe/restful.feature)
- [`src/test/resources/features/module-safe/request.feature`](src/test/resources/features/module-safe/request.feature)
- [`src/test/resources/features/module-safe/request-with-body/basic-step.feature`](src/test/resources/features/module-safe/request-with-body/basic-step.feature)
- [`src/test/resources/features/module-safe/request-with-body/spec-in-step.feature`](src/test/resources/features/module-safe/request-with-body/spec-in-step.feature)
- [`src/test/resources/features/module-safe/jfactory.feature`](src/test/resources/features/module-safe/jfactory.feature)

## Related projects

- [DAL-java](../DAL-java/README.md): request and response assertions are expressed in DAL.
- [jfactory](../jfactory/README.md): request bodies can be generated from specs and traits.
- [jfactory-cucumber](../jfactory-cucumber/README.md): complementary data setup layer for Cucumber scenarios.
