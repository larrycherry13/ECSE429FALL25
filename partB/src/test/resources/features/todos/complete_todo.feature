Feature: Complete Todo
  As a project manager
  I want to mark todos as completed
  So that I can track progress

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - mark todo completed
    When I create a todo with title "Laundry" and description "Do laundry"
    And I mark that todo as completed
    Then the response status should be 200
    And the todo should have doneStatus true

  Scenario: Alternate flow - mark completed again
    When I create a todo with title "Laundry" and description "Do laundry"
    And I mark that todo as completed
    And I mark that todo as completed
    Then the response status should be 200

  Scenario: Error flow - mark invalid todo
    When I delete a todo that does not exist
    Then the response status should be 404