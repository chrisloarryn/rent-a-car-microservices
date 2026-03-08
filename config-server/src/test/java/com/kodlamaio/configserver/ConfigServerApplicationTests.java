package com.kodlamaio.configserver;

import com.kodlamaio.configserver.api.controllers.SystemController;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ConfigServerApplicationTests {

	@Test
	void mainDelegatesToSpringApplication() {
		try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
			String[] args = {"--server.port=0"};

			ConfigServerApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(ConfigServerApplication.class, args));
		}
	}

	@Test
	void applicationClassCanBeInstantiated() {
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(ConfigServerApplication::new);
	}

	@Test
	void pingReturnsConfigServerStatus() {
		var response = new SystemController().ping();

		org.junit.jupiter.api.Assertions.assertEquals("config-server", response.getService());
		org.junit.jupiter.api.Assertions.assertEquals("UP", response.getStatus());
		org.junit.jupiter.api.Assertions.assertNotNull(response.getTimestamp());
	}
}
