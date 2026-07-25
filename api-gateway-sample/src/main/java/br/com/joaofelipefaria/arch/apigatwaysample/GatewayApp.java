package br.com.joaofelipefaria.arch.apigatwaysample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import br.com.joaofelipefaria.arch.apigatwaysample.controller.GatewayProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayApp {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApp.class, args);
    }

}
