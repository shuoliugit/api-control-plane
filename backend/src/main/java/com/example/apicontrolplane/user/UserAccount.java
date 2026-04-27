package com.example.apicontrolplane.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserAccount {
  @Id
  private UUID id;
  @Column(nullable = false, unique = true)
  private String email;
  @Column(nullable = false)
  private String passwordHash;
  @Column(nullable = false)
  private String displayName;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;
  @Column(nullable = false)
  private Instant createdAt;

  protected UserAccount() {}

  public UserAccount(String email, String passwordHash, String displayName, Role role) {
    this.id = UUID.randomUUID();
    this.email = email.toLowerCase();
    this.passwordHash = passwordHash;
    this.displayName = displayName;
    this.role = role;
    this.createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; }
  public String getDisplayName() { return displayName; }
  public Role getRole() { return role; }
}
