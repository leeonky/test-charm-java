Feature: web ui

  Rule: find element deprecated

    Scenario Outline: find all element by css_
      When launch the following web page:
        """
        html
          body
            div unexpected
            .target expected1
            .target expected2
        """
      Then page in driver <driver> should:
        """
        findAll.css[.target].text[]= [expected1 expected2]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: find all element by simple text_
      When launch the following web page:
        """
        html
          body
            div unexpected
            label expected
            span expected
        """
      Then page in driver <driver> should:
        """
        findAll.text[expected].tag[]= [label span]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: find all element by xpath
      When launch the following web page:
        """
        html
          body
            div unexpected
            div(attr='a') expected1
            div(attr='a') expected2
        """
      Then page in driver <driver> should:
        """
        findAll.xpath["//div[@attr='a']"].text[]= [expected1 expected2]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: find all element by placeholder
      When launch the following web page:
        """
        html
          body
            div unexpected
            div(placeholder='a') expected1
            div(placeholder='a') expected2
        """
      Then page in driver <driver> should:
        """
        findAll.placeholder[a].text[]= [expected1 expected2]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: should only find elements from current node by css
      When launch the following web page:
        """
        html
          body
            .target unexpected
            .expected
              .target expected
        """
      Then page in driver <driver> should:
        """
        findAll.css[.expected][0].findAll.css[.target].text[]= [expected]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: should only find elements from current node by text
      When launch the following web page:
        """
        html
          body
            div text
            .expected
              span text
        """
      Then page in driver <driver> should:
        """
        findAll.css[.expected][0].findAll.text[text].tag[]= [span]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: should only find elements from current node by placeholder
      When launch the following web page:
        """
        html
        body
          div(placeholder='a') unexpected
          .expected
            div(placeholder='a') expected1
            div(placeholder='a') expected2
        """
      Then page in driver <driver> should:
        """
        findAll.css[.expected][0].findAll.placeholder[a].text[]= [expected1 expected2]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

  Rule: find element

    Scenario Outline: find all element by css
      When launch the following web page:
        """
        html
          body
            div unexpected
            .target expected1
            .target expected2
        """
      Then page in driver <driver> should:
        """
        css[.target].text[]= [expected1 expected2]
        """
      And logs should:
        """
        : | level | message                            |
          | INFO  | Finding: css{html} => css{.target} |
          | INFO  | Found 2 elements                   |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: find all element by simple text
      When launch the following web page:
        """
        html
          body
            div unexpected
            label expected
            span expected
        """
      Then page in driver <driver> should:
        """
        caption[expected].tag[]= [label span]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

  Rule: find single element

    Scenario Outline: single element of all result list
      When launch the following web page:
        """
        html
          body
            div unexpected
            .target expected
        """
      Then page in driver <driver> should:
        """
        css[.target].text= expected
        """
      And logs should:
        """
        : | level | message                            |
          | INFO  | Finding: css{html} => css{.target} |
          | INFO  | Found 1 elements                   |
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
      When find element via driver <driver>:
        """
        css[.target].text
        """
      Then failed with:
        """
        Get property `text` failed, property can be:
          1. public field
          2. public getter
          3. public method
          4. Map key value
          5. customized type getter
          6. static method extension
        java.lang.IllegalStateException: Operations can only be performed on a single located element at: css{html} => css{.target}, but 2 elements were found
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

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
        css[.target].text= hello
        """
      And logs should:
        """
        : | level | message                            |
          | ...                                        |
          | INFO  | Finding: css{html} => css{.target} |
          | INFO  | Found 0 elements                   |
          | INFO  | Finding: css{html} => css{.target} |
          | INFO  | Found 1 elements                   |
          | ...                                        |
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
      When find element via driver <driver>:
        """
        css[.target].text
        """
      Then failed with:
        """
        Get property `text` failed, property can be:
          1. public field
          2. public getter
          3. public method
          4. Map key value
          5. customized type getter
          6. static method extension
        java.lang.IllegalStateException: Operations can only be performed on a single located element at: css{html} => css{.target}, but no elements were found
        """
      And logs should:
        """
        : | level | message                            |
          | ...                                        |
          | INFO  | Finding: css{html} => css{.target} |
          | INFO  | Found 0 elements                   |
          | INFO  | Finding: css{html} => css{.target} |
          | INFO  | Found 0 elements                   |
          | ...                                        |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: Once elements have been operated, the previously located set of elements does not change
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
      When find element via driver <driver>:
        """
        css[.target]: {
          text[]= []
          text= hello
        }
        """
      Then failed with:
        """
        Get property `text` failed, property can be:
          1. public field
          2. public getter
          3. public method
          4. Map key value
          5. customized type getter
          6. static method extension
        java.lang.IllegalStateException: Operations can only be performed on a single located element at: css{html} => css{.target}, but 0 elements were found
        """
      And logs should:
        """
        : | level | message                            |
          | INFO  | Finding: css{html} => css{.target} |
          | INFO  | Found 0 elements                   |
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
      When find element via driver <driver>:
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
