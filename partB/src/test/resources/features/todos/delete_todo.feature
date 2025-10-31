Feature: Delete Todo
  As a project manager
  I want to remove completed or outdated todos
  So that my task list stays clean

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - delete existing todo
    When I create a todo with title "Temp" and description "to be deleted"
    And I delete that todo
    Then the response status should be 200
    And that todo should no longer exist

  Scenario: Alternate flow - delete same todo twice
    When I delete that todo
    Then the response status should be 404

  Scenario: Error flow - delete invalid todo id
    When I delete a todo that does not exist
    Then the response status should be 404