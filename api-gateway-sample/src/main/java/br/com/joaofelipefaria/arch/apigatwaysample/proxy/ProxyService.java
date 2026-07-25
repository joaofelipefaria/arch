package br.com.joaofelipefaria.arch.apigatwaysample.proxy;

import java.net.URI;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.com.joaofelipefaria.arch.apigatwaysample.exception.GatewayException;
import br.com.joaofelipefaria.arch.apigatwaysample.routing.Route;
import br.com.joaofelipefaria.arch.apigatwaysample.routing.RouteResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProxyService {

	private final RouteResolver routeResolver;
	private final RestClient restClient;

	public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body) {
		try {
			Route route = resolveRoute(request);
			URI targetUri = buildTargetUri(request, route);
			HttpHeaders headers = copyHeaders(request);

			// Create a request using the same HTTP method received by the gateway (GET, POST, PUT, DELETE, etc.)
			return restClient
			        .method(HttpMethod.valueOf(request.getMethod()))
			        // Set the destination URI resolved by the gateway
			        .uri(targetUri)
			        // Copy all incoming request headers to the outgoing request
			        .headers(h -> h.addAll(headers))
			        // Forward the original request body.
			        // If there is no body (e.g. GET requests), send an empty byte array.
			        .body(body == null ? new byte[0] : body)
			        // Execute the request and handle the raw HTTP response.
			        // Using exchange() allows the gateway to preserve the original
			        // status code instead of throwing exceptions for 4xx/5xx responses.
			        .exchange((clientRequest, clientResponse) -> {
			            // Create a new HttpHeaders object for the response.
			            HttpHeaders responseHeaders = new HttpHeaders();
			            // Copy all headers returned by the destination service.
			            responseHeaders.putAll(clientResponse.getHeaders());
			            // Read the entire response body as a byte array.
			            byte[] responseBody = clientResponse.getBody().readAllBytes();
			            // Return the response exactly as received from the destination service,
			            // preserving the status code, headers, and body.
			            return ResponseEntity
			                    .status(clientResponse.getStatusCode())
			                    .headers(responseHeaders)
			                    .body(responseBody);
			        });
		} catch (Exception ex) {
			throw new GatewayException("Unable to forward request.", ex);
		}
	}

	private Route resolveRoute(HttpServletRequest request) {
		String requestUri = request.getRequestURI();
		String[] segments = requestUri.substring(1).split("/", 2);
		return routeResolver.resolve(segments[0]);
	}

	private URI buildTargetUri(HttpServletRequest request, Route route) {
		String[] segments = request.getRequestURI().substring(1).split("/", 2);
		String path = segments.length > 1 ? "/" + segments[1] : "";
		String url = route.baseUrl() + path;
		if (request.getQueryString() != null) {
			url += "?" + request.getQueryString();
		}
		return URI.create(url);
	}

	private HttpHeaders copyHeaders(HttpServletRequest request) {
		HttpHeaders headers = new HttpHeaders();
		Collections.list(request.getHeaderNames())
				.forEach(name -> headers.addAll(name, Collections.list(request.getHeaders(name))));
		return headers;
	}

}