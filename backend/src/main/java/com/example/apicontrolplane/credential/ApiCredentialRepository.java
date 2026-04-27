package com.example.apicontrolplane.credential;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiCredentialRepository extends JpaRepository<ApiCredential, UUID> {
  @EntityGraph(attributePaths = {"application"})
  Optional<ApiCredential> findByApplicationId(UUID applicationId);

  @EntityGraph(attributePaths = {"application"})
  Optional<ApiCredential> findByClientId(String clientId);
}
