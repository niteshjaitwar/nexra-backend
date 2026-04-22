package com.nexra.hrms.nexra.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springdoc.core.customizers.OperationCustomizer;

import java.util.List;

/**
 * Centralises OpenAPI 3.1 metadata for the Nexra modular monolith. Declares
 * the JWT bearer security scheme, default server URL, license, contact and
 * a consistent Info block. Individual controllers decorate their endpoints
 * with {@code @Operation} and {@code @ApiResponses}; this config merely
 * describes the document itself.
 *
 * @author niteshjaitwar
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String JWT_SCHEME_NAME = "bearerAuth";

    private final String apiTitle;
    private final String apiVersion;
    private final String apiDescription;
    private final String contactName;
    private final String contactEmail;
    private final String serverUrl;

    /**
     * Builds the config with externalised metadata fields.
     *
     * @param apiTitle       public title of the API surface.
     * @param apiVersion     semantic version string published in the document.
     * @param apiDescription marketing friendly description.
     * @param contactName    maintainer display name.
     * @param contactEmail   maintainer email.
     * @param serverUrl      base URL of the environment serving the docs.
     */
    public OpenApiConfig(
            @Value("${nexra.openapi.title:Nexra Platform API}") final String apiTitle,
            @Value("${nexra.openapi.version:1.0.0}") final String apiVersion,
            @Value("${nexra.openapi.description:Modular HRMS and CRM platform for the Nexra suite.}") final String apiDescription,
            @Value("${nexra.openapi.contact.name:Nexra Engineering}") final String contactName,
            @Value("${nexra.openapi.contact.email:engineering@nexra.example}") final String contactEmail,
            @Value("${nexra.openapi.server-url:http://localhost:8080}") final String serverUrl) {
        this.apiTitle = apiTitle;
        this.apiVersion = apiVersion;
        this.apiDescription = apiDescription;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.serverUrl = serverUrl;
    }

    /**
     * Declares the OpenAPI document bean consumed by springdoc-openapi. The
     * document advertises bearer JWT authentication globally so Swagger UI
     * users can exercise secured endpoints with a single token paste.
     *
     * @return fully populated OpenAPI 3.1 document.
     */
    @Bean
    public OpenAPI nexraOpenApi() {
        final SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT obtained from /auth/login or /oauth2/token.");
        final Info info = new Info()
                .title(apiTitle)
                .version(apiVersion)
                .description(apiDescription)
                .contact(new Contact().name(contactName).email(contactEmail))
                .license(new License().name("Proprietary").url("https://nexra.example/license"));
        return new OpenAPI()
                .info(info)
                .servers(List.of(new Server().url(serverUrl).description("Active environment")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(JWT_SCHEME_NAME, bearerScheme));
    }

    /**
     * Applies default OpenAPI metadata for endpoints that do not yet define
     * per-method descriptions or response blocks, ensuring complete docs.
     *
     * @return operation customizer for springdoc.
     */
    @Bean
    public OperationCustomizer nexraOperationDefaultsCustomizer() {
        return (operation, handlerMethod) -> {
            applyDefaultSummary(operation, handlerMethod);
            applyDefaultResponses(operation);
            applyModuleTag(operation, handlerMethod);
            return operation;
        };
    }

    private void applyDefaultSummary(final io.swagger.v3.oas.models.Operation operation, final HandlerMethod handlerMethod) {
        if (operation.getSummary() == null || operation.getSummary().isBlank()) {
            operation.setSummary(humanize(handlerMethod.getMethod().getName()));
        }
    }

    private void applyDefaultResponses(final io.swagger.v3.oas.models.Operation operation) {
        if (operation.getResponses() != null && !operation.getResponses().isEmpty()) {
            return;
        }
        if (operation.getResponses() == null) {
            operation.setResponses(new ApiResponses());
        }
        operation.getResponses()
                .addApiResponse("200", new ApiResponse().description("Request processed successfully."))
                .addApiResponse("400", new ApiResponse().description("Invalid request payload or parameters."))
                .addApiResponse("401", new ApiResponse().description("Authentication required or token invalid."))
                .addApiResponse("403", new ApiResponse().description("Caller is not authorized for this resource."))
                .addApiResponse("500", new ApiResponse().description("Unexpected server error."));
    }

    private void applyModuleTag(final io.swagger.v3.oas.models.Operation operation, final HandlerMethod handlerMethod) {
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            return;
        }
        final String declaringClass = handlerMethod.getBeanType().getName();
        final String marker = ".modules.";
        final int markerIndex = declaringClass.indexOf(marker);
        if (markerIndex < 0) {
            return;
        }
        final String modulePath = declaringClass.substring(markerIndex + marker.length());
        final int separator = modulePath.indexOf('.');
        final String module = separator < 0 ? modulePath : modulePath.substring(0, separator);
        operation.addTagsItem(module.toUpperCase());
    }

    private String humanize(final String methodName) {
        return methodName.replaceAll("([a-z])([A-Z])", "$1 $2");
    }
}
