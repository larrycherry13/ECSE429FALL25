Feature: View Todos of Category
  As a user
  I want to view all todos associated with a category
  So that I can see related tasks

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - retrieve todos of a category with associated todos
    When I create a category with title "Work" and description "Work tasks"
    And I create a todo with title "Meeting" and description "Team meeting" through the category endpoint
    And I retrieve all todos for that category
    Then the response status should be 200
    And the response should contain at least 1 todo

  Scenario: Alternate flow - retrieve todos of a category with no associated todos
    When I create a category with title "Work" and description "Work tasks"
    And I retrieve all todos for that category
    Then the response status should be 200
    And the response should contain 0 todos

  Scenario: Error flow - retrieve todos for non-existent category returns no todos
    When I retrieve all todos for a category that does not exist
    Then the response status should be 200
    And the response should contain no todos