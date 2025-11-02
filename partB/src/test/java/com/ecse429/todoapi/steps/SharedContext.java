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

    // the last category id we created successfully
    private String lastCreatedCategoryId;

    // store the last created category's title and description for updates
    private String lastCreatedCategoryTitle;
    private String lastCreatedCategoryDescription;

    // list of ALL categories we created in this scenario (so we can delete them in @After)
    private final List<String> createdCategoryIds = new ArrayList<>();

    // the last project id we created successfully
    private String lastCreatedProjectId;

    // store the last created project's title and description for updates
    private String lastCreatedProjectTitle;
    private String lastCreatedProjectDescription;

    // list of ALL projects we created in this scenario (so we can delete them in @After)
    private final List<String> createdProjectIds = new ArrayList<>();

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

    public String getLastCreatedCategoryId() {
        return lastCreatedCategoryId;
    }

    public void setLastCreatedCategoryId(String lastCreatedCategoryId) {
        this.lastCreatedCategoryId = lastCreatedCategoryId;
    }

    public String getLastCreatedCategoryTitle() {
        return lastCreatedCategoryTitle;
    }

    public void setLastCreatedCategoryTitle(String lastCreatedCategoryTitle) {
        this.lastCreatedCategoryTitle = lastCreatedCategoryTitle;
    }

    public String getLastCreatedCategoryDescription() {
        return lastCreatedCategoryDescription;
    }

    public void setLastCreatedCategoryDescription(String lastCreatedCategoryDescription) {
        this.lastCreatedCategoryDescription = lastCreatedCategoryDescription;
    }

    public void trackCreatedCategoryId(String id) {
        if (id != null && !id.isEmpty()) {
            createdCategoryIds.add(id);
        }
    }

    public List<String> getCreatedCategoryIds() {
        return createdCategoryIds;
    }

    public String getLastCreatedProjectId() {
        return lastCreatedProjectId;
    }

    public void setLastCreatedProjectId(String lastCreatedProjectId) {
        this.lastCreatedProjectId = lastCreatedProjectId;
    }

    public String getLastCreatedProjectTitle() {
        return lastCreatedProjectTitle;
    }

    public void setLastCreatedProjectTitle(String lastCreatedProjectTitle) {
        this.lastCreatedProjectTitle = lastCreatedProjectTitle;
    }

    public String getLastCreatedProjectDescription() {
        return lastCreatedProjectDescription;
    }

    public void setLastCreatedProjectDescription(String lastCreatedProjectDescription) {
        this.lastCreatedProjectDescription = lastCreatedProjectDescription;
    }

    public void trackCreatedProjectId(String id) {
        if (id != null && !id.isEmpty()) {
            createdProjectIds.add(id);
        }
    }

    public List<String> getCreatedProjectIds() {
        return createdProjectIds;
    }

    // wipe all scenario info
    public void reset() {
        lastResponse = null;
        lastCreatedTodoId = null;
        lastCreatedTodoTitle = null;
        lastCreatedTodoDescription = null;
        createdTodoIds.clear();
        lastCreatedCategoryId = null;
        lastCreatedCategoryTitle = null;
        lastCreatedCategoryDescription = null;
        createdCategoryIds.clear();
        lastCreatedProjectId = null;
        lastCreatedProjectTitle = null;
        lastCreatedProjectDescription = null;
        createdProjectIds.clear();
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

    public String extractIdFromCategoryJson(String body) {
        // same naive logic for categories
        return extractIdFromTodoJson(body);
    }

    public String extractIdFromProjectJson(String body) {
        // same naive logic for projects
        return extractIdFromTodoJson(body);
    }

    public int countCategoriesInJson(String body) {
        if (body == null) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = body.indexOf("\"id\"", idx)) != -1) {
            count++;
            idx++;
        }
        return count;
    }

    public int countTodosInJson(String body) {
        if (body == null) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = body.indexOf("\"id\"", idx)) != -1) {
            count++;
            idx++;
        }
        return count;
    }
}