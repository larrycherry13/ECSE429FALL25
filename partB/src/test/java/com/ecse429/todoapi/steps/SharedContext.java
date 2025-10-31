package com.ecse429.todoapi.steps;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * SharedContext:
 * - stores data created in a scenario
 * - stores last API response
 * - lets Hooks clean up after each scenario
 *
 * Each scenario gets a fresh SharedContext.
 */
public class SharedContext {

    // last HTTP response we got from the API
    private HttpResponse<String> lastResponse;

    // the last todo id we created successfully
    private String lastCreatedTodoId;

    // store the last created todo's title and description for updates
    private String lastCreatedTodoTitle;
    private String lastCreatedTodoDescription;

    // list of ALL todos we created in this scenario (so we can delete them in @After)
    private final List<String> createdTodoIds = new ArrayList<>();

    public HttpResponse<String> getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(HttpResponse<String> lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getLastCreatedTodoId() {
        return lastCreatedTodoId;
    }

    public void setLastCreatedTodoId(String lastCreatedTodoId) {
        this.lastCreatedTodoId = lastCreatedTodoId;
    }

    public String getLastCreatedTodoTitle() {
        return lastCreatedTodoTitle;
    }

    public void setLastCreatedTodoTitle(String lastCreatedTodoTitle) {
        this.lastCreatedTodoTitle = lastCreatedTodoTitle;
    }

    public String getLastCreatedTodoDescription() {
        return lastCreatedTodoDescription;
    }

    public void setLastCreatedTodoDescription(String lastCreatedTodoDescription) {
        this.lastCreatedTodoDescription = lastCreatedTodoDescription;
    }

    public void trackCreatedTodoId(String id) {
        if (id != null && !id.isEmpty()) {
            createdTodoIds.add(id);
        }
    }

    public List<String> getCreatedTodoIds() {
        return createdTodoIds;
    }

    // wipe all scenario info
    public void reset() {
        lastResponse = null;
        lastCreatedTodoId = null;
        lastCreatedTodoTitle = null;
        lastCreatedTodoDescription = null;
        createdTodoIds.clear();
    }

    // ---- helpers to parse JSON bodies in a lazy/simple way ----
    //
    // We are NOT doing full JSON parsing here because the assignment
    // doesn't grade JSON libraries. We'll just do naive substringing
    // to pull "id" out of API responses like:
    // { "id": "1", "title": "something", ... }
    //
    // NOTE: this assumes the API returns "id": "123" as a string.
    public String extractIdFromTodoJson(String body) {
        // super naive: look for `"id":`
        if (body == null) return null;
        int idx = body.indexOf("\"id\"");
        if (idx == -1) return null;
        int colon = body.indexOf(":", idx);
        if (colon == -1) return null;
        int firstQuote = body.indexOf("\"", colon);
        if (firstQuote == -1) return null;
        int secondQuote = body.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return null;
        return body.substring(firstQuote + 1, secondQuote);
    }
}