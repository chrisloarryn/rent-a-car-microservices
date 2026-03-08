package com.kodlamaio.discoveryserver;

import com.kodlamaio.discoveryserver.api.controllers.SystemController;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class DiscoveryServerApplicationTests {

	@Test
	void mainDelegatesToSpringApplication() {
		try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
			String[] args = {"--server.port=0"};

			DiscoveryServerApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(DiscoveryServerApplication.class, args));
		}
	}

	@Test
	void applicationClassCanBeInstantiated() {
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(DiscoveryServerApplication::new);
	}

	@Test
	void pingReturnsDiscoveryServerStatus() {
		var response = new SystemController().ping();

		org.junit.jupiter.api.Assertions.assertEquals("discovery-server", response.getService());
		org.junit.jupiter.api.Assertions.assertEquals("UP", response.getStatus());
		org.junit.jupiter.api.Assertions.assertNotNull(response.getTimestamp());
	}
}
