package com.example.apicontrolplane.sandbox;

import com.example.apicontrolplane.credential.ApiCredential;
import com.example.apicontrolplane.credential.ApiCredentialRepository;
import com.example.apicontrolplane.credential.CredentialService;
import com.example.apicontrolplane.exception.ApiException;
import com.example.apicontrolplane.portal.ApplicationStatus;
import com.example.apicontrolplane.usage.ApiUsageEvent;
import com.example.apicontrolplane.usage.ApiUsageRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sandbox")
public class SandboxController {
  private final ApiCredentialRepository credentials;
  private final CredentialService credentialService;
  private final RateLimiter rateLimiter;
  private final ApiUsageRepository usage;

  public SandboxController(ApiCredentialRepository credentials, CredentialService credentialService, RateLimiter rateLimiter, ApiUsageRepository usage) {
    this.credentials = credentials;
    this.credentialService = credentialService;
    this.rateLimiter = rateLimiter;
    this.usage = usage;
  }

  @GetMapping("/accounts")
  Map<String, Object> accounts(@RequestHeader("X-Client-Id") String clientId, @RequestHeader("X-Client-Secret") String secret, HttpServletRequest request) {
    ApiCredential credential = authenticate(clientId, secret);
    enforceRateLimit(clientId);
    log(credential, request, 200);
    return Map.of("accounts", java.util.List.of(
        Map.of("id", "acct_sandbox_001", "status", "ACTIVE", "balance", 128420),
        Map.of("id", "acct_sandbox_002", "status", "REVIEW", "balance", 42000)));
  }

  @PostMapping("/payments")
  Map<String, Object> payment(@RequestHeader("X-Client-Id") String clientId, @RequestHeader("X-Client-Secret") String secret,
      @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
    ApiCredential credential = authenticate(clientId, secret);
    enforceRateLimit(clientId);
    log(credential, request, 202);
    return Map.of("paymentId", "pay_" + UUID.randomUUID().toString().replace("-", ""), "status", "ACCEPTED", "submitted", requestBody);
  }

  private ApiCredential authenticate(String clientId, String secret) {
    ApiCredential credential = credentials.findByClientId(clientId)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid client credentials"));
    if (!credentialService.matches(credential, secret)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid client credentials");
    }
    var app = credential.getApplication();
    if (app.getStatus() != ApplicationStatus.APPROVED || !app.isProvisioned()) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Application is not provisioned for sandbox access");
    }
    return credential;
  }

  private void enforceRateLimit(String clientId) {
    if (!rateLimiter.allow(clientId)) {
      throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Sandbox rate limit exceeded");
    }
  }

  private void log(ApiCredential credential, HttpServletRequest request, int status) {
    String requestId = request.getHeader("X-Request-Id");
    usage.save(new ApiUsageEvent(credential.getApplication(), request.getRequestURI(), request.getMethod(), status,
        requestId == null ? UUID.randomUUID().toString() : requestId));
  }
}
