Feature: Interoperability stories
  As a project manager
  I want todos, projects, and categories to work together
  So that my data stays consistent

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  # NOTE for Interoperability:
  # After linking a todo to a project/category, deleting the project/category
  # does NOT delete the todo. The todo survives (orphan).
  # This will be captured in Error or Alternate flows.

  # Normal flow Scenario Outline (link todo to project/category)
  # Alternate flow Scenario Outline (re-link / move)
  # Error flow Scenario Outline (delete project/category and check orphaned todo)