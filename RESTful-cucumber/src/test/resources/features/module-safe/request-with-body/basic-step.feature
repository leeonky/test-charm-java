Feature: Basic Request With Body Steps

  Background:
    Given base url "http://www.a.com"
    Given the following declarations:
      """
      JFactory jfactory = new JFactory();
      """
    And the following class definition:
      """
      public class Request {
        public int intValue;
        public String strValue1, strValue2;

        public int getIntValue() { return intValue; }
        public String getStrValue1() { return strValue1; }
        public String getStrValue2() { return strValue2; }
      }
      """
    And the following class definition:
      """
      public class RequestSpec extends Spec<Request> {}
      """
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
      public class File extends Spec<TextFile> {}
      """
    And register as follows:
      """
      jfactory.register(RequestSpec.class);
      jfactory.register(File.class);
      """
    And use "jfactory" as JFactory

  Rule: no doc type

    Scenario Outline: guess content type from defautRequestContentType(dal:application/json)
      When <method> "/index":
        """
        text: hello
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers: {
            ['Content-Type']: ['application/json']
          }
          body.json= {
            text= hello
          }
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: guess content type from header
      Given header by RESTful api:
        """
        {
          "Content-Type": "text/plain"
        }
        """
      When <method> "/index":
        """
        text: hello
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers: {
            ['Content-Type']: ['text/plain']
          }
          body.string= 'text: hello'
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

  Rule: dal:application/json

    Scenario Outline: <method> doc type overrides header content type
      Given header by RESTful api:
        """
        {
          "Content-Type": "text/plain"
        }
        """
      When <method> "/index":
        """ dal:application/json
        {...}
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: ['application/json']
          body.json= {}
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: with body
      When <method> "/index":
        """ dal:application/json
        text: hello
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: ['application/json']
          body.json= {
            text= hello
          }
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and params
      When <method> "/index?中文参数=中文值&second=value2":
        """ dal:application/json
        text: 'Hello world'
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: ['application/json']
          body.json= {
            text= 'Hello world'
          }
          queryStringParameters: {
           中文参数= [中文值]
           second= [value2]
          }
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and ::header
      When <method> "/index":
        """ dal:application/json
        {
          text: 'Hello world',
          ::headers: {
            key1: value1
            key2: [value2 value3]
          }
        }
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers: {
            'Content-Type': ['application/json']
            key1: ['value1']
            key2: ['value2', 'value3']
          }
          body.json= {
            text= 'Hello world'
          }
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and header step and ::header
      Given header by RESTful api:
        """
        {
          "key1": "value0"
        }
        """
      When <method> "/index":
        """ dal:application/json
        {
          text: 'Hello world',
          ::headers: {
            key1: value1
            key2: [value2 value3]
          }
        }
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers: {
            'Content-Type': ['application/json']
            key1: ['value1']
            key2: ['value2', 'value3']
          }
          body.json= {
            text= 'Hello world'
          }
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: with body and spec
      When <method> "/index":
        """ dal:application/json
        ::this(RequestSpec): {
          intValue: 1
          strValue1: hello
        }
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: ['application/json']
          body.json= {
            intValue= 1
            strValue1= hello
            strValue2= /^strValue2.*/
          }
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

  Rule: dal:multipart/form-data

    Scenario Outline: <method> doc type overrides header content type
      Given header by RESTful api:
        """
        {
          "Content-Type": "text/plain"
        }
        """
      When <method> "/index":
        """ dal:multipart/form-data
        {...}
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: [/^multipart\/form-data; boundary=.*/]
        }]
        """
      And got request form data:
        """
        : []
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: body with virtual file
      When <method> "/index":
        """ dal:multipart/form-data
        {
          text: 'Hello world'
          file(File): {
            name= u.txt
            content= hello-world
          }
        }
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: [/^multipart\/form-data; boundary=.*/]
        }]
        """
      And got request form data:
        """
        : | +fieldName | outputStream.data.string | name  |
          | file       | hello-world              | u.txt |
          | text       | Hello world              | *     |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: with legacy file step
      Given a file "图片1" with name "图片.png":
        """
        hello 头像
        """
      When <method> "/index":
        """ dal:multipart/form-data
        {
          姓名: 张三
          '@附件': 图片1
        }
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: [/^multipart\/form-data; boundary=.*/]
        }]
        """
      And got request form data:
        """
        : | +fieldName | outputStream.data.string | name     |
          | 姓名       | 张三                     | *        |
          | 附件       | hello 头像               | 图片.png |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and params
      When <method> "/index?中文参数=中文值&second=value2":
        """ dal:multipart/form-data
        text: 'Hello world'
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: [/^multipart\/form-data; boundary=.*/]
          queryStringParameters: {
           中文参数= [中文值]
           second= [value2]
          }
        }]
        """
      And got request form data:
        """
        : | +fieldName | outputStream.data.string |
          | text       | Hello world              |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and ::header
      When <method> "/index":
        """ dal:multipart/form-data
        {
          text: 'Hello world',
          ::headers: {
            key1: value1
            key2: [value2 value3]
          }
        }
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers: {
            'Content-Type': [/^multipart\/form-data; boundary=.*/]
            key1: ['value1']
            key2: ['value2', 'value3']
          }
        }]
        """
      And got request form data:
        """
        : | +fieldName | outputStream.data.string |
          | text       | Hello world              |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and header step and ::header
      Given header by RESTful api:
        """
        {
          "key1": "value0"
        }
        """
      When <method> "/index":
        """ dal:multipart/form-data
        {
          text: 'Hello world',
          ::headers: {
            key1: value1
            key2: [value2 value3]
          }
        }
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers: {
            'Content-Type': [/^multipart\/form-data; boundary=.*/]
            key1: ['value1']
            key2: ['value2', 'value3']
          }
        }]
        """
      And got request form data:
        """
        : | +fieldName | outputStream.data.string |
          | text       | Hello world              |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: with body and spec
      When <method> "/index":
        """ dal:multipart/form-data
        ::this(RequestSpec): {
          intValue: 1
          strValue1: hello
        }
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: [/^multipart\/form-data; boundary=.*/]
        }]
        """
      And got request form data:
        """
        : | +fieldName | outputStream.data.string |
          | intValue   | '1'                      |
          | strValue1  | hello                    |
          | strValue2  | /^strValue2.*/           |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

  Rule: application/json

    Scenario Outline: <method> doc type overrides header content type
      Given header by RESTful api:
        """
        {
          "Content-Type": "text/plain"
        }
        """
      When <method> "/index":
        """ application/json
        {}
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers: {
            ['Content-Type']: ['application/json']
          }
          body.json= {}
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: body
      When <method> "/index":
        """ application/json
        raw-string
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: ['application/json']
          body.rawBytes.base64.string= raw-string
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and params
      When <method> "/index?中文参数=中文值&second=value2":
        """ application/json
        raw-string
        """
      Then got request:
        """
        : [{
          method: <method>
          path: '/index'
          headers[Content-Type]: ['application/json']
          body.rawBytes.base64.string= raw-string
          queryStringParameters: {
           中文参数= [中文值]
           second= [value2]
          }
        }]
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |
