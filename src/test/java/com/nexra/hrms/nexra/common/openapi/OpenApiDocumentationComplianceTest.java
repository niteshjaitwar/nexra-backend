package com.nexra.hrms.nexra.common.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Validates that the generated OpenAPI document publishes a summary and
 * response schema for every documented HTTP operation. This protects release
 * quality by preventing undocumented endpoints from slipping into production.
 *
 * @author niteshjaitwar
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationComplianceTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Ensures each OpenAPI path operation contains a non-blank summary and at
     * least one response entry.
     *
     * @throws Exception when the OpenAPI endpoint cannot be queried or parsed.
     */
    @Test
    void openApiMustExposeSummaryAndResponsesForEveryOperation() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode paths = root.path("paths");
        assertThat(paths.isObject()).isTrue();
        assertThat(paths.size()).isGreaterThan(0);

        Iterator<Map.Entry<String, JsonNode>> pathIterator = paths.fields();
        while (pathIterator.hasNext()) {
            Map.Entry<String, JsonNode> pathEntry = pathIterator.next();
            Iterator<Map.Entry<String, JsonNode>> operationIterator = pathEntry.getValue().fields();
            while (operationIterator.hasNext()) {
                Map.Entry<String, JsonNode> operationEntry = operationIterator.next();
                JsonNode operation = operationEntry.getValue();

                assertThat(operation.path("summary").asText(""))
                    .as("summary missing for %s %s", operationEntry.getKey(), pathEntry.getKey())
                    .isNotBlank();
                assertThat(operation.path("responses").isObject())
                    .as("responses missing for %s %s", operationEntry.getKey(), pathEntry.getKey())
                    .isTrue();
                assertThat(operation.path("responses").size())
                    .as("responses empty for %s %s", operationEntry.getKey(), pathEntry.getKey())
                    .isGreaterThan(0);
            }
        }
    }
}
