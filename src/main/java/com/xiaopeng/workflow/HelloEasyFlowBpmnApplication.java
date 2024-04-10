package com.xiaopeng.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class HelloEasyFlowBpmnApplication {

	public static void main(String[] args) {
		log.info("HelloEasyFlowBpmnApplication starting...");
		SpringApplication.run(HelloEasyFlowBpmnApplication.class, args);
	}

}
