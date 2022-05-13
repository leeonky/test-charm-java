Feature: RESTful api steps

  Background:
    Given base url "http://www.a.com"

  Scenario Outline: <method> with no params
    When <method> "/index"
    Then "http://www.a.com" got a "<method>" request on "/index"
    Examples:
      | method |
      | GET    |
      | DELETE |

  Scenario Outline: <method> with body and no params
    When <method> "/index":
    """
    { "text": "Hello world" }
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body
    """
    { "text": "Hello world" }
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> with header
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
      method: '<method>'
      path: '/index'
      headers: {
        key1: ['value1']
        key2: ['value2', 'value3']
      }
    }]
    """
    And "http://www.a.com" got a "<method>" request on "/index"
    Examples:
      | method |
      | GET    |
      | DELETE |

  Scenario Outline: <method> with body and header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": ["value2", "value3"]
    }
    """
    When <method> "/index":
    """
    { "text": "Hello world" }
    """
    Then got request:
    """
    : [{
      method: '<method>'
      path: '/index'
      headers: {
        ['Content-Type']: ['application/json']
        key1: ['value1']
        key2: ['value2', 'value3']
      }
    }]
    """
    And "http://www.a.com" got a "<method>" request on "/index"
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> with content type
    When <method> "/index":
    """text/html
    { "text": "Hello world" }
    """
    Then got request:
    """
    : [{
      headers['Content-Type']: ['text/html']
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> response
    Given response 200 on "<method>" "/index":
    """
    Hello world
    """
    When <method> "/index"
    Then response should be:
    """
    : {
      code=200
      body.string='Hello world'
      raw.class.simpleName='HttpURLConnection'
    }
    """
    Examples:
      | method |
      | GET    |
      | DELETE |

  Scenario Outline: <method> with body and response
    Given response 200 on "<method>" "/index":
    """
    Hello world
    """
    When <method> "/index":
    """
    { "text": "Hello world" }
    """
    Then response should be:
    """
    : {
      code=200
      body.string='Hello world'
      raw.class.simpleName='HttpURLConnection'
    }
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario: GET download response
    Given binary response 200 on GET "/download" with file name "download.txt":
    """
    Hello world
    """
    When GET "/download"
    Then response should be:
    """
    : {
      code=200
      body.string='Hello world'
      fileName='download.txt'
    }
    """

  Scenario: GET download response chinese file name
    Given binary response 200 on GET "/download" with file name "下载.txt":
    """
    Hello world
    """
    When GET "/download"
    Then response should be:
    """
    : {
      code=200
      body.string='Hello world'
      fileName='下载.txt'
    }
    """

  Scenario: upload file request
    Given a file "an avatar":
    """
    hello avatar
    """
    When POST form "/users":
    """
    {
      "name": "Tom",
      "@avatar": "an avatar"
    }
    """
    Then got request:
    """
    : [{
      method: 'POST'
      path: '/users'
      headers: {
        ['Content-Type']: [/^multipart\/form-data.*/]
      }
    }]
    """
    And got request form value:
    """
    : [{
      headers: /.*name="name"(.|\r|\n)*/
      body.string: 'Tom'
    } {
      headers: /.*name="avatar"(.|\r|\n)*/
      headers: /.*filename=".*\.upload"(.|\r|\n)*/
      body.string: 'hello avatar'
    }]
    """

  Scenario: upload file request with name
    Given a file "an avatar" with name "image.png":
    """
    hello avatar
    """
    When POST form "/users":
    """
    {
      "@avatar": "an avatar"
    }
    """
    And got request form value:
    """
    : [{
      headers: /.*name="avatar"(.|\r|\n)*/
      headers: /.*filename="image\.png"(.|\r|\n)*/
      body.string: 'hello avatar'
    }]
    """

  Scenario: post form with unicode
    Given a file "图片1" with name "图片.png":
    """
    hello 头像
    """
    When POST form "/users":
    """
    {
      "姓名": "张三",
      "@附件": "图片1"
    }
    """
    Then got request:
    """
    : [{
      method: 'POST'
      path: '/users'
      headers: {
        ['Content-Type']: [/^multipart\/form-data.*/]
      }
    }]
    """
    And got request form value:
    """
    : [{
      headers: /.*name="%E5%A7%93%E5%90%8D"(.|\r|\n)*/
      body.string: '%E5%BC%A0%E4%B8%89'
    } {
      headers: /.*name="%E9%99%84%E4%BB%B6"(.|\r|\n)*/
      headers: /.*filename="%E5%9B%BE%E7%89%87.png"(.|\r|\n)*/
      body.string: 'hello 头像'
    }]
    """

  Scenario: get and verify response in one step
    Given response 200 on "GET" "/index":
    """
    Hello world
    """
    Then "/index" should response:
    """
    : {
      code=200
      body.string='Hello world'
    }
    """

  Scenario Outline: verify <method> and get response
    Given response 200 on "<method>" "/index":
    """
    Hello world
    """
    Given response 200 on "GET" "/index":
    """
    Hello world
    """
    Then <method> "/index":
    """
    any body
    """
    Then data should be saved to "/index" with response:
    """
    : {
      code=200
      body.string='Hello world'
    }
    """
    Examples:
      | method |
      | POST   |
      | PUT    |
