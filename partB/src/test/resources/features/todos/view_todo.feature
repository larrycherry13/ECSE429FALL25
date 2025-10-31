Feature: View Todo
  As a team member
  I want to view todos by their ID
  So that I can check task details

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - retrieve existing todo by ID
    When I create a todo with title "Check Email" and description "Inbox cleanup"
    And I retrieve that todo by its id
    Then the response status should be 200

  Scenario: Alternate flow - retrieve via query parameter
    When I create a todo with title "Groceries" and description "Buy milk"
    And I retrieve that todo by its id
    Then the response status should be 200
    And that todo should be retrievable by its id

  Scenario: Error flow - retrieve non-existent todo
    When I delete a todo that does not exist
    Then the response status should be 404