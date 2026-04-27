package com.example.apicontrolplane.usage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApiUsageRepository extends JpaRepository<ApiUsageEvent, UUID> {
  List<ApiUsageEvent> findTop50ByApplicationIdOrderByOccurredAtDesc(UUID applicationId);

  @Query("select count(e) from ApiUsageEvent e where e.application.id = :applicationId and e.occurredAt >= :since")
  long countSince(UUID applicationId, Instant since);
}
