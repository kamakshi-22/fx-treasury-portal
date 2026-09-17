package com.fxtreasury.valuation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ValuationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValuationEngineApplication.class, args);
	}

}