package com.example.apicontrolplane.portal;

import com.example.apicontrolplane.auth.CurrentUser;
import com.example.apicontrolplane.credential.ApiCredentialRepository;
import com.example.apicontrolplane.credential.CredentialService;
import com.example.apicontrolplane.exception.ApiException;
import com.example.apicontrolplane.usage.ApiUsageRepository;
import com.example.apicontrolplane.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apps")
public class ApplicationController {
  private final DeveloperApplicationRepository apps;
  private final UserRepository users;
  private final ApiCredentialRepository credentials;
  private final CredentialService credentialService;
  private final ApiUsageRepository usage;

  public ApplicationController(DeveloperApplicationRepository apps, UserRepository users, ApiCredentialRepository credentials,
      CredentialService credentialService, ApiUsageRepository usage) {
    this.apps = apps;
    this.users = users;
    this.credentials = credentials;
    this.credentialService = credentialService;
    this.usage = usage;
  }

  @GetMapping
  List<AppResponse> list(@AuthenticationPrincipal CurrentUser user) {
    return apps.findByOwnerIdOrderByCreatedAtDesc(user.id()).stream().map(AppResponse::from).toList();
  }

  @PostMapping
  AppResponse create(@AuthenticationPrincipal CurrentUser user, @RequestBody @Valid UpsertAppRequest request) {
    var owner = users.findById(user.id()).orElseThrow();
    return AppResponse.from(apps.save(new DeveloperApplication(owner, request.name(), request.description())));
  }

  @PutMapping("/{id}")
  AppResponse update(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id, @RequestBody @Valid UpsertAppRequest request) {
    DeveloperApplication app = owned(id, user.id());
    app.update(request.name(), request.description());
    return AppResponse.from(apps.save(app));
  }

  @DeleteMapping("/{id}")
  void delete(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
    DeveloperApplication app = owned(id, user.id());
    if (app.isProvisioned()) {
      throw new ApiException(HttpStatus.CONFLICT, "Provisioned applications cannot be deleted");
    }
    apps.delete(app);
  }

  @PostMapping("/{id}/credentials")
  CredentialService.CredentialResponse generateCredentials(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
    DeveloperApplication app = owned(id, user.id());
    return credentialService.generate(app);
  }

  @PostMapping("/{id}/credentials/rotate")
  CredentialService.CredentialResponse rotateCredentials(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
    owned(id, user.id());
    var credential = credentials.findByApplicationId(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Credentials not found"));
    return credentialService.rotate(credential);
  }

  @GetMapping("/{id}/usage")
  List<UsageResponse> usage(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
    owned(id, user.id());
    return usage.findTop50ByApplicationIdOrderByOccurredAtDesc(id).stream()
        .map(e -> new UsageResponse(e.getEndpoint(), e.getMethod(), e.getStatusCode(), e.getRequestId(), e.getOccurredAt().toString()))
        .toList();
  }

  private DeveloperApplication owned(UUID id, UUID ownerId) {
    return apps.findByIdAndOwnerId(id, ownerId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Application not found"));
  }

  public record UpsertAppRequest(@NotBlank @Size(max = 120) String name, @Size(max = 1000) String description) {}
  public record AppResponse(UUID id, String name, String description, ApplicationStatus status, boolean provisioned, String createdAt, String updatedAt) {
    public static AppResponse from(DeveloperApplication app) {
      return new AppResponse(app.getId(), app.getName(), app.getDescription(), app.getStatus(), app.isProvisioned(), app.getCreatedAt().toString(), app.getUpdatedAt().toString());
    }
  }
  public record UsageResponse(String endpoint, String method, int statusCode, String requestId, String occurredAt) {}
}
