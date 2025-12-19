package com.albaraka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.albaraka", "com.albaraka_bank"})
public class AlbarakaV1Application {

	public static void main(String[] args) {
		SpringApplication.run(AlbarakaV1Application.class, args);
	}

}
