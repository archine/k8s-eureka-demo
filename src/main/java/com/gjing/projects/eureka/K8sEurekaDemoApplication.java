package com.gjing.projects.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author Gjing
 */
@EnableEurekaServer
@SpringBootApplication
public class K8sEurekaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(K8sEurekaDemoApplication.class, args);
    }
}
