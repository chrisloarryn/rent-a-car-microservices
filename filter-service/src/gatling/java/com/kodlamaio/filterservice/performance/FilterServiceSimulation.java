package com.kodlamaio.filterservice.performance;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.forAll;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class FilterServiceSimulation extends Simulation
{
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(GatlingSettings.baseUrl())
            .acceptHeader("application/json")
            .userAgentHeader("Gatling filter-service");

    private final ScenarioBuilder scenario = scenario("filter_query_flow")
            .exec(http("list_filters")
                    .get("/api/filters")
                    .check(status().is(200)))
            .exec(http("filter_docs")
                    .get("/v3/api-docs")
                    .check(status().is(200)));

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
}
