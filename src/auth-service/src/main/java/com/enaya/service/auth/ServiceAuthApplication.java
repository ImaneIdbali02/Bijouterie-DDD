package com.enaya.service.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceAuthApplication {
	public static void main(String[] args) {
	 try {
		Class.forName("org.postgresql.Driver");
		System.out.println("PostgreSQL driver found!");
	} catch (ClassNotFoundException e) {
		System.out.println("PostgreSQL driver NOT found!");
	}

        SpringApplication.run(ServiceAuthApplication.class, args);

    }

}
