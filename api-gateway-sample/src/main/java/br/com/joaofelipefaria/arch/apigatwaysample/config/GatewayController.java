package br.com.joaofelipefaria.arch.apigatwaysample.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.joaofelipefaria.arch.apigatwaysample.proxy.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GatewayController {

    private final ProxyService proxyService;

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {

        return proxyService.forward(request, body);

    }

}