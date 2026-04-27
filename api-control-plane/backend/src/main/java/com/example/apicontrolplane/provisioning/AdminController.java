package com.example.apicontrolplane.provisioning;

import com.example.apicontrolplane.exception.ApiException;
import com.example.apicontrolplane.portal.ApplicationController.AppResponse;
import com.example.apicontrolplane.portal.DeveloperApplication;
import com.example.apicontrolplane.portal.DeveloperApplicationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/apps")
public class AdminController {
  private final DeveloperApplicationRepository apps;

  public AdminController(DeveloperApplicationRepository apps) {
    this.apps = apps;
  }

  @GetMapping
  List<AdminAppResponse> all() {
    return apps.findAllByOrderByCreatedAtDesc().stream().map(AdminAppResponse::from).toList();
  }

  @PostMapping("/{id}/approve")
  AppResponse approve(@PathVariable UUID id) {
    DeveloperApplication app = get(id);
    app.approve();
    return AppResponse.from(apps.save(app));
  }

  @PostMapping("/{id}/reject")
  AppResponse reject(@PathVariable UUID id) {
    DeveloperApplication app = get(id);
    app.reject();
    return AppResponse.from(apps.save(app));
  }

  @PostMapping("/{id}/provision")
  AppResponse provision(@PathVariable UUID id) {
    DeveloperApplication app = get(id);
    app.markProvisioned();
    return AppResponse.from(apps.save(app));
  }

  private DeveloperApplication get(UUID id) {
    return apps.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Application not found"));
  }

  public record AdminAppResponse(UUID id, String ownerEmail, String name, String description, String status, boolean provisioned, String createdAt) {
    static AdminAppResponse from(DeveloperApplication app) {
      return new AdminAppResponse(app.getId(), app.getOwner().getEmail(), app.getName(), app.getDescription(), app.getStatus().name(), app.isProvisioned(), app.getCreatedAt().toString());
    }
  }
}
