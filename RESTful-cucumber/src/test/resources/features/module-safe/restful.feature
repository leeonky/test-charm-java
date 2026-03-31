Feature: RESTful api steps

  Background:
    Given base url "http://www.a.com"

  Rule: Basic Response

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

    Scenario: GET and then use response twice
      Given response 200 on "GET" "/index":
      """
      Hello world
      """
      When GET "/index"
      Then response should be:
      """
      body.string='Hello world'
      """
      Then response should be:
      """
      body.string='Hello world'
      """

    Scenario: GET response with header
      Given response 200 on "GET" "/index" with body "Hello world" and headers:
      """
      {
        "key1": "value1",
        "key2": ["value2", "value3"]
      }
      """
      When GET "/index"
      Then response should be:
      """
      : {
        code=200
        body.string='Hello world'
        headers: {
          key1= value1
          key2= [value3, value2]
        }
      }
      """

    Scenario: GET and then use response header twice
      Given response 200 on "GET" "/index" with body "Hello world" and headers:
      """
      {
        "key1": "value1",
        "key2": ["value2", "value3"]
      }
      """
      When GET "/index"
      Then response should be:
      """
      headers: {
        key1= value1
        key2= [value3, value2]
      }
      """
      Then response should be:
      """
      headers: {
        key1= value1
        key2= [value3, value2]
      }
      """

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

  Rule: Post Form, Upload and Download

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
      And got request form data:
      """
      : | +fieldName | outputStream.data.string | name     |
        | 姓名       | 张三                     | *        |
        | 附件       | hello 头像               | 图片.png |
      """

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
      And got request form data:
      """
      : | +fieldName | outputStream.data.string | name       |
        | avatar     | hello avatar             | /.*upload/ |
        | name       | Tom                      | *          |
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
      And got request form data:
      """
      : | +fieldName | outputStream.data.string | name      |
        | avatar     | hello avatar             | image.png |
      """

  Rule: Request and Response in One Step

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

    Scenario: delete and verify response in one step
      Given response 200 on "DELETE" "/index":
      """
      Hello world
      """
      Then DELETE "/index" should response:
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
      """ text/plain
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
