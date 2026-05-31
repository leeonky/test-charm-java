# page-flow

[简体中文](README.zh-CN.md)

`page-flow` is the UI-facing extension of the Test Charm stack. It treats UI interactions as data operations, then reuses [DAL-java](../DAL-java/README.md) and [jfactory](../jfactory/README.md) to keep locating, operating, filling, and asserting on pages compact and readable.

This module is still evolving, but the current abstractions already show its direction clearly: express UI state and UI actions through the same data-first language used elsewhere in the stack.

## Add it to a project

```groovy
testImplementation "org.test-charm:page-flow:<version>"
```

Use a driver-specific implementation such as the Selenium or Playwright companion module for real browser automation.

## Core abstractions

### `PageFlow`

The root environment. It provides:

- a `DAL` instance for reading and assertions
- a `JFactory` instance for structured input data
- a shared object map for contextual objects

Think of `PageFlow` as the place where UI automation joins the rest of the Test Charm data world.

### `Element`

Represents one UI element and exposes:

- locating (`css`, `xpath`, `caption`, `placeholder`)
- actions (`click`, `typeIn`, `clear`, `fillIn`)
- state (`text`, `tag`, `enabled`, `visible`, `dom`, `screenshot`)
- DAL-based evaluation and assertions

This is important because Page Flow does not want assertions to jump back into imperative driver code unless that is genuinely necessary.

### `Panel`

Represents a higher-level region built around an element. Panels add a more domain-shaped place to define page operations without leaving the DAL style.

That means you can gradually move from "raw element interaction" to "business-shaped page regions" without switching mental models.

## What usage looks like

### Locate and assert

```dal
css: {
  '.target-str': str
  '.target-int': 100
}
```

What this expression does, step by step:

- `css` says "from the current page root, use CSS lookup"
- `'.target-str': str` finds `.target-str`, reads its value as DAL data, and verifies it against `str`
- `'.target-int': 100` does the same for `.target-int`

Roughly equivalent Java without Page Flow would look like:

```java
WebElement targetStr = driver.findElement(By.cssSelector(".target-str"));
assertEquals("str", targetStr.getText());

WebElement targetInt = driver.findElement(By.cssSelector(".target-int"));
assertEquals("100", targetInt.getText());
```

The point is not only fewer lines. The point is that locating and asserting are described as one structural statement, so the test reads like a data expectation instead of a sequence of driver calls.

### Operate on the page

```dal
css[.target].click
```

What is happening here:

- `css[.target]` locates exactly one element matching `.target`
- `.click` performs the click operation on that element

Roughly equivalent Java is:

```java
WebElement target = driver.findElement(By.cssSelector(".target"));
target.click();
```

Again, the goal is not only to make the code shorter. The goal is to keep UI interaction inside the same "data navigation + operation" model used elsewhere in the stack.

### Wait for the UI to settle

```dal
patience[1s].css[.target].text = hello
```

This expression combines waiting, locating, reading, and asserting:

- `patience[1s]` gives the lookup one second to settle
- `css[.target]` keeps retrying the lookup in that patience window
- `.text` reads the element text
- `= hello` verifies the final value

Roughly equivalent Java is:

```java
WebElement target = new WebDriverWait(driver, Duration.ofSeconds(1))
    .until(d -> d.findElement(By.cssSelector(".target")));
assertEquals("hello", target.getText());
```

In UI automation, many failures are timing failures. This style keeps the waiting logic next to the assertion instead of scattering it across explicit polling code and helper methods.

### Fill a form from structured data

```dal
css: {
  'input[name=username]'.fillIn: alice
  'input[name=password]'.fillIn: secret
  'button[type=submit]'.click: {...}
}
```

What this block is doing:

- locate the username field and fill it with `alice`
- locate the password field and fill it with `secret`
- locate the submit button and click it

Roughly equivalent Java is:

```java
driver.findElement(By.cssSelector("input[name=username]")).clear();
driver.findElement(By.cssSelector("input[name=username]")).sendKeys("alice");

driver.findElement(By.cssSelector("input[name=password]")).clear();
driver.findElement(By.cssSelector("input[name=password]")).sendKeys("secret");

driver.findElement(By.cssSelector("button[type=submit]")).click();
```

Because Page Flow reuses JFactory-style collectors, this can also grow into object-shaped form filling instead of staying at one-field-at-a-time driver code.

This is a big part of the design philosophy: a form is still a structured data surface, so it should be possible to feed it structured data directly.

## Why it is different from a thin wrapper

The goal is not only to hide Selenium or Playwright APIs. The goal is to let UI work participate in the same language as the rest of your test data:

- locate elements through DAL expressions
- assert UI state with DAL structure matching
- build input values with JFactory
- keep UI automation closer to domain data and farther from low-level scripting

That is the real promise of the module.

## Repository examples

Useful repository examples live here:

- [`src/test/resources/features/locate/find-one.feature`](src/test/resources/features/locate/find-one.feature)
- [`src/test/resources/features/locate/find-all.feature`](src/test/resources/features/locate/find-all.feature)
- [`src/test/resources/features/operation.feature`](src/test/resources/features/operation.feature)
- [`src/test/resources/features/form.feature`](src/test/resources/features/form.feature)

## Related projects

- [DAL-java](../DAL-java/README.md): powers locating, inspection, and assertions.
- [DAL-extension-basic](../DAL-extension-basic/README.md): contributes async and file-oriented helpers often useful around UI work.
- `page-flow-selenium` and `page-flow-playwright`: concrete browser backends.
