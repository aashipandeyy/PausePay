package com.financeautopilot.financeautopilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FinanceautopilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceautopilotApplication.class, args);
	}

}
