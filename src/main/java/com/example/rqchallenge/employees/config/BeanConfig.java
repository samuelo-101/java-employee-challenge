package com.example.rqchallenge.employees.config;

import com.example.rqchallenge.employees.exception.EmployeeApiInternalServerException;
import com.example.rqchallenge.employees.exception.ExternalApiRedirectException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class BeanConfig {

    @Value("${external.api.base.url}")
    private String externalApiBaseUrl;

    @Bean
    public WebClient webClient() {
        final int requestAndResponseTimeoutMills = 10000;
        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, requestAndResponseTimeoutMills)
                .responseTimeout(Duration.ofMillis(requestAndResponseTimeoutMills))
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(requestAndResponseTimeoutMills, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(requestAndResponseTimeoutMills, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(externalApiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(webClientErrorHandler())
                .build();
    }

    private ExchangeFilterFunction webClientErrorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new EmployeeApiInternalServerException(errorBody)));
            } else if (clientResponse.statusCode().is3xxRedirection()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new ExternalApiRedirectException(errorBody)));
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

}
