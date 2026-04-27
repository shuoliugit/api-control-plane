package com.example.apicontrolplane;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationWorkflowTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void developerCanCreateAppAndAdminCanApprove() throws Exception {
    String devToken = register("owner@example.com", "Owner User");
    String adminToken = login("admin@controlplane.local", "AdminPassword123!");

    String appBody = mvc.perform(post("/api/apps")
            .header("Authorization", "Bearer " + devToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Treasury Sandbox","description":"Payment integration validation"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING_REVIEW"))
        .andReturn().getResponse().getContentAsString();
    String appId = objectMapper.readTree(appBody).get("id").asText();

    mvc.perform(post("/api/admin/apps/" + appId + "/approve").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"));
  }

  private String register(String email, String displayName) throws Exception {
    String body = mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"Password123!\",\"displayName\":\"" + displayName + "\"}"))
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body).get("token").asText();
  }

  private String login(String email, String password) throws Exception {
    String body = mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
        .andReturn().getResponse().getContentAsString();
    JsonNode json = objectMapper.readTree(body);
    return json.get("token").asText();
  }
}
