package com.hyzs.dog.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author Hua-cloud
 */
@EnableEurekaClient
@SpringBootApplication
public class DogGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(DogGatewayApplication.class, args);
	}

}
