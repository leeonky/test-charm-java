Feature: Request With Spec and Body Steps

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
    And the following class definition:
      """
      public class RequestFile extends Request {
        public org.testcharm.io.VirtualFile file;
      }
      """
    And the following class definition:
      """
      public class RequestFileSpec extends Spec<RequestFile> {}
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
      jfactory.register(RequestFileSpec.class);
      jfactory.register(File.class);
      """
    And use "jfactory" as JFactory

  Rule: no doc type

    Scenario Outline: guess content type from defautSpecRequestContentType(application/json)
      When <method> "RequestSpec" "/index":
        """
        {
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

    Scenario Outline: guess content type from header
      Given header by RESTful api:
        """
        {
          "Content-Type": "multipart/form-data"
        }
        """
      When <method> "RequestSpec" "/index":
        """
        {
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
      When <method> "RequestSpec" "/index":
        """ application/json
        {
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

    Scenario Outline: with body
      When <method> "RequestSpec" "/index":
        """ application/json
        strValue1: hello
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

    Scenario Outline: <method> with body and params
      When <method> "RequestSpec" "/index?中文参数=中文值&second=value2":
        """ application/json
        strValue1: hello
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
      When <method> "RequestSpec" "/index":
        """ application/json
        {
          strValue1: hello
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

    Scenario Outline: <method> with body and header step and ::header
      Given header by RESTful api:
        """
        {
          "key1": "value0"
        }
        """
      When <method> "RequestSpec" "/index":
        """ dal:application/json
        {
          strValue1: hello
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

  Rule: multipart/form-data

    Scenario Outline: <method> doc type overrides header content type
      Given header by RESTful api:
        """
        {
          "Content-Type": "text/plain"
        }
        """
      When <method> "RequestSpec" "/index":
        """ multipart/form-data
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
        : | +fieldName | outputStream.data.string |
          | intValue   | '1'                      |
          | strValue1  | /^strValue1.*/           |
          | strValue2  | /^strValue2.*/           |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: body with virtual file
      When <method> "RequestFileSpec" "/index":
        """ multipart/form-data
        {
          strValue1: 'Hello world'
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
          | intValue   | '1'                      | *     |
          | strValue1  | Hello world              | *     |
          | strValue2  | /^strValue2.*/           | *     |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and params
      When <method> "RequestSpec" "/index?中文参数=中文值&second=value2":
        """ multipart/form-data
        strValue1: hello
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
          | intValue   | '1'                      |
          | strValue1  | hello                    |
          | strValue2  | /^strValue2.*/           |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |

    Scenario Outline: <method> with body and ::header
      When <method> "RequestSpec" "/index":
        """ multipart/form-data
        {
          strValue1: hello
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
          | intValue   | '1'                      |
          | strValue1  | hello                    |
          | strValue2  | /^strValue2.*/           |
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
      When <method> "RequestSpec" "/index":
        """ multipart/form-data
        {
          strValue1: hello
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
          | intValue   | '1'                      |
          | strValue1  | hello                    |
          | strValue2  | /^strValue2.*/           |
        """
      Examples:
        | method |
        | POST   |
        | PUT    |
        | PATCH  |
