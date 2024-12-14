@jfactory
Feature: JFactory Integration

  Background:
    Given base url "http://www.a.com"

  Scenario Outline: <method> with body created by spec
    When <method> "LoginRequest" "/index":
    """
    {
      "username": "admin",
      "captcha": {
        "code": "1234"
      }
    }
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      method: '<method>'
      path: '/index'
      headers: {
        ['Content-Type']: ['application/json']
      }
      body.json= {
        username: admin,
        password: password#1,
        captcha: {
          code: '1234'
        }
      }
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> with body array created by spec
    When <method> "LoginRequest" "/index":
    """
    [{
      "username": "admin",
      "captcha": {
        "code": "1234"
      }
    }]
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      body.json= [{
        username: admin,
        password: password#1,
        captcha: {
          code: '1234'
        }
      }]
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> with body created by trait and spec
    When <method> "<traitAndSpec>" "/index":
    """
    {
      "username": "admin"
    }
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      body.json= {
        username: admin,
        password: wrongPassword,
        captcha: null
      }
    }]
    """
    Examples:
      | method | traitAndSpec               |
      | POST   | WrongPassword LoginRequest |
      | POST   | WrongPassword,LoginRequest |
      | PUT    | WrongPassword LoginRequest |
      | PUT    | WrongPassword,LoginRequest |

  Scenario Outline: <method> with body array created by trait and spec
    When <method> "<traitAndSpec>" "/index":
    """
    [{
      "username": "admin"
    }]
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      body.json= [{
        username: admin,
        password: wrongPassword,
        captcha: null
      }]
    }]
    """
    Examples:
      | method | traitAndSpec               |
      | POST   | WrongPassword LoginRequest |
      | POST   | WrongPassword,LoginRequest |
      | PUT    | WrongPassword LoginRequest |
      | PUT    | WrongPassword,LoginRequest |

  Scenario Outline: <method> with body created by spec and inline replace
    Given var "user" value is "admin"
    When <method> "LoginRequest" "/index":
    """
    {
      "username": "${user}"
    }
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      body.json.username= admin
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario: post form with body created by spec
    When POST form "LoginRequest" to "/index":
    """
    {captcha: {...}}
    """
    Then "http://www.a.com" got a "POST" request on "/index" with body matching
    """
    : [{
      body.string= /.*username#1.*/
    }]
    """
    And got request form value:
      """
      : [{
        headers: /.*name="username"(.|\r|\n)*/
        body.string: /.*username.*/
      } {
        headers: /.*name="captcha"(.|\r|\n)*/
        body.string: /.*Captcha.*/
      } {
        headers: /.*name="password"(.|\r|\n)*/
        body.string: /.*password.*/
      }]
      """

  Scenario: post form with body created by trait and spec
    When POST form "WrongPassword LoginRequest" to "/index":
    """
    {captcha: {...}}
    """
    And got request form value:
      """
      : [{
        headers: /.*name="username"(.|\r|\n)*/
        body.string: /.*username.*/
      } {
        headers: /.*name="captcha"(.|\r|\n)*/
        body.string: /.*Captcha.*/
      } {
        headers: /.*name="password"(.|\r|\n)*/
        body.string: /.*wrongPassword.*/
      }]
      """

  Scenario: post form with file created by spec
    Given a file "aFile" with name "image.png":
      """
      hello 头像
      """
    When POST form "DefaultFormBean" to "/index":
    """
    {
      str= bla
    }
    """
    And got request form value:
      """
      : [{
        headers: /.*name="str"(.|\r|\n)*/
        body.string: bla
      }{
        headers: /.*name="oneFile"(.|\r|\n)*/
        headers: /.*filename="图片.png"(.|\r|\n)*/
        body.string: 'hello 头像'
      }]
      """
