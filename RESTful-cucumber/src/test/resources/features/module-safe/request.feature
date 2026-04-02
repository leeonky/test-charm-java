@jfactory
Feature: Request without Body

  Background:
    Given base url "http://www.a.com"

  Rule: GET/DELETE

    Scenario Outline: <method> with no params
      When <method> "/index"
      Then got request:
      """
      : [{
        method= '<method>'
        path= '/index'
        queryStringParameters= null
      }]
      """
      Examples:
        | method |
        | GET    |
        | DELETE |

    Scenario Outline: <method> with params in url
      When <method> "/index?中文参数=中文值&second=value2"
      Then got request:
      """
      : [{
        method= '<method>'
        path= '/index'
        queryStringParameters= {
          中文参数= [中文值]
          second= [value2]
        }
      }]
      """
      Examples:
        | method |
        | GET    |
        | DELETE |

    Scenario Outline: <method> with header step
      Given header by RESTful api:
        """
        {
          "key1": "value1",
          "key2": ["value2", "value3"]
        }
        """
      When <method> "/index"
      Then got request:
        """
        : [{
          method= '<method>'
          path= '/index'
          headers: {
            key1= ['value1']
            key2= ['value2', 'value3']
          }
        }]
        """
      Examples:
        | method |
        | GET    |
        | DELETE |

  Rule: GET/DELETE with docstring

    Scenario Outline: <method> with single value param
      When <method> "/index":
        """ application/json
        {
          "中文参数": "中文值",
          "second": "value1"
        }
        """
      Then got request:
        """
        : [{
          method= '<method>'
          path= '/index'
          queryStringParameters= {
            中文参数= [中文值]
            second= [value1]
          }
        }]
        """
      Examples:
        | method |
        | GET    |
        | DELETE |

    Scenario Outline: <method> with multiple values params
      When <method> "/index":
        """
        {
          "key": ["value1", "value2"]
        }
        """
      Then got request:
        """
        : [{
          method= '<method>'
          path= '/index'
          queryStringParameters.'key[]'= [value1, value2]
        }]
        """
      Examples:
        | method |
        | GET    |
        | DELETE |

    Scenario Outline: <method> with single value param need encoding
      When <method> "/index":
        """
        {
          ":url:": "http://www.a.com/index?中文参数=中文值",
        }
        """
      Then got request:
        """
        : [{
          method= '<method>'
          path= '/index'
          queryStringParameters= {
            ':url:'= ['http://www.a.com/index?中文参数=中文值']
          }
        }]
        """
      Examples:
        | method |
        | GET    |
        | DELETE |

    Scenario Outline: <method> with header
      When <method> "/index":
        """
        ::headers: {
          key1: value1
          key2: [value2 value3]
        }
        """
      Then got request:
        """
        : [{
          method: '<method>'
          path: '/index'
          headers: {
            key1: ['value1']
            key2: ['value2', 'value3']
          }
        }]
        """
      Examples:
        | method |
        | GET    |
        | DELETE |

    Scenario Outline: <method> with header and query params
      When <method> "/index":
        """
        {
          "key": ["value1", "value2"]
          ::headers: {
            key1: value1
            key2: [value2 value3]
          }
        }
        """
      Then got request:
        """
        : [{
          method: '<method>'
          path: '/index'
          headers: {
            key1: ['value1']
            key2: ['value2', 'value3']
          }
          queryStringParameters: {
              'key[]': [value1 value2]
          }
        }]
        """
      Examples:
        | method |
        | GET    |
        | DELETE |

  Rule: Request Body Without any content type

    Background:
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
      And register as follows:
        """
        jfactory.register(RequestSpec.class);
        """
      And use "jfactory" as JFactory

    Scenario Outline: specify spec in step
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
          method: '<method>'
          path: '/index'
          headers: {
            ['Content-Type']: ['application/json']
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

