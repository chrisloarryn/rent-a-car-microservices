package com.kodlamaio.rentalservice.performance;

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

public class RentalServiceSimulation extends Simulation
{
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(GatlingSettings.baseUrl())
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling rental-service");

    private final ScenarioBuilder scenario = scenario("rental_flow")
            .exec(this::seedRental)
            .exec(http("create_rental")
                    .post("/api/rentals")
                    .body(StringBody("""
                            {
                              "carId":"#{carId}",
                              "dailyPrice":120.0,
                              "rentedForDays":3,
                              "cardNumber":"1234567812345678",
                              "cardHolder":"John Doe",
                              "cardExpirationYear":2027,
                              "cardExpirationMonth":12,
                              "cardCvv":"123"
                            }
                            """))
                    .asJson()
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("rentalId")))
            .exec(http("get_rental")
                    .get("/api/rentals/#{rentalId}")
                    .check(status().is(200)))
            .exec(http("list_rentals")
                    .get("/api/rentals")
                    .check(status().is(200)))
            .exec(http("delete_rental")
                    .delete("/api/rentals/#{rentalId}")
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

    private Session seedRental(Session session)
    {
        String carId = java.util.UUID.randomUUID().toString();
        String suffix = String.valueOf(ThreadLocalRandom.current().nextInt(100_000, 999_999));
        return session
                .set("carId", carId)
                .set("suffix", suffix);
    }
}
