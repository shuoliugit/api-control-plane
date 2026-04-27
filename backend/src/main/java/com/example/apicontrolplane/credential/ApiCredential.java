package com.example.apicontrolplane.credential;

import com.example.apicontrolplane.portal.DeveloperApplication;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_credentials")
public class ApiCredential {
  @Id
  private UUID id;
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "application_id")
  private DeveloperApplication application;
  @Column(nullable = false, unique = true)
  private String clientId;
  @Column(nullable = false)
  private String clientSecretHash;
  @Column(nullable = false)
  private Instant lastRotatedAt;
  @Column(nullable = false)
  private Instant createdAt;

  protected ApiCredential() {}

  public ApiCredential(DeveloperApplication application, String clientId, String clientSecretHash) {
    this.id = UUID.randomUUID();
    this.application = application;
    this.clientId = clientId;
    this.clientSecretHash = clientSecretHash;
    this.createdAt = Instant.now();
    this.lastRotatedAt = createdAt;
  }

  public void rotate(String hash) {
    this.clientSecretHash = hash;
    this.lastRotatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public DeveloperApplication getApplication() { return application; }
  public String getClientId() { return clientId; }
  public String getClientSecretHash() { return clientSecretHash; }
  public Instant getLastRotatedAt() { return lastRotatedAt; }
}
