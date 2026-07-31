package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class RestClientConfigTest {

    @Test
    void restClientCreatesClient() {
        RestClient restClient = new RestClientConfig().restClient();

        assertNotNull(restClient);
    }
}
