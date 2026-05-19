package com.ssafy.ssabre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
public class SsabreApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsabreApplication.class, args);
	}
	// test

}
