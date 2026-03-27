package com.nexra.hrms.nexra.modules.hrms.employee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class JsonTestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestUtils() {
    }

    static String readJsonPath(final String json, final String path) throws Exception {
        if (!"$.data.departmentId".equals(path)) {
            throw new IllegalArgumentException("Unsupported path in test helper: " + path);
        }
        JsonNode root = OBJECT_MAPPER.readTree(json);
        return root.path("data").path("departmentId").asText();
    }
}
