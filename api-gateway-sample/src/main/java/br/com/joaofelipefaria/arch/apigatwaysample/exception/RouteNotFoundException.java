package br.com.joaofelipefaria.arch.apigatwaysample.exception;

public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(String serviceName) {
        super("No route configured for service: " + serviceName);
    }

}
