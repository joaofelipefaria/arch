package br.com.joaofelipefaria.arch.apigatwaysample.routing;

import org.springframework.stereotype.Component;

import br.com.joaofelipefaria.arch.apigatwaysample.controller.GatewayProperties;
import br.com.joaofelipefaria.arch.apigatwaysample.exception.RouteNotFoundException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RouteResolver {

    private final GatewayProperties properties;

    public Route resolve(String service) {

        String baseUrl = properties.getRoutes().get(service.toLowerCase());

        if (baseUrl == null) {
            throw new RouteNotFoundException(service);
        }

        return new Route(service, baseUrl);

    }

}