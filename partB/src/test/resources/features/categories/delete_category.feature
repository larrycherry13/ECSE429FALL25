Feature: Delete Category
  As a user
  I want to remove obsolete categories
  So that my organization stays clean

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - delete existing category
    When I create a category with title "Temp" and description "to be deleted"
    And I delete that category
    Then the response status should be 200
    And that category should no longer exist

  Scenario: Alternate flow - delete same category twice
    When I create a category with title "Temp" and description "to be deleted"
    And I delete that category
    And I delete that category
    Then the response status should be 404

  Scenario: Error flow - delete invalid category id
    When I delete a category that does not exist
    Then the response status should be 404