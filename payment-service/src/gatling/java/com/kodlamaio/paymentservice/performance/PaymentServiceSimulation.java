package com.kodlamaio.paymentservice.performance;

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

public class PaymentServiceSimulation extends Simulation
{
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(GatlingSettings.baseUrl())
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling payment-service");

    private final ScenarioBuilder scenario = scenario("payment_flow")
            .exec(this::seedPayment)
            .exec(http("create_payment")
                    .post("/api/payments")
                    .body(StringBody("""
                            {
                              "cardNumber":"#{cardNumber}",
                              "cardHolder":"John Doe",
                              "cardExpirationYear":2027,
                              "cardExpirationMonth":12,
                              "cardCvv":"123",
                              "balance":500.0
                            }
                            """))
                    .asJson()
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("paymentId")))
            .exec(http("get_payment")
                    .get("/api/payments/#{paymentId}")
                    .check(status().is(200)))
            .exec(http("process_rental_payment")
                    .post("/api/internal/payments/rental-processing")
                    .body(StringBody("""
                            {
                              "cardNumber":"#{cardNumber}",
                              "cardHolder":"John Doe",
                              "cardExpirationYear":2027,
                              "cardExpirationMonth":12,
                              "cardCvv":"123",
                              "price":100.0
                            }
                            """))
                    .asJson()
                    .check(status().is(200)))
            .exec(http("delete_payment")
                    .delete("/api/payments/#{paymentId}")
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

    private Session seedPayment(Session session)
    {
        long suffix = ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L, 9_999_999_999_999_999L);
        return session.set("cardNumber", Long.toString(suffix));
    }
}
