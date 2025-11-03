package com.ecse429.todoapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectSteps {

    private final ApiClient api;
    private final SharedContext ctx;

    public ProjectSteps(SharedContext ctx) {
        this.api = new ApiClient();
        this.ctx = ctx;
    }

    // ---------- WHENs ----------
    @When("I create a project with title {string} and description {string}")
    public void create_project_with_title_and_description(String title, String desc) throws Exception {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, desc);
        HttpResponse<String> res = api.post("/projects", body);
        ctx.setLastResponse(res);
        String id = ctx.extractIdFromProjectJson(res.body());
        ctx.setLastCreatedProjectId(id);
        ctx.setLastCreatedProjectTitle(title);
        ctx.setLastCreatedProjectDescription(desc);
        ctx.trackCreatedProjectId(id);
    }

    @When("I create a project with only title {string}")
    public void create_project_with_only_title(String title) throws Exception {
        String body = String.format("{\"title\":\"%s\"}", title);
        HttpResponse<String> res = api.post("/projects", body);
        ctx.setLastResponse(res);
        String id = ctx.extractIdFromProjectJson(res.body());
        ctx.setLastCreatedProjectId(id);
        ctx.setLastCreatedProjectTitle(title);
        ctx.setLastCreatedProjectDescription("");
        ctx.trackCreatedProjectId(id);
    }

    @When("I try to create a project with malformed JSON")
    public void create_project_with_malformed_json() throws Exception {
        HttpResponse<String> res = api.post("/projects", "{title:\"missingQuotes}");
        ctx.setLastResponse(res);
    }

    @When("I retrieve that project by its id")
    public void get_project_by_id() throws Exception {
        HttpResponse<String> res = api.get("/projects/" + ctx.getLastCreatedProjectId());
        ctx.setLastResponse(res);
    }

    @When("I update that project description to {string}")
    public void update_project_description(String newDesc) throws Exception {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\", \"completed\": false}",
                                   ctx.getLastCreatedProjectTitle(),
                                   newDesc);
        HttpResponse<String> res = api.put("/projects/" + ctx.getLastCreatedProjectId(), body);
        ctx.setLastResponse(res);
    }

    @When("I mark that project as completed")
    public void mark_project_completed() throws Exception {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\", \"completed\": true}",
                                   ctx.getLastCreatedProjectTitle(),
                                   ctx.getLastCreatedProjectDescription());
        HttpResponse<String> res = api.put("/projects/" + ctx.getLastCreatedProjectId(), body);
        ctx.setLastResponse(res);
    }


    @When("I delete a project that does not exist")
    public void delete_nonexistent_project() throws Exception {
        HttpResponse<String> res = api.delete("/projects/999999");
        ctx.setLastResponse(res);
    }

    @When("I link todo id {string} to that project")
    public void link_todo_to_project(String todoId) throws Exception {
        String body = String.format("{\"id\":\"%s\"}", todoId);
        HttpResponse<String> res = api.post("/projects/" + ctx.getLastCreatedProjectId() + "/tasks", body);
        ctx.setLastResponse(res);
    }

    @When("I retrieve tasks for that project")
    public void get_tasks_for_project() throws Exception {
        HttpResponse<String> res = api.get("/projects/" + ctx.getLastCreatedProjectId() + "/tasks");
        ctx.setLastResponse(res);
    }

    // ---------- THENs ----------
    @Then("that project should be retrievable by its id")
    public void project_should_be_retrievable() throws Exception {
        HttpResponse<String> res = api.get("/projects/" + ctx.getLastCreatedProjectId());
        assertEquals(200, res.statusCode(), "Project not retrievable");
        assertTrue(res.body().contains(ctx.getLastCreatedProjectId()), "Project ID missing in response");
    }

    @Then("the project should have completed status true")
    public void project_completed_status_true() throws Exception {
        HttpResponse<String> res = api.get("/projects/" + ctx.getLastCreatedProjectId());
        boolean hasCompletedTrue = res.body().contains("\"completed\": true") ||
                                   res.body().contains("\"completed\":true") ||
                                   res.body().contains("\"completed\": \"true\"") ||
                                   res.body().contains("\"completed\":\"true\"");
        assertTrue(hasCompletedTrue, "completed status not true. Response: " + res.body());
    }

    @Then("no new project should have been created")
    public void no_new_project_created() {
        String body = ctx.getLastResponse().body();
        boolean hasId = body != null && body.contains("\"id\"");
        assertFalse(hasId, "Unexpected ID found; project was created unexpectedly.");
    }

    @Then("that project should no longer exist")
    public void project_should_not_exist() throws Exception {
        HttpResponse<String> res = api.get("/projects/" + ctx.getLastCreatedProjectId());
        assertEquals(404, res.statusCode(), "Expected 404 after deletion");
    }

    @Then("the response should contain that todo")
    public void response_should_contain_todo() {
        String body = ctx.getLastResponse().body();
        assertNotNull(body, "Response body is null");
        assertTrue(body.contains("\"id\""), "Response should contain todo data");
    }
}
