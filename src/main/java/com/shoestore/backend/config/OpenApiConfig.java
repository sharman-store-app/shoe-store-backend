package com.shoestore.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myOpenApiConfig() {

        Info info = new Info()
                .title("REST API for a modern e-commerce shoe store developed as a team project.")
                .description("""
                        <b>Features:</b>
                        <ul>
                        <li>JWT authentication and role-based authorization</li>
                        <li>Google Sign-In (OAuth 2.0)</li>
                        <li>User registration and email verification</li>
                        <li>Password reset via email (Brevo)</li>
                        <li>Product, variant, and image management</li>
                        <li>Shopping cart operations</li>
                        <li>Order management</li>
                        <li>Stripe Checkout integration</li>
                        <li>Discount code support</li>
                        <li>CSV data importers</li>
                        <li>Session tracking and analytics</li>
                        <li>OpenAPI 3 documentation</li>
                        </ul>

                        Built with Java 21, Spring Boot, Spring Security, Spring Data JPA,
                        Hibernate, PostgreSQL, Liquibase, Docker, Stripe, Brevo, and JWT.<br><br>

                        Deployed with a React/Next.js frontend on Vercel and a Spring Boot
                        backend running on a Dockerized VPS with PostgreSQL and Nginx Proxy
                        Manager.</b><br>
                        """);

        return new OpenAPI().info(info)
                .tags(List.of(
                        new Tag().name("01. Authentication")
                                .description("Authentication related endpoints"),
                        new Tag().name("02. Users").description("User related endpoints"),
                        new Tag().name("03. Products").description("Product related endpoints"),
                        new Tag().name("04. Cart and cart item")
                                .description("Cart and cart item related endpoints"),
                        new Tag().name("05. Orders").description("Order related endpoints"),
                        new Tag().name("06. Sessions").description("Session endpoint"),
                        new Tag().name("07. Payment").description("Payment endpoints")
                ))
                .components(new Components().addSecuritySchemes("BearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
