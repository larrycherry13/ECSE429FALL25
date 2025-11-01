package com.ecse429.todoapi.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class CategorySteps {

    private final ApiClient api;
    private final SharedContext ctx;

    public CategorySteps(SharedContext ctx) {
        this.api = new ApiClient();
        this.ctx = ctx;
    }

    // ---------- WHENs for CREATE ----------
    @When("I create a category with title {string} and description {string}")
    public void create_category_with_title_and_description(String title, String desc) throws Exception {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, desc);
        HttpResponse<String> res = api.post("/categories", body);
        ctx.setLastResponse(res);
        if (res.statusCode() == 201) {
            String id = ctx.extractIdFromCategoryJson(res.body());
            ctx.setLastCreatedCategoryId(id);
            ctx.setLastCreatedCategoryTitle(title);
            ctx.setLastCreatedCategoryDescription(desc);
            ctx.trackCreatedCategoryId(id);
        }
    }

    @When("I create a category with only title {string}")
    public void create_category_with_only_title(String title) throws Exception {
        String body = String.format("{\"title\":\"%s\"}", title);
        HttpResponse<String> res = api.post("/categories", body);
        ctx.setLastResponse(res);
        if (res.statusCode() == 201) {
            String id = ctx.extractIdFromCategoryJson(res.body());
            ctx.setLastCreatedCategoryId(id);
            ctx.setLastCreatedCategoryTitle(title);
            ctx.setLastCreatedCategoryDescription(""); // empty description
            ctx.trackCreatedCategoryId(id);
        }
    }

    @When("I try to create a category without providing a title")
    public void create_category_without_title() throws Exception {
        String body = "{\"description\":\"no title provided\"}";
        HttpResponse<String> res = api.post("/categories", body);
        ctx.setLastResponse(res);
    }

    // ---------- WHENs for UPDATE ----------
    @When("I update that category description to {string}")
    public void update_category_description(String newDesc) throws Exception {
        if (ctx.getLastCreatedCategoryId() == null) {
            // No category was created, so this should fail
            HttpResponse<String> res = api.put("/categories/100", 
                String.format("{\"title\":\"Office\", \"description\":\"%s\"}", newDesc));
            ctx.setLastResponse(res);
        } else {
            String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", 
                                       ctx.getLastCreatedCategoryTitle(), 
                                       newDesc);
            HttpResponse<String> res = api.put("/categories/" + ctx.getLastCreatedCategoryId(), body);
            ctx.setLastResponse(res);
        }
    }

    @When("I update a non-existing category with title {string} and description to {string}")
    public void update_nonexisting_category(String title, String desc) throws Exception {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, desc);
        HttpResponse<String> res = api.put("/categories/100", body);
        ctx.setLastResponse(res);
    }

    // ---------- WHENs for DELETE ----------
    @When("I delete that category")
    public void delete_that_category() throws Exception {
        if (ctx.getLastCreatedCategoryId() == null) {
            // No category was created, try to delete a non-existent one
            HttpResponse<String> res = api.delete("/categories/100");
            ctx.setLastResponse(res);
        } else {
            // Store the ID before deleting, in case we need to delete again
            String categoryIdToDelete = ctx.getLastCreatedCategoryId();
            HttpResponse<String> res = api.delete("/categories/" + categoryIdToDelete);
            ctx.setLastResponse(res);
            // Don't clear the ID so we can attempt to delete the same category again if needed
        }
    }

    @When("I delete a category that does not exist")
    public void delete_nonexistent_category() throws Exception {
        HttpResponse<String> res = api.delete("/categories/100");
        ctx.setLastResponse(res);
    }

    // ---------- WHENs for VIEW ALL ----------
    @When("I retrieve all categories")
    public void retrieve_all_categories() throws Exception {
        HttpResponse<String> res = api.get("/categories");
        ctx.setLastResponse(res);
    }

    // ---------- WHENs for VIEW TODOS OF CATEGORY ----------
    @When("I create a todo with title {string} and description {string} through the category endpoint")
    public void create_todo_through_category_endpoint(String title, String desc) throws Exception {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, desc);
        HttpResponse<String> res = api.post("/categories/" + ctx.getLastCreatedCategoryId() + "/todos", body);
        ctx.setLastResponse(res);
        if (res.statusCode() == 201) {
            String id = ctx.extractIdFromTodoJson(res.body());
            ctx.setLastCreatedTodoId(id);
            ctx.trackCreatedTodoId(id);
        }
    }

    @When("I retrieve all todos for that category")
    public void retrieve_todos_for_category() throws Exception {
        HttpResponse<String> res = api.get("/categories/" + ctx.getLastCreatedCategoryId() + "/todos");
        ctx.setLastResponse(res);
    }

    @When("I retrieve all todos for a category that does not exist")
    public void retrieve_todos_for_nonexistent_category() throws Exception {
        HttpResponse<String> res = api.get("/categories/100/todos");
        ctx.setLastResponse(res);
    }

    // ---------- THENs ----------
    @Then("that category should be retrievable by its id")
    public void category_should_be_retrievable() throws Exception {
        HttpResponse<String> res = api.get("/categories/" + ctx.getLastCreatedCategoryId());
        assertEquals(200, res.statusCode(), "Category not retrievable");
        assertTrue(res.body().contains(ctx.getLastCreatedCategoryId()), "Category ID missing in response");
    }

    @Then("no new category should have been created")
    public void no_new_category_created() {
        String body = ctx.getLastResponse().body();
        boolean hasId = body != null && body.contains("\"id\"");
        assertFalse(hasId, "Unexpected ID found; category was created unexpectedly.");
    }

    @Then("that category should no longer exist")
    public void category_should_not_exist() throws Exception {
        HttpResponse<String> res = api.get("/categories/" + ctx.getLastCreatedCategoryId());
        assertEquals(404, res.statusCode(), "Expected 404 after deletion");
    }

    @Then("the response should contain at least {int} categories")
    public void response_should_contain_at_least_n_categories(Integer minCount) {
        String body = ctx.getLastResponse().body();
        int count = ctx.countCategoriesInJson(body);
        assertTrue(count >= minCount, 
            String.format("Expected at least %d categories but found %d", minCount, count));
    }

    @Then("the response should contain at least {int} todo")
    public void response_should_contain_at_least_n_todo(Integer minCount) {
        String body = ctx.getLastResponse().body();
        int count = ctx.countTodosInJson(body);
        assertTrue(count >= minCount, 
            String.format("Expected at least %d todos but found %d", minCount, count));
    }

    @Then("the response should contain {int} todos")
    public void response_should_contain_exact_n_todos(Integer exactCount) {
        String body = ctx.getLastResponse().body();
        int count = ctx.countTodosInJson(body);
        assertEquals(exactCount.intValue(), count, 
            String.format("Expected exactly %d todos but found %d", exactCount, count));
    }

    @Then("the response should contain no todos")
    public void response_should_contain_no_todos() {
        String body = ctx.getLastResponse().body();
        int count = ctx.countTodosInJson(body);
        assertEquals(0, count, 
            String.format("Expected no todos but found %d", count));
    }
}