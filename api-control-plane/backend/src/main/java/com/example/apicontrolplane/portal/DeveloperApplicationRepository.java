package com.example.apicontrolplane.portal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperApplicationRepository extends JpaRepository<DeveloperApplication, UUID> {
  List<DeveloperApplication> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
  Optional<DeveloperApplication> findByIdAndOwnerId(UUID id, UUID ownerId);

  @EntityGraph(attributePaths = {"owner"})
  List<DeveloperApplication> findAllByOrderByCreatedAtDesc();
}
