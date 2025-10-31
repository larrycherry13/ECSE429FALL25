Feature: Update Todo
  As a team member
  I want to update todo descriptions
  So that they stay accurate

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - update description successfully
    When I create a todo with title "Report" and description "Initial draft"
    And I update that todo description to "Final version"
    Then the response status should be 200

  Scenario: Alternate flow - update with same description
    When I create a todo with title "Redundant" and description "Same desc"
    And I update that todo description to "Same desc"
    Then the response status should be 200

  Scenario: Error flow - update invalid ID
    When I update that todo description to "Should fail"
    Then the response status should be 404