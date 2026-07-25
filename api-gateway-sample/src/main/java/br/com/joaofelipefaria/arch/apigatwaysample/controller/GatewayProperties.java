package br.com.joaofelipefaria.arch.apigatwaysample.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private Map<String, String> routes = new HashMap<>();

}
