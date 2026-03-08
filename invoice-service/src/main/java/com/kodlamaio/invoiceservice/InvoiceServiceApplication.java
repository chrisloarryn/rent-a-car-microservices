package com.kodlamaio.invoiceservice;

import com.kodlamaio.commonpackage.utils.constants.Paths;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {Paths.ConfigurationBasePackage, Paths.Invoice.ServiceBasePackage})
@OpenAPIDefinition(info = @Info(title = "Rent A Car Invoice Service", version = "v1"))
public class InvoiceServiceApplication
{
	public static void main(String[] args) {
		SpringApplication.run(InvoiceServiceApplication.class, args);
	}
}
