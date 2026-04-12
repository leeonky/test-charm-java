Feature: Use RestfulStep in source code

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
    Given the following declarations:
      """
      org.testcharm.cucumber.restful.RestfulStep restfulStep = new org.testcharm.cucumber.restful.RestfulStep();
      """
    And register as follows:
      """
      jfactory.register(RequestSpec.class);
      jfactory.register(File.class);
      restfulStep.setJFactory(jfactory);
      restfulStep.setBaseUrl("http://www.a.com:8080");
      """

  Scenario Outline: Request <method>WithSpec
    When executing the following code:
      """
      restfulStep.<method>WithSpec("/index", "application/json", new String[]{"RequestSpec"}, "{intValue: 1, strValue1: hello}");
      """
    Then got request:
      """
      : [{
        method.toLowerCase: <method>
        path: '/index'
        headers[Content-Type]: [application/json]
        body.json= {
          intValue= 1
          strValue1= hello
          strValue2= /^strValue2.*/
        }
      }]
      """
    Examples:
      | method |
      | post   |
      | put    |
      | patch  |

  Scenario Outline: Request <method>
    When executing the following code:
      """
      restfulStep.<method>("/index", "application/json", "text: hello");
      """
    Then got request:
      """
      : [{
        method.toLowerCase: <method>
        path: '/index'
        headers[Content-Type]: [application/json]
        body.rawBytes.base64.string= 'text: hello'
      }]
      """
    Examples:
      | method |
      | post   |
      | put    |
      | patch  |

  Scenario Outline: Request <method> in Default
    And register as follows:
      """
      restfulStep.setDefaultDocType("text/plain");
      """
    When executing the following code:
      """
      restfulStep.<method>InDefault("/index", "hello");
      """
    Then got request:
      """
      : [{
        method.toLowerCase: <method>
        path: '/index'
        headers[Content-Type]: [text/plain]
        body.rawBytes.base64.string= hello
      }]
      """
    Examples:
      | method |
      | post   |
      | put    |
      | patch  |

  Scenario Outline: Request <method> Object
    When executing the following code:
      """
      restfulStep.<method>Object("/index", "application/json", new java.util.HashMap<String, Object>(){{
        put("intValue", 1);
        put("strValue1", "hello");
      }});
      """
    Then got request:
      """
      : [{
        method.toLowerCase: <method>
        path: '/index'
        headers[Content-Type]: [application/json]
        body.json= {
          intValue= 1
          strValue1= hello
        }
      }]
      """
    Examples:
      | method |
      | post   |
      | put    |
      | patch  |

  Scenario Outline: Request <method> Object in Default
    And register as follows:
      """
      restfulStep.setDefaultDocType("application/json");
      """
    When executing the following code:
      """
      Request request = new Request();
      request.intValue = 1;
      request.strValue1 = "hello";
      request.strValue2 = "world";
      restfulStep.<method>ObjectInDefault("/index", request);
      """
    Then got request:
      """
      : [{
        method.toLowerCase: <method>
        path: '/index'
        headers[Content-Type]: [application/json]
        body.json= {
          intValue= 1
          strValue1= hello
          strValue2= world
        }
      }]
      """
    Examples:
      | method |
      | post   |
      | put    |
      | patch  |

  Scenario Outline: Request <method> in Json
    And register as follows:
      """
      restfulStep.setDefaultDocType("text/plain");
      """
    When executing the following code:
      """
      restfulStep.<method>InJson("/index", "100");
      """
    Then got request:
      """
      : [{
        method.toLowerCase: <method>
        path: '/index'
        headers[Content-Type]: [application/json]
        body.json= 100
      }]
      """
    Examples:
      | method |
      | post   |
      | put    |
      | patch  |

  Scenario Outline: Request <method> Object in Json
    And register as follows:
      """
      restfulStep.setDefaultDocType("text/plain");
      """
    When executing the following code:
      """
      Request request = new Request();
      request.intValue = 1;
      request.strValue1 = "hello";
      request.strValue2 = "world";
      restfulStep.<method>ObjectInJson("/index", request);
      """
    Then got request:
      """
      : [{
        method.toLowerCase: <method>
        path: '/index'
        headers[Content-Type]: [application/json]
        body.json= {
          intValue= 1
          strValue1= hello
          strValue2= world
        }
      }]
      """
    Examples:
      | method |
      | post   |
      | put    |
      | patch  |
