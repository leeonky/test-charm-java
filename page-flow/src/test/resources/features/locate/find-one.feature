Feature: locating

  Rule: api

    Scenario Outline: single element of all result list
      When launch the following web page:
        """
        html
          body
            div unexpected
            .target-str str
            .target-int 100
            .target-double 1.1
            .target-long 99999999999999999
        """
      Then page in driver <driver> should:
        """
        css: {
          '.target-str': str
          '.target-int': 100
          '.target-double': 1.1
          '.target-long': 99999999999999999
        }
        """
      And logs should:
        """
        : | level | message                                    |
          | INFO  | Locating... (8888ms)                       |
          | INFO  | Selector: css{html} => css{.target-str}    |
          | INFO  | Found 1 elements                           |
          | INFO  | Locating... (8888ms)                       |
          | INFO  | Selector: css{html} => css{.target-int}    |
          | INFO  | Found 1 elements                           |
          | INFO  | Locating... (8888ms)                       |
          | INFO  | Selector: css{html} => css{.target-double} |
          | INFO  | Found 1 elements                           |
          | INFO  | Locating... (8888ms)                       |
          | INFO  | Selector: css{html} => css{.target-long}   |
          | INFO  | Found 1 elements                           |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: more than one element
      Given launch the following web page:
        """
        html
          body
            .target unexpected
            .target unexpected
        """
      When try to find element via driver <driver>:
        """
        patience[1s].css[.target].text
        """
      Then failed with:
        """
        Operations can only be performed on a single located element at:
        css{html} => css{.target}
        but found 2: [
            org.testcharm.pf.cucumber.<type> {
                dom: java.lang.String <<div class="target">unexpected</div>>,
                enabled: java.lang.Boolean <true>,
                input: java.lang.Boolean <false>,
                locator: css{.target},
                visible: java.lang.Boolean <true>
            },
            org.testcharm.pf.cucumber.<type> {
                dom: java.lang.String <<div class="target">unexpected</div>>,
                enabled: java.lang.Boolean <true>,
                input: java.lang.Boolean <false>,
                locator: css{.target},
                visible: java.lang.Boolean <true>
            }
        ]
        """
      Examples:
        | driver     | type                   |
        | selenium   | Selenium$SeleniumE     |
        | playwright | Playwright$PlaywrightE |

    Scenario Outline: more than one element on verification
      Given launch the following web page:
        """
        html
          body
            .target unexpected
            .target unexpected
        """
      When try to find element via driver <driver>:
        """
        patience[1s].css[.target]: unexpected
        """
      Then failed with:
        """
        Operations can only be performed on a single located element at:
        css{html} => css{.target}
        but found 2: [
            org.testcharm.pf.cucumber.<type> {
                dom: java.lang.String <<div class="target">unexpected</div>>,
                enabled: java.lang.Boolean <true>,
                input: java.lang.Boolean <false>,
                locator: css{.target},
                visible: java.lang.Boolean <true>
            },
            org.testcharm.pf.cucumber.<type> {
                dom: java.lang.String <<div class="target">unexpected</div>>,
                enabled: java.lang.Boolean <true>,
                input: java.lang.Boolean <false>,
                locator: css{.target},
                visible: java.lang.Boolean <true>
            }
        ]
        """
      Examples:
        | driver     | type                   |
        | selenium   | Selenium$SeleniumE     |
        | playwright | Playwright$PlaywrightE |

    Scenario Outline: find element after waiting time
      Given launch the following web page:
        """
        html
          script.
            document.addEventListener('DOMContentLoaded', function() {
              setTimeout(function() {
                var newElement = document.createElement('div');
                newElement.className = 'target';
                newElement.textContent = 'hello';
                document.body.appendChild(newElement);
              }, 500);
            });
          body
        """
      Then page in driver <driver> should:
        """
        patience[1s].css[.target].text= hello
        """
      And logs should:
        """
        : | level | message                             |
          | ...                                         |
          | INFO  | Locating... (1000ms)                |
          | INFO  | Selector: css{html} => css{.target} |
          | INFO  | Found 0 elements                    |
          | INFO  | Selector: css{html} => css{.target} |
          | INFO  | Found 1 elements                    |
          | ...                                         |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: find nothing after waiting time
      Given launch the following web page:
        """
        html
          body
        """
      When try to find element via driver <driver>:
        """
        patience[1s].css[.target].text
        """
      Then failed with:
        """
        Operations can only be performed on a single located element at:
        css{html} => css{.target}
        but found 0: []
        """
      And logs should:
        """
        : | level | message                             |
          | ...                                         |
          | INFO  | Selector: css{html} => css{.target} |
          | INFO  | Found 0 elements                    |
          | INFO  | Selector: css{html} => css{.target} |
          | INFO  | Found 0 elements                    |
          | ...                                         |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: Once only element have been operated, the previously located set of elements does not change
      Given launch the following web page:
        """
        html
          script.
            document.addEventListener('DOMContentLoaded', function() {
              setTimeout(function() {
                var newElement = document.createElement('div');
                newElement.className = 'target';
                newElement.textContent = 'world';
                document.body.appendChild(newElement);
              }, 500);
            });
          body
            .target hello
        """
      When try to find element via driver <driver>:
        """
        css[.target]: {
          text= hello
          text[]= [hello world]
        }
        """
      Then failed with:
        """
        Unexpected list size
        Expected: <2>
        Actual: <1>
        Actual list: [
            java.lang.String <hello>
        ]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

  Rule: filter

    Scenario Outline: filter and find element after waiting time
      Given launch the following web page:
        """
        html
          script.
            document.addEventListener('DOMContentLoaded', function() {
              setTimeout(function() {
                var newElement = document.createElement('div');
                newElement.className = 'target';
                newElement.textContent = 'hello';
                document.body.appendChild(newElement);

                newElement = document.createElement('div');
                newElement.className = 'not-target';
                newElement.textContent = 'world';
                document.body.appendChild(newElement);
              }, 500);
            });
          body
        """
      Then page in driver <driver> should:
        """
        patience[1s].css[div]::filter: {
          attribute[class]: [target]
        }: {
          text= hello
        }
        """
      And logs should:
        """
        : | level | message                                                                 |
          | ...                                                                             |
          | INFO  | {::should.startsWith: 'Filtering by source code(@'}                     |
          | INFO  | Selector: css{html} => css{div}                                         |
          | INFO  | Found 0 elements                                                        |
          | INFO  | {::should.startsWith: 'Filtered from 0 to 0 elements by source code(@'} |
          | INFO  | Selector: css{html} => css{div}                                         |
          | INFO  | Found 2 elements                                                        |
          | INFO  | {::should.startsWith: 'Filtered from 2 to 1 elements by source code(@'} |
          | ...                                                                             |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: filter but more than one elements
      Given launch the following web page:
        """
        html
          body
            .target unexpected
            .target unexpected
        """
      When try to find element via driver <driver>:
        """
        (patience[200ms].css[.target]::filter: {text= unexpected}).text
        """
      Then failed with:
        """
        Operations can only be performed on a single located element at:
        Filtering by source code
            css{html} => css{.target}
        but found 2: [
            org.testcharm.pf.cucumber.<type> {
                dom: java.lang.String <<div class="target">unexpected</div>>,
                enabled: java.lang.Boolean <true>,
                input: java.lang.Boolean <false>,
                locator: css{.target},
                visible: java.lang.Boolean <true>
            },
            org.testcharm.pf.cucumber.<type> {
                dom: java.lang.String <<div class="target">unexpected</div>>,
                enabled: java.lang.Boolean <true>,
                input: java.lang.Boolean <false>,
                locator: css{.target},
                visible: java.lang.Boolean <true>
            }
        ]
        """
      And logs should:
        """
        : | level | message                                                               |
          | ...                                                                           |
          | INFO  | {::should.startsWith: 'Filtering by source code'}                     |
          | INFO  | Selector: css{html} => css{.target}                                   |
          | INFO  | Found 2 elements                                                      |
          | INFO  | {::should.startsWith: 'Filtered from 2 to 2 elements by source code'} |
          | ...                                                                           |
        """
      Examples:
        | driver     | type                   |
        | selenium   | Selenium$SeleniumE     |
        | playwright | Playwright$PlaywrightE |

    Scenario Outline: filter visible element
      Given launch the following web page:
        """
        html
        head
        body
          .hidden(style='display:none')
            .target hello
          .visible
            .target world
        """
      Then page in driver <driver> should:
        """
        css[.target]!: world
        """
      And logs should:
        """
        : | level | message                                                             |
          | ...                                                                         |
          | INFO  | {::should.startsWith: 'Filtering by visible(@'}                     |
          | INFO  | Selector: css{html} => css{.target}                                 |
          | INFO  | Found 2 elements                                                    |
          | INFO  | {::should.startsWith: 'Filtered from 2 to 1 elements by visible(@'} |
          | ...                                                                         |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: filter visible but more than one elements
      Given launch the following web page:
        """
        html
        head
        body
          .hidden(style='display:none')
            .target hello
            .target world
        """
      When try to find element via driver <driver>:
        """
        patience[200ms].css[.target]!: world
        """
      Then failed with:
        """
        Operations can only be performed on a single located element at:
        Filtering by visible
            css{html} => css{.target}
        but found 0: []
        """
      And logs should:
        """
        : | level | message                                                           |
          | ...                                                                       |
          | INFO  | {::should.startsWith: 'Filtering by visible'}                     |
          | INFO  | Selector: css{html} => css{.target}                               |
          | INFO  | Found 2 elements                                                  |
          | INFO  | {::should.startsWith: 'Filtered from 2 to 0 elements by visible'} |
          | ...                                                                       |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

  Rule: multiple locator

    Scenario Outline: elements + elements
      Given launch the following web page:
        """
        html
          body
            .target hello
        """
      Then page in driver <driver> should:
        """
        (css[.target] + css[.any-not-exist]).text= hello
        """
      And logs should:
        """
        : | level | message                                                             |
          | ...                                                                         |
          | INFO  | /^Group\(@.*\) locating\.\.\.$/                                     |
          | INFO  | Selector: css{html} => css{.target}                                 |
          | INFO  | Found 1 elements                                                    |
          | INFO  | Selector: css{html} => css{.any-not-exist}                          |
          | INFO  | Found 0 elements                                                    |
          | INFO  | /^Group\(@.*\) found a total of 1 elements$/                        |
          | ...                                                                         |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: use longer waiting time
      Given launch the following web page:
        """
        html
          script.
            document.addEventListener('DOMContentLoaded', function() {
              setTimeout(function() {
                var newElement = document.createElement('div');
                newElement.className = 'target';
                newElement.textContent = 'hello';
                document.body.appendChild(newElement);
              }, 500);
            });
          body
        """
      Then page in driver <driver> should:
        """
        (patience[200ms].css[.target] + patience[1s].css[.any-not-exist]).text= hello
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: use longer waiting time not plus
      Given launch the following web page:
        """
        html
          script.
            document.addEventListener('DOMContentLoaded', function() {
              setTimeout(function() {
                var newElement = document.createElement('div');
                newElement.className = 'target';
                newElement.textContent = 'hello';
                document.body.appendChild(newElement);
              }, 600);
            });
          body
        """
      When try to find element via driver <driver>:
        """
        (patience[300ms].css[.target] + patience[300ms].css[.any-not-exist]).text= hello
        """
      Then failed with:
        """
        Operations can only be performed on a single located element at:
        Group:
            css{html} => css{.target}
            css{html} => css{.any-not-exist}
        but found 0: []
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: elements + elements + elements
      Given launch the following web page:
        """
        html
          body
            .target hello
        """
      Then page in driver <driver> should:
        """
        (css[.target] + css[.any-not-exist1] + css[.any-not-exist2]).text= hello
        """
      And logs should:
        """
        : | level | message                                                             |
          | INFO  | Locating... (8888ms)                                                |
          | INFO  | /^Group\(@.*\) locating\.\.\.$/                                     |
          | INFO  | Selector: css{html} => css{.target}                                 |
          | INFO  | Found 1 elements                                                    |
          | INFO  | Selector: css{html} => css{.any-not-exist1}                         |
          | INFO  | Found 0 elements                                                    |
          | INFO  | Selector: css{html} => css{.any-not-exist2}                         |
          | INFO  | Found 0 elements                                                    |
          | INFO  | /^Group\(@.*\) found a total of 1 elements$/                        |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: invalid operator + for multiple locator
      Given launch the following web page:
        """
        html
          body
            .target hello
        """
      When try to find element via driver <driver>:
        """
        css[.target] + 1
        """
      Then failed with:
        """
        No operation `PLUS` between 'org.testcharm.pf.LocatorElements' and 'java.lang.Integer'
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |
