Feature: Update Category
  As a team member
  I want to update category descriptions
  So that they stay accurate and relevant

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - update description successfully
    When I create a category with title "Office" and description "Office tasks"
    And I update that category description to "Updated office tasks"
    Then the response status should be 200

  Scenario: Alternate flow - update with same description
    When I create a category with title "Office" and description "Office tasks"
    And I update that category description to "Office tasks"
    Then the response status should be 200

  Scenario: Error flow - update invalid ID
    When I update a non-existing category with title "Office" and description to "Office tasks"
    Then the response status should be 404