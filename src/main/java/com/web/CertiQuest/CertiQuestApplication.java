package com.web.CertiQuest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.web.CertiQuest")
public class CertiQuestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertiQuestApplication.class, args);

		// Keep JVM running so Kafka listener stays alive
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
