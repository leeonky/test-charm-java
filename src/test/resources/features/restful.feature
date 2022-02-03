Feature: RESTful api steps

  Background:
    Given base url "http://www.a.com"

  Scenario: get with no params
    When GET "/index"
    Then "http://www.a.com" got a GET request on "/index"

  Scenario: get with header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": "value2"
    }
    """
#      "list": ["value1", "value2"]
    When GET "/index"
    Then got request:
    """
    : [{
      method: 'GET'
      path: '/index'
      headers: {
        key1: ['value1']
        key2: ['value2']
      }
    }]
    """
    And "http://www.a.com" got a GET request on "/index"
#  list: ['value1'  'value2']
#  TODO multi header
#  TODO reset RESTfulStep request and response