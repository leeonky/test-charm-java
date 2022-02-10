Feature: reset RESTful states

  Background:
    Given base url "http://www.a.com"

  Scenario: given response
    Given response 200 on GET "/index":
    """
    Hello world
    """
    When GET "/index"

  Scenario: should no response
    Then response should be:
    """
    : null
    """

#  TODO reset RESTfulStep response
