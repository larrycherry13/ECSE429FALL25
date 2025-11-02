Feature: View All Categories
  As a user
  I want to view all categories
  So that I can see all possible categories

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - retrieve all categories when multiple exist
    When I create a category with title "Work" and description "Work tasks"
    And I create a category with title "Home" and description "Home tasks"
    And I retrieve all categories
    Then the response status should be 200
    And the response should contain at least 2 categories

  Scenario: Alternate flow - retrieve all categories when system has defaults
    When I retrieve all categories
    Then the response status should be 200