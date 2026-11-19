package com.clinica.dailyautism;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DailyAutismApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailyAutismApplication.class, args);
	}

}
