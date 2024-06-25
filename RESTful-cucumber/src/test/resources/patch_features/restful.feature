Feature: RESTful api steps

  Background:
    Given base url "http://www.a.com"

  Rule: Basic Request

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
        | PATCH  |

    Scenario Outline: <method> with body and params
      When <method> "/index?中文参数=中文值&second=value2":
      """
      { "text": "Hello world" }
      """
      Then "http://www.a.com" got a "<method>" request on "/index" with params "中文参数=中文值&second=value2" and body
      """
      { "text": "Hello world" }
      """
      Examples:
        | method |
        | PATCH  |

    Scenario Outline: <method> with body and header and default content type is application/json
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
        | PATCH  |

    Scenario Outline: <method> with content type and not set in header
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
        | PATCH  |

    Scenario Outline: <method> without content type but set in header already
      Given header by RESTful api:
      """
      {
        "<header>": "text/html"
      }
      """
      When <method> "/index":
      """
      { "text": "Hello world" }
      """
      Then got request:
      """
      : [{
        headers['<header>']: ['text/html']
      }]
      """
      Examples:
        | method | header       |
        | PATCH  | content-Type |

    Scenario Outline: <method> with content type and set in header
      Given header by RESTful api:
      """
      {
        "Content-Type": "anyContentType"
      }
      """
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
        | PATCH  |

    Scenario Outline: <method> without content type and set in header
      Given header by RESTful api:
      """
      {
        "anyHeader": null
      }
      """
      When <method> "/index":
      """
      {}
      """
      Then got request:
      """
      : [{
        headers['anyHeader']: null
      }]
      """
      Examples:
        | method |
        | PATCH  |

  Rule: Basic Response

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
        | PATCH  |

  Rule: Request and Response in One Step

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
        | PATCH  |

  Rule: Request with Binary Body

    Scenario Outline: <method> with binary body
      When <method> "/index":
      """application/octet-stream
      'hello world'.bytes
      """
      Then "http://www.a.com" got a "<method>" request on "/index" with body matching
      """
      : [{
        method: '<method>'
        path: '/index'
        headers: {
          ['Content-Type']: ['application/octet-stream']
        }
        body.base64Bytes.base64.string= 'hello world'
      }]
      """
      Examples:
        | method |
        | PATCH  |

    Scenario Outline: <method> with binary body of string
      When <method> "/index":
      """application/octet-stream
      'hello world'
      """
      Then "http://www.a.com" got a "<method>" request on "/index" with body matching
      """
      : [{
        body.base64Bytes.base64.string= 'hello world'
      }]
      """
      Examples:
        | method |
        | PATCH  |

    Scenario Outline: <method> with binary body of file
      Given a file "an avatar":
      """
      hello avatar
      """
      When <method> "/index":
      """application/octet-stream
      files['an avatar'].content
      """
      Then "http://www.a.com" got a "<method>" request on "/index" with body matching
      """
      : [{
        body.base64Bytes.base64.string= 'hello avatar'
      }]
      """
      Examples:
        | method |
        | PATCH  |

    Scenario Outline: <method> with binary body of external file
      Given an external file "/tmp/restful.txt":
      """
      hello avatar
      """
      When <method> "/index":
      """application/octet-stream
      '/tmp/restful.txt'.file
      """
      Then "http://www.a.com" got a "<method>" request on "/index" with body matching
      """
      : [{
        body.base64Bytes.base64.string= 'hello avatar'
      }]
      """
      Examples:
        | method |
        | PATCH  |

    Scenario Outline: <method> with binary body of external path
      Given an external file "/tmp/restful.txt":
      """
      hello avatar
      """
      When <method> "/index":
      """application/octet-stream
      '/tmp/restful.txt'.path
      """
      Then "http://www.a.com" got a "<method>" request on "/index" with body matching
      """
      : [{
        body.base64Bytes.base64.string= 'hello avatar'
      }]
      """
      Examples:
        | method |
        | PATCH  |
