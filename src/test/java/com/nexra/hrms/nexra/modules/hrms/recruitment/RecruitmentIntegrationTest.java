package com.nexra.hrms.nexra.modules.hrms.recruitment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration coverage for recruitment workflows inside the modular monolith.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecruitmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void recruitmentFlowWorks() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "ACME", List.of("ROLE_PLATFORM_ADMIN"));

        String jobResponse = mockMvc.perform(put("/api/v1/recruitment/jobs")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "title":"Java Engineer",
                      "department":"Tech",
                      "location":"Hyd",
                      "status":"OPEN"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Request-Id"))
            .andReturn().getResponse().getContentAsString();

        String jobId = OBJECT_MAPPER.readTree(jobResponse).path("data").path("jobId").asText();

        String candidateResponse = mockMvc.perform(post("/api/v1/recruitment/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "jobId":"%s",
                      "fullName":"Alice",
                      "email":"a@test.com"
                    }
                    """.formatted(jobId)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String candidateId = OBJECT_MAPPER.readTree(candidateResponse).path("data").path("candidateId").asText();

        mockMvc.perform(post("/api/v1/recruitment/candidates/{candidateId}/stage", candidateId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "stage":"HIRED",
                      "comment":"Selected"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.stage").value("HIRED"));

        mockMvc.perform(get("/api/v1/recruitment/candidates/{candidateId}/history", candidateId)
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].toStage").exists());

        mockMvc.perform(get("/api/v1/recruitment/summary")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalJobs").value(1))
            .andExpect(jsonPath("$.data.hiredCandidates").value(1));
    }

    @Test
    void tenantMismatchRejected() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "OTHER", List.of("ROLE_PLATFORM_ADMIN"));
        mockMvc.perform(get("/api/v1/recruitment/jobs")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void duplicateCandidateEmailRejected() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "DUPL", List.of("ROLE_PLATFORM_ADMIN"));

        String jobResponse = mockMvc.perform(put("/api/v1/recruitment/jobs")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"DUPL",
                      "title":"QA Engineer",
                      "department":"Tech",
                      "location":"Indore",
                      "status":"OPEN"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String jobId = OBJECT_MAPPER.readTree(jobResponse).path("data").path("jobId").asText();

        mockMvc.perform(post("/api/v1/recruitment/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"DUPL",
                      "jobId":"%s",
                      "fullName":"Bob",
                      "email":"bob@test.com"
                    }
                    """.formatted(jobId)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/recruitment/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"DUPL",
                      "jobId":"%s",
                      "fullName":"Robert",
                      "email":"BOB@test.com"
                    }
                    """.formatted(jobId)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Candidate already exists for tenant with the same email."));
    }

    @Test
    void rejectsMalformedJsonPayloads() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "ACME", List.of("ROLE_PLATFORM_ADMIN"));

        mockMvc.perform(post("/api/v1/recruitment/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"ACME","jobId":"missing","fullName":"Broken"
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
            .andExpect(jsonPath("$.message").value("Invalid request payload."));
    }

    @Test
    void rejectsRecruitmentAdministrationForNonAdminRole() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "ACME", List.of("ROLE_EMPLOYEE"));

        mockMvc.perform(put("/api/v1/recruitment/jobs")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "title":"Business Analyst",
                      "department":"Ops",
                      "location":"Pune",
                      "status":"OPEN"
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User does not have recruitment administration permission"));
    }

    @Test
    void rejectsDuplicateCandidatePhoneWithinTenant() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "DUPP", List.of("ROLE_PLATFORM_ADMIN"));

        String jobResponse = mockMvc.perform(put("/api/v1/recruitment/jobs")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"DUPP",
                      "title":"Support Engineer",
                      "department":"Support",
                      "location":"Noida",
                      "status":"OPEN"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String jobId = OBJECT_MAPPER.readTree(jobResponse).path("data").path("jobId").asText();

        mockMvc.perform(post("/api/v1/recruitment/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"DUPP",
                      "jobId":"%s",
                      "fullName":"First Candidate",
                      "email":"first@test.com",
                      "phone":"9999999999"
                    }
                    """.formatted(jobId)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/recruitment/candidates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"DUPP",
                      "jobId":"%s",
                      "fullName":"Second Candidate",
                      "email":"second@test.com",
                      "phone":"9999999999"
                    }
                    """.formatted(jobId)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Candidate already exists for tenant with the same phone."));
    }

    private String bearerToken(final UUID userId, final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("recruitment.admin@acme.test")
            .claim("uid", userId.toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .signWith(key)
            .compact();
    }
}
