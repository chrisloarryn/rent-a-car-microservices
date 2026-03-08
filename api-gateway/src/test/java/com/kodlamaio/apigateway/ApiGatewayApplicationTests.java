package com.kodlamaio.apigateway;

import com.kodlamaio.apigateway.api.controllers.SystemController;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import reactor.test.StepVerifier;

class ApiGatewayApplicationTests {

	@Test
	void mainDelegatesToSpringApplication() {
		try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
			String[] args = {"--server.port=0"};

			ApiGatewayApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(ApiGatewayApplication.class, args));
		}
	}

	@Test
	void applicationClassCanBeInstantiated() {
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(ApiGatewayApplication::new);
	}

	@Test
	void pingReturnsGatewayStatus() {
		SystemController controller = new SystemController();

		StepVerifier.create(controller.ping())
				.assertNext(response -> {
					org.junit.jupiter.api.Assertions.assertEquals("api-gateway", response.getService());
					org.junit.jupiter.api.Assertions.assertEquals("UP", response.getStatus());
					org.junit.jupiter.api.Assertions.assertNotNull(response.getTimestamp());
				})
				.verifyComplete();
	}
}
