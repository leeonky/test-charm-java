Feature: operation

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

    Scenario Outline: screenshot
      Given launch the following web page:
        """
        html
          head
          body
            .target hello
        """
      When perform via driver <driver>:
        """
        css[.target].screenshot: {
          class.simpleName: 'byte[]'
          string::should.contains: 'PNG'
        }
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: html
      Given launch the following web page:
        """
        html
          head
          body
            .target
              label hello
        """
      When perform via driver <driver>:
        """
        css[.target]: {
          dom= '<div class="target"><label>hello</label></div>'
        }
        """

      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: download file
      Given launch the following web page:
        """
        html
        head
            script.
              function downloadFile() {
                  const element = document.createElement('a');
                  element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent('hello world'));
                  element.setAttribute('download', 'hello.txt');
                  document.body.appendChild(element);
                  element.click();
                  document.body.removeChild(element);
              }
        body
            button(onclick="downloadFile()") Download
        """
      When perform via driver <driver>:
        """
        css[button].download
        """
      Then working dir should:
        """
        ::eventually: { hello.txt: 'hello world' }
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: element enable
      Given launch the following web page:
        """
        html
          head
          body
            .disabled
              input(type= 'text' disabled)
              textarea(disabled)
              select(disabled)
            .enabled
              input(type= 'text')
              textarea
              select
        """
      Then page in driver <driver> should:
        """
        css: {
          [.disabled].css: {
            input.enabled= false
            textarea.enabled= false
            select.enabled= false
          }
          [.enabled].css: {
              input.enabled= true
              textarea.enabled= true
              select.enabled= true
          }
        }
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: element visible
      Given launch the following web page:
        """
        html
        head
        body
          .hidden(style='display:none')
            input(type= 'text')
            textarea
            select
          .visible
            input(type= 'text')
            textarea
            select
        """
      Then page in driver <driver> should:
        """
        css: {
          [.hidden].css: {
              input.visible= false
              textarea.visible= false
              select.visible= false
          }
          [.visible].css: {
              input.visible= true
              textarea.visible= true
              select.visible= true
          }
        }
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: sort element by text
      Given launch the following web page:
        """
        html
          head
          body
            .target Banana
            .target Apple
            .target Cherry
        """
      Then page in driver <driver> should:
        """
        css[.target]: +[Apple Banana Cherry]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

  Rule: element attribute

    Scenario Outline: access element attribute by @ + attribute name
      Given launch the following web page:
        """
        html
        head
        body
          .target(attr='value')
        """
      Then page in driver <driver> should:
        """
        css[.target].@attr= value
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: @class should be an array
      Given launch the following web page:
        """
        html
        head
        body
          div(class='class1 class2')
        """
      Then page in driver <driver> should:
        """
        css[div].@class= [class1 class2]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: ::watch should invoke org.testcharm.dal.extensions.inspector.Inspector::watch with screen shot
      Given launch the following web page:
        """
        html
        head
        body
          div hello
        """
      When perform via driver <driver>:
        """
        css.div::watch,
        ::global[css.div]= {
          class.simpleName: 'byte[]'
          string::should.contains: 'PNG'
        }
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

