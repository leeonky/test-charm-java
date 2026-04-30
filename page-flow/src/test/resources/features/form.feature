Feature: form

  Rule: fill in input

    Scenario Outline: web element textarea input select is input
      Given launch the following web page:
        """
        html
          head
          body
            textarea
            input
            select
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
      Then page in driver <driver> should:
        """
        css[input].value= 'any str'
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
      Then page in driver <driver> should:
        """
        css[input].value= false
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
      Then page in driver <driver> should:
        """
        css[input].value= true
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

    Scenario Outline: fill in select
      Given launch the following web page:
        """
        html
          head
          body
            select
              option Apple
              option Banana
              option(selected) Cherry
              option Date
        """
      Then page in driver <driver> should:
        """
        css[select].value= Cherry
        """
      When perform via driver <driver>:
        """
        css[select].fillIn: Banana
        """
      Then page in driver <driver> should:
        """
        css[select].value= Banana
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: fill in multi select
      Given launch the following web page:
        """
        html
          head
          body
            select(multiple)
              option Apple
              option Banana
              option(selected) Cherry
              option Date
        """
      Then page in driver <driver> should:
        """
        css[select].value= Cherry
        """
      When perform via driver <driver>:
        """
        css[select].fillIn: [Apple Date]
        """
      Then page in driver <driver> should:
        """
        css[select].value= [Apple Date]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: upload one file
      Given the following class definition:
        """
        public class TextFile implements org.testcharm.io.VirtualFile {
          private String name;

          public String getName() {
            return name;
          }

          public void setName(String name) {
            this.name = name;
          }

          public String content;

          @Override
          public byte[] binary() {
              return content.getBytes();
          }
        }
        """
      Given the following class definition:
        """
        public class File extends Spec<TextFile> {
        }
        """
      And register "jFactory" with:
        """
        jFactory.register(File.class);
        """
      Given launch the following web page:
        """
        html
        head
        body
          form(action="http://host.docker.internal:10081/submit" method="POST")
            input(type= 'file' name= 'f')
            button(type="submit") Submit
        """
      When perform via driver <driver>:
        """
        css[input].fillIn(File): {
          name= foo
          content= 'hello world'
        }

        css[button].click: {...}
        """
      Then server should receive form data:
        """
        = {
          f: foo
        }
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

  Rule: Submit Form

    Scenario Outline:  Submit Form
      Given launch the following web page:
        """
        html
          head
          body
            form(action="http://host.docker.internal:10081/submit" method="POST")
              input(name="username" type="text")
              input(name="password" type="password")
              button(type="submit") Submit
        """
      When perform via driver <driver>:
        """
        css: {
        'input[name=username]'.fillIn: alice
        'input[name=password]'.fillIn: secret
        'button[type=submit]'.click: {...}
        }
        """
      Then server should receive form data:
        """
        = {
          username= alice
          password= secret
        }
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline:  Submit Form with JFactory
      And the following class definition:
        """
        public class UserInfo {
          public String username, password;
        }
        """
      Given the following class definition:
        """
        import org.testcharm.pf.*;
        import org.testcharm.pf.cucumber.<driver>.*;
        import org.testcharm.jfactory.*;
        public class MainPage extends AbstractPanel<<driver>E> {
          public MainPage(<driver>E element) {
            super(element);
          }

          public ScopedJFactoryCollector login() {
            return new ScopedJFactoryCollector(new JFactory(), UserInfo.class) {
              public void onExit() {
                UserInfo info = (UserInfo)build();
                perform("css['input[name=username]'].fillIn: $username", info);
                perform("css['input[name=password]'].fillIn: $password", info);
                perform("css['button[type=submit]'].click");
              }
            };
          }
        }
        """
      And launch the following web page:
        """
        html
          head
          body
            form(action="http://host.docker.internal:10081/submit" method="POST")
              input(name="username" type="text")
              input(name="password" type="password")
              button(type="submit") Submit
        """
      When perform page "MainPage" via driver <driver>:
        """
        login: {
          username: alice
          password: secret
        }
        """
      Then server should receive form data:
        """
        = {
          username= alice
          password= secret
        }
        """
      Examples:
        | driver     |
        | Selenium   |
        | Playwright |

