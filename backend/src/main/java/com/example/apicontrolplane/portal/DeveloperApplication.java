package com.example.apicontrolplane.portal;

import com.example.apicontrolplane.user.UserAccount;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "developer_applications")
public class DeveloperApplication {
  @Id
  private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id")
  private UserAccount owner;
  @Column(nullable = false)
  private String name;
  private String description;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationStatus status;
  @Column(nullable = false)
  private boolean provisioned;
  @Column(nullable = false)
  private Instant createdAt;
  @Column(nullable = false)
  private Instant updatedAt;

  protected DeveloperApplication() {}

  public DeveloperApplication(UserAccount owner, String name, String description) {
    this.id = UUID.randomUUID();
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.status = ApplicationStatus.PENDING_REVIEW;
    this.provisioned = false;
    this.createdAt = Instant.now();
    this.updatedAt = createdAt;
  }

  public void update(String name, String description) {
    if (provisioned) {
      throw new IllegalStateException("Provisioned applications cannot be edited");
    }
    this.name = name;
    this.description = description;
    this.updatedAt = Instant.now();
  }

  public void approve() {
    if (!provisioned) this.status = ApplicationStatus.APPROVED;
  }

  public void reject() {
    if (!provisioned) this.status = ApplicationStatus.REJECTED;
  }

  public void markProvisioned() {
    if (status != ApplicationStatus.APPROVED) {
      throw new IllegalStateException("Only approved applications can be provisioned");
    }
    this.provisioned = true;
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UserAccount getOwner() { return owner; }
  public String getName() { return name; }
  public String getDescription() { return description; }
  public ApplicationStatus getStatus() { return status; }
  public boolean isProvisioned() { return provisioned; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
