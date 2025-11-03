Feature: Update Project
  As a user
  I want to update project descriptions
  So that project details stay current

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - update description successfully
    When I create a project with title "Website" and description "Initial specs"
    And I update that project description to "Revised requirements"
    Then the response status should be 200

  Scenario: Alternate flow - update with same description
    When I create a project with title "Refactor" and description "Code cleanup"
    And I update that project description to "Code cleanup"
    Then the response status should be 200

  Scenario: Error flow - update invalid ID
    When I update that project description to "Should fail"
    Then the response status should be 404
