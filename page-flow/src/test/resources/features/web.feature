Feature: web ui

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
        : | level | message                             |
          | INFO  | Locating: css{html} => css{.target} |
          | INFO  | Found 2 elements                    |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: find all element by caption
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
      And logs should:
        """
        : | level | message                                  |
          | INFO  | Locating: css{html} => caption{expected} |
          | INFO  | Found 2 elements                         |
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
        xpath["//div[@attr='a']"].text[]= [expected1 expected2]
        """
      And logs should:
        """
        : | level | message                                        |
          | INFO  | Locating: css{html} => xpath{//div[@attr='a']} |
          | INFO  | Found 2 elements                               |
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
        placeholder[a].text[]= [expected1 expected2]
        """
      And logs should:
        """
        : | level | message                               |
          | INFO  | Locating: css{html} => placeholder{a} |
          | INFO  | Found 2 elements                      |
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
        : | level | message                             |
          | INFO  | Locating: css{html} => css{.target} |
          | INFO  | Found 1 elements                    |
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
        : | level | message                             |
          | ...                                         |
          | INFO  | Locating: css{html} => css{.target} |
          | INFO  | Found 0 elements                    |
          | INFO  | Locating: css{html} => css{.target} |
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
        : | level | message                             |
          | ...                                         |
          | INFO  | Locating: css{html} => css{.target} |
          | INFO  | Found 0 elements                    |
          | INFO  | Locating: css{html} => css{.target} |
          | INFO  | Found 0 elements                    |
          | ...                                         |
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
      When try to find element via driver <driver>:
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
        : | level | message                             |
          | INFO  | Locating: css{html} => css{.target} |
          | INFO  | Found 0 elements                    |
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

  Rule: ui operation

    Scenario Outline: click element
      Given launch the following web page:
        """
        html
          head
            script.
              function changeText() {
                const target = document.querySelector('.target');
                target.textContent = 'HelloWorld';
              }
          body
            .target(onclick="changeText()") click
        """
      When perform via driver <driver>:
        """
        css[.target].click
        """
      Then page in driver <driver> should:
        """
        css[.target]::eventually: {
          text= HelloWorld
        }
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: return tag name in lower case
      Given launch the following web page:
        """
        html
          head
          body
            .target
        """
      Then page in driver <driver> should:
        """
        css[.target].tag= div
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: web element textarea and input is input
      Given launch the following web page:
        """
        html
          head
          body
            textarea
            input
            div
        """
      Then page in driver <driver> should:
        """
        css:     | input |
        textarea | true  |
           input | true  |
             div | false |
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: type in and verify via value
      Given launch the following web page:
        """
        html
          head
          body
            textarea
        """
      When perform via driver <driver>:
        """
        css[textarea].typeIn[hello]
        """
      Then page in driver <driver> should:
        """
        css[textarea].value= hello
        """
      When perform via driver <driver>:
        """
        css[textarea].typeIn[world]
        """
      Then page in driver <driver> should:
        """
        css[textarea].value= helloworld
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: clear input
      Given launch the following web page:
        """
        html
          head
          body
            input(value= 'any str')
        """
      When perform via driver <driver>:
        """
        css[input].clear
        """
      Then page in driver <driver> should:
        """
        css[input].value= ''
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: fill in means clear and type in
      Given launch the following web page:
        """
        html
          head
          body
            input(value= 'any str')
        """
      When perform via driver <driver>:
        """
        css[input].fillIn: hello
        """
      Then page in driver <driver> should:
        """
        css[input].value= hello
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: fill in (check) checkbox
      Given launch the following web page:
        """
        html
          head
          body
            input(type= 'checkbox')
        """
      When perform via driver <driver>:
        """
        css[input].fillIn: true
        """
      Then page in driver <driver> should:
        """
        css[input].value= true
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: fill in (uncheck) checkbox
      Given launch the following web page:
        """
        html
          head
          body
            input(type= 'checkbox', checked)
        """
      When perform via driver <driver>:
        """
        css[input].fillIn: false
        """
      Then page in driver <driver> should:
        """
        css[input].value= false
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |


#  auto fillin
#  fill select multi select