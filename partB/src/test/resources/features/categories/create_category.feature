Feature: Create Category
  As a project manager
  I want to create new categories
  So that I can organize todos and projects

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario Outline: Normal flow - create category with valid title and description
    When I create a category with title "<title>" and description "<description>"
    Then the response status should be 201
    And that category should be retrievable by its id
  Examples:
    | title      | description         |
    | Work       | Work tasks          |
    | Personal   | Personal tasks      |

  Scenario Outline: Alternate flow - create category with only title
    When I create a category with only title "<title>"
    Then the response status should be 201
    And that category should be retrievable by its id
  Examples:
    | title        |
    | Work         |
    | Personal     |

  Scenario: Error flow - create category without title
    When I try to create a category without providing a title
    Then the response status should be 400
    And no new category should have been created