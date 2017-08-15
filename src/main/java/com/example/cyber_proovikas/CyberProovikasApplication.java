package com.example.cyber_proovikas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CyberProovikasApplication {

	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(CyberProovikasApplication.class);
		logger.info("Starting Application.");
		SpringApplication.run(CyberProovikasApplication.class, args);
	}
}
