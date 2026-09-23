package com.tiagoperon.bfftaskscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BffTaskSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BffTaskSchedulerApplication.class, args);
	}

}
