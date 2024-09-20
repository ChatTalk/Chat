package com.example.chatservermessage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ChatServerMessageApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatServerMessageApplication.class, args);
	}

}
