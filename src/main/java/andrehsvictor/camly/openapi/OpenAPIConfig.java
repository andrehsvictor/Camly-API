package andrehsvictor.camly.openapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.version:1.0.0}")
    private String appVersion;

    @Value("${camly.server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Camly API")
                        .description("A photo storage and sharing service API")
                        .version(appVersion)
                        .contact(new Contact()
                                .name("André Victor")
                                .email("andrehsvictor@gmail.com")
                                .url("https://github.com/andrehsvictor/Camly-API"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addServersItem(new Server()
                        .url(serverUrl)
                        .description("Server URL"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authentication. Enter the JWT token in the format: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}