package com.ecse429.todoapi.steps;

import io.cucumber.java.Before;
import io.cucumber.java.After;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hooks:
 * - Guarantees service is running before every Scenario
 * - Cleans up created data after every Scenario
 * - Resets SharedContext so tests are independent
 *
 * This satisfies:
 *  - "Ensure story tests fail if service is not running."
 *  - "Restore the system to the initial state upon completion."
 *  - "Run in any order."
 */
public class Hooks {

    private final ApiClient api;
    private final SharedContext ctx;

    public Hooks(SharedContext ctx) {
        this.api = new ApiClient();
        this.ctx = ctx;
    }

    @Before
    public void beforeScenario() throws IOException, InterruptedException {
        // 1. Check the service is alive (assignment requires tests to fail if it's not)
        HttpResponse<String> ping = api.get("/todos");
        boolean serviceUp = ping != null && ping.statusCode() >= 200 && ping.statusCode() < 500;
        assertTrue(serviceUp, "Service is not running on http://localhost:4567. Start runTodoManagerRestAPI-1.5.5.jar first.");

        // 2. Fresh context for this scenario
        ctx.reset();
    }

    @After
    public void afterScenario() throws IOException, InterruptedException {
        // delete any todos we created in this Scenario
        for (String id : ctx.getCreatedTodoIds()) {
            api.delete("/todos/" + id);
        }

        // clean memory copy
        ctx.reset();
    }
}