package com.kodlamaio.inventoryservice.performance;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.forAll;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class InventoryServiceSimulation extends Simulation
{
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(GatlingSettings.baseUrl())
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling inventory-service");

    private final ScenarioBuilder scenario = scenario("inventory_brand_flow")
            .exec(this::seedBrand)
            .exec(http("create_brand")
                    .post("/api/brands")
                    .body(StringBody("{\"name\":\"#{brandName}\"}"))
                    .asJson()
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("brandId")))
            .exec(http("get_brand")
                    .get("/api/brands/#{brandId}")
                    .check(status().is(200)))
            .exec(http("list_brands")
                    .get("/api/brands")
                    .check(status().is(200)))
            .exec(http("delete_brand")
                    .delete("/api/brands/#{brandId}")
                    .check(status().is(204)));

    {
        setUp(scenario.injectOpen(
                        rampUsers(GatlingSettings.users()).during(GatlingSettings.rampDuration()),
                        constantUsersPerSec(GatlingSettings.users()).during(GatlingSettings.holdDuration())))
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().count().is(0L),
                        global().responseTime().percentile3().lt(1500),
                        forAll().successfulRequests().percent().is(100.0));
    }

    private Session seedBrand(Session session)
    {
        String suffix = String.valueOf(ThreadLocalRandom.current().nextInt(100_000, 999_999));
        return session.set("brandName", "Brand " + suffix);
    }
}
