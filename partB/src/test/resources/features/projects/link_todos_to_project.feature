Feature: Link Todos to Project
  As a team lead
  I want to link todos to projects
  So that I can track tasks within projects

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - link todo to project
    When I create a project with title "Backend" and description "API development"
    And I create a todo with title "Write tests" and description "Unit tests"
    And I link todo id "1" to that project
    Then the response status should be 201

  Scenario: Alternate flow - link multiple todos to same project
    When I create a project with title "Frontend" and description "UI work"
    And I create a todo with title "Task1" and description "First task"
    And I create a todo with title "Task2" and description "Second task"
    And I link todo id "1" to that project
    And I link todo id "2" to that project
    Then the response status should be 201

  Scenario: Error flow - link non-existent todo
    When I create a project with title "Test" and description "Testing"
    And I link todo id "99999" to that project
    Then the response status should be 404
