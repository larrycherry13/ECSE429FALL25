package com.ecse429.todoapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class InteropSteps {

    private final ApiClient api;
    private final SharedContext ctx;

    // Store first project/category for "move" scenarios
    private String firstProjectId;
    private String firstCategoryId;

    public InteropSteps(SharedContext ctx) {
        this.api = new ApiClient();
        this.ctx = ctx;
    }

    // ==================== GIVEN Steps ====================

    @Given("a todo exists with title {string}")
    public void a_todo_exists_with_title(String title) throws Exception {
        String body = String.format("{\"title\":\"%s\"}", title);
        HttpResponse<String> res = api.post("/todos", body);
        String id = ctx.extractIdFromTodoJson(res.body());
        ctx.setLastCreatedTodoId(id);
        ctx.setLastCreatedTodoTitle(title);
        ctx.trackCreatedTodoId(id);
    }

    @Given("a category exists with title {string}")
    public void a_category_exists_with_title(String title) throws Exception {
        String body = String.format("{\"title\":\"%s\"}", title);
        HttpResponse<String> res = api.post("/categories", body);
        String id = ctx.extractIdFromCategoryJson(res.body());
        ctx.setLastCreatedCategoryId(id);
        ctx.setLastCreatedCategoryTitle(title);
        ctx.trackCreatedCategoryId(id);
    }

    @Given("a project exists with title {string}")
    public void a_project_exists_with_title(String title) throws Exception {
        String body = String.format("{\"title\":\"%s\"}", title);
        HttpResponse<String> res = api.post("/projects", body);
        String id = ctx.extractIdFromProjectJson(res.body());
        ctx.setLastCreatedProjectId(id);
        ctx.setLastCreatedProjectTitle(title);
        ctx.trackCreatedProjectId(id);
    }

    @Given("that todo is linked to that project")
    public void that_todo_is_linked_to_that_project() throws Exception {
        // Save first project ID before linking
        firstProjectId = ctx.getLastCreatedProjectId();

        String body = String.format("{\"id\":\"%s\"}", ctx.getLastCreatedProjectId());
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof", body);
        ctx.setLastResponse(res);
    }

    @Given("that todo is linked to that category")
    public void that_todo_is_linked_to_that_category() throws Exception {
        // Save first category ID before linking
        firstCategoryId = ctx.getLastCreatedCategoryId();

        String body = String.format("{\"id\":\"%s\"}", ctx.getLastCreatedCategoryId());
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/categories", body);
        ctx.setLastResponse(res);
    }

    @Given("another project exists with title {string}")
    public void another_project_exists_with_title(String title) throws Exception {
        String body = String.format("{\"title\":\"%s\"}", title);
        HttpResponse<String> res = api.post("/projects", body);
        String id = ctx.extractIdFromProjectJson(res.body());
        // This becomes the new "last created" project
        ctx.setLastCreatedProjectId(id);
        ctx.setLastCreatedProjectTitle(title);
        ctx.trackCreatedProjectId(id);
    }

    @Given("another category exists with title {string}")
    public void another_category_exists_with_title(String title) throws Exception {
        String body = String.format("{\"title\":\"%s\"}", title);
        HttpResponse<String> res = api.post("/categories", body);
        String id = ctx.extractIdFromCategoryJson(res.body());
        // This becomes the new "last created" category
        ctx.setLastCreatedCategoryId(id);
        ctx.setLastCreatedCategoryTitle(title);
        ctx.trackCreatedCategoryId(id);
    }

    // ==================== WHEN Steps - Linking ====================

    @When("I link that todo to that project")
    public void link_todo_to_project() throws Exception {
        String body = String.format("{\"id\":\"%s\"}", ctx.getLastCreatedProjectId());
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof", body);
        ctx.setLastResponse(res);
    }

    @When("I link that todo to that category")
    public void link_todo_to_category() throws Exception {
        String body = String.format("{\"id\":\"%s\"}", ctx.getLastCreatedCategoryId());
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/categories", body);
        ctx.setLastResponse(res);
    }

    @When("I link that todo to that project again")
    public void link_todo_to_project_again() throws Exception {
        String body = String.format("{\"id\":\"%s\"}", ctx.getLastCreatedProjectId());
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof", body);
        ctx.setLastResponse(res);
    }

    @When("I link that todo to the second project")
    public void link_todo_to_second_project() throws Exception {
        // Last created project is the second one
        String body = String.format("{\"id\":\"%s\"}", ctx.getLastCreatedProjectId());
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof", body);
        ctx.setLastResponse(res);
    }

    @When("I link that todo to the second category")
    public void link_todo_to_second_category() throws Exception {
        // Last created category is the second one
        String body = String.format("{\"id\":\"%s\"}", ctx.getLastCreatedCategoryId());
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/categories", body);
        ctx.setLastResponse(res);
    }

    // ==================== WHEN Steps - Unlinking ====================

    @When("I unlink that todo from the first project")
    public void unlink_todo_from_first_project() throws Exception {
        HttpResponse<String> res = api.delete("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof/" + firstProjectId);
        ctx.setLastResponse(res);
    }

    @When("I unlink that todo from the first category")
    public void unlink_todo_from_first_category() throws Exception {
        HttpResponse<String> res = api.delete("/todos/" + ctx.getLastCreatedTodoId() + "/categories/" + firstCategoryId);
        ctx.setLastResponse(res);
    }

    // ==================== WHEN Steps - Error Cases ====================

    @When("I try to link that todo to a nonexistent project")
    public void link_todo_to_nonexistent_project() throws Exception {
        String body = "{\"id\":\"9999999\"}";
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof", body);
        ctx.setLastResponse(res);
    }

    @When("I try to link that todo to a nonexistent category")
    public void link_todo_to_nonexistent_category() throws Exception {
        String body = "{\"id\":\"9999999\"}";
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/categories", body);
        ctx.setLastResponse(res);
    }

    @When("I try to link that todo with malformed JSON")
    public void link_todo_with_malformed_json() throws Exception {
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof", "{id:missingQuotes}");
        ctx.setLastResponse(res);
    }

    @When("I try to link that todo with empty JSON")
    public void link_todo_with_empty_json() throws Exception {
        HttpResponse<String> res = api.post("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof", "{}");
        ctx.setLastResponse(res);
    }

    // ==================== WHEN Steps - Retrieve Relationships ====================

    @When("I retrieve all tasks for that project")
    public void retrieve_tasks_for_project() throws Exception {
        HttpResponse<String> res = api.get("/projects/" + ctx.getLastCreatedProjectId() + "/tasks");
        ctx.setLastResponse(res);
    }

    @When("I retrieve all categories for that todo")
    public void retrieve_categories_for_todo() throws Exception {
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId() + "/categories");
        ctx.setLastResponse(res);
    }

    @When("I retrieve all projects for that todo")
    public void retrieve_projects_for_todo() throws Exception {
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof");
        ctx.setLastResponse(res);
    }

    @When("I try to retrieve tasks for a nonexistent project")
    public void retrieve_tasks_for_nonexistent_project() throws Exception {
        HttpResponse<String> res = api.get("/projects/9999999/tasks");
        ctx.setLastResponse(res);
    }

    // ==================== WHEN Steps - Deletion ====================
    // Note: "I delete that category" is already defined in CategorySteps
    // We only need project deletion here since ProjectSteps doesn't exist yet

    @When("I delete that project")
    public void delete_that_project() throws Exception {
        HttpResponse<String> res = api.delete("/projects/" + ctx.getLastCreatedProjectId());
        ctx.setLastResponse(res);
    }

    // ==================== THEN Steps - Assertions ====================

    @Then("the todo should be linked to the project")
    public void todo_should_be_linked_to_project() throws Exception {
        // Verify forward link: todo -> project
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof");
        assertTrue(res.body().contains(ctx.getLastCreatedProjectId()),
            "Todo not linked to project. Response: " + res.body());
    }

    @Then("the project should contain that todo in its tasks")
    public void project_should_contain_todo() throws Exception {
        // Verify reverse link: project -> todo
        HttpResponse<String> res = api.get("/projects/" + ctx.getLastCreatedProjectId() + "/tasks");
        assertTrue(res.body().contains(ctx.getLastCreatedTodoId()),
            "Project does not contain todo in tasks. Response: " + res.body());
    }

    @Then("the todo should be linked to the category")
    public void todo_should_be_linked_to_category() throws Exception {
        // Verify forward link: todo -> category
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId() + "/categories");
        assertTrue(res.body().contains(ctx.getLastCreatedCategoryId()),
            "Todo not linked to category. Response: " + res.body());
    }

    @Then("the todo should be linked to the second project")
    public void todo_should_be_linked_to_second_project() throws Exception {
        // Verify todo is linked to current (second) project
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId() + "/tasksof");
        assertTrue(res.body().contains(ctx.getLastCreatedProjectId()),
            "Todo not linked to second project. Response: " + res.body());
    }

    @Then("the first project should not contain that todo")
    public void first_project_should_not_contain_todo() throws Exception {
        // Verify first project does NOT contain the todo
        HttpResponse<String> res = api.get("/projects/" + firstProjectId + "/tasks");
        assertFalse(res.body().contains(ctx.getLastCreatedTodoId()),
            "First project still contains todo. Response: " + res.body());
    }

    @Then("the todo should be linked to the second category")
    public void todo_should_be_linked_to_second_category() throws Exception {
        // Verify todo is linked to current (second) category
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId() + "/categories");
        assertTrue(res.body().contains(ctx.getLastCreatedCategoryId()),
            "Todo not linked to second category. Response: " + res.body());
    }

    @Then("the response status should not be {int}")
    public void response_status_should_not_be(Integer code) {
        assertNotEquals(code.intValue(), ctx.getLastResponse().statusCode(),
            "Expected status NOT to be " + code + " but got " + ctx.getLastResponse().statusCode());
    }

    @Then("the response should contain at least {int} category")
    public void response_should_contain_at_least_n_category(Integer minCount) {
        String body = ctx.getLastResponse().body();
        int count = ctx.countCategoriesInJson(body);
        assertTrue(count >= minCount,
            String.format("Expected at least %d categories but found %d", minCount, count));
    }

    @Then("the response should contain at least {int} project")
    public void response_should_contain_at_least_n_project(Integer minCount) {
        String body = ctx.getLastResponse().body();
        // Use same counting logic as categories (both count "id" fields)
        int count = ctx.countCategoriesInJson(body);
        assertTrue(count >= minCount,
            String.format("Expected at least %d projects but found %d", minCount, count));
    }

    @Then("that todo should still exist as an orphan")
    public void todo_should_still_exist_as_orphan() throws Exception {
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId());
        assertEquals(200, res.statusCode(), "Orphaned todo should still exist");
        assertTrue(res.body().contains(ctx.getLastCreatedTodoId()),
            "Orphaned todo ID missing in response");
    }

    @Then("the orphaned todo should be accessible")
    public void orphaned_todo_should_be_accessible() throws Exception {
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId());
        assertEquals(200, res.statusCode(),
            "Orphaned todo should be accessible. Got status: " + res.statusCode());
    }
}
