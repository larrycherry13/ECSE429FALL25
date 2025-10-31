package com.ecse429.todoapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class TodoSteps {

    private final ApiClient api;
    private final SharedContext ctx;

    public TodoSteps(SharedContext ctx) {
        this.api = new ApiClient();
        this.ctx = ctx;
    }

    // ---------- GIVENs ----------
    @Given("the todo API service is running")
    public void the_service_is_running() throws IOException, InterruptedException {
        HttpResponse<String> res = api.get("/todos");
        assertTrue(res.statusCode() < 500, "API service not reachable.");
    }

    @Given("the system is reset to a clean initial state")
    public void system_reset() {
        ctx.reset();
    }

    // ---------- WHENs ----------
    @When("I create a todo with title {string} and description {string}")
    public void create_todo_with_title_and_description(String title, String desc) throws Exception {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, desc);
        HttpResponse<String> res = api.post("/todos", body);
        ctx.setLastResponse(res);
        String id = ctx.extractIdFromTodoJson(res.body());
        ctx.setLastCreatedTodoId(id);
        ctx.setLastCreatedTodoTitle(title);
        ctx.setLastCreatedTodoDescription(desc);
        ctx.trackCreatedTodoId(id);
    }

    @When("I create a todo with only title {string}")
    public void create_todo_with_only_title(String title) throws Exception {
        String body = String.format("{\"title\":\"%s\"}", title);
        HttpResponse<String> res = api.post("/todos", body);
        ctx.setLastResponse(res);
        String id = ctx.extractIdFromTodoJson(res.body());
        ctx.setLastCreatedTodoId(id);
        ctx.setLastCreatedTodoTitle(title);
        ctx.setLastCreatedTodoDescription(""); // empty description
        ctx.trackCreatedTodoId(id);
    }

    @When("I try to create a todo with malformed JSON")
    public void create_todo_with_malformed_json() throws Exception {
        HttpResponse<String> res = api.post("/todos", "{title:\"missingQuotes}");
        ctx.setLastResponse(res);
    }

    @When("I retrieve that todo by its id")
    public void get_todo_by_id() throws Exception {
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId());
        ctx.setLastResponse(res);
    }

    @When("I update that todo description to {string}")
    public void update_todo_description(String newDesc) throws Exception {
        // Include all fields: title, description, and doneStatus 
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\", \"doneStatus\": false}", 
                                   ctx.getLastCreatedTodoTitle(), 
                                   newDesc);
        HttpResponse<String> res = api.put("/todos/" + ctx.getLastCreatedTodoId(), body);
        ctx.setLastResponse(res);
    }

    @When("I mark that todo as completed")
    public void mark_todo_completed() throws Exception {
        // Include all fields: title, description, and doneStatus (boolean true)
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\", \"doneStatus\": true}", 
                                   ctx.getLastCreatedTodoTitle(), 
                                   ctx.getLastCreatedTodoDescription());
        HttpResponse<String> res = api.put("/todos/" + ctx.getLastCreatedTodoId(), body);
        ctx.setLastResponse(res);
    }

    @When("I delete that todo")
    public void delete_that_todo() throws Exception {
        HttpResponse<String> res = api.delete("/todos/" + ctx.getLastCreatedTodoId());
        ctx.setLastResponse(res);
    }

    @When("I delete a todo that does not exist")
    public void delete_nonexistent_todo() throws Exception {
        HttpResponse<String> res = api.delete("/todos/999999");
        ctx.setLastResponse(res);
    }

    // ---------- THENs ----------
    @Then("the response status should be {int}")
    public void response_status_should_be(Integer code) {
        assertEquals(code.intValue(), ctx.getLastResponse().statusCode(),
                "Expected status " + code + " but got " + ctx.getLastResponse().statusCode());
    }

    @Then("that todo should be retrievable by its id")
    public void todo_should_be_retrievable() throws Exception {
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId());
        assertEquals(200, res.statusCode(), "Todo not retrievable");
        assertTrue(res.body().contains(ctx.getLastCreatedTodoId()), "Todo ID missing in response");
    }

    @Then("the todo should have doneStatus true")
    public void todo_done_status_true() throws Exception {
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId());
        // Check for different possible formats of doneStatus true
        boolean hasDoneStatusTrue = res.body().contains("\"doneStatus\": true") || 
                                   res.body().contains("\"doneStatus\":true") ||
                                   res.body().contains("\"doneStatus\": \"true\"") ||
                                   res.body().contains("\"doneStatus\":\"true\"");
        assertTrue(hasDoneStatusTrue, "doneStatus not true. Response: " + res.body());
    }

    @Then("no new todo should have been created")
    public void no_new_todo_created() {
        String body = ctx.getLastResponse().body();
        boolean hasId = body != null && body.contains("\"id\"");
        assertFalse(hasId, "Unexpected ID found; todo was created unexpectedly.");
    }

    @Then("that todo should no longer exist")
    public void todo_should_not_exist() throws Exception {
        HttpResponse<String> res = api.get("/todos/" + ctx.getLastCreatedTodoId());
        assertEquals(404, res.statusCode(), "Expected 404 after deletion");
    }
}