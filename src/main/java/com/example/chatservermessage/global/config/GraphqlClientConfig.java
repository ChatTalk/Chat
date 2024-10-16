package com.example.chatservermessage.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

// 비동기 및 논블로킹, Reactive Streams 을 지원
@Configuration
public class GraphqlClientConfig {

    @Value("${graphql.url.chat}")
    private String CHAT_GRAPHQL_URL;

    @Bean
    public HttpGraphQlClient graphQlClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(CHAT_GRAPHQL_URL)
                .build();

        return HttpGraphQlClient.builder(webClient)
                .build();
    }
}
