Feature: Create Todo
  As a project manager
  I want to create new todos
  So that I can track upcoming tasks

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario Outline: Normal flow - create todo with valid title and description
    When I create a todo with title "<title>" and description "<description>"
    Then the response status should be 201
    And that todo should be retrievable by its id
  Examples:
    | title      | description     |
    | Homework   | Finish math hw  |
    | Groceries  | Buy vegetables  |

  Scenario Outline: Alternate flow - create todo with only title
    When I create a todo with only title "<title>"
    Then the response status should be 201
    And that todo should be retrievable by its id
  Examples:
    | title        |
    | QuickTask    |
    | EmptyDesc    |

  Scenario: Error flow - malformed JSON request
    When I try to create a todo with malformed JSON
    Then the response status should be 400
    And no new todo should have been created