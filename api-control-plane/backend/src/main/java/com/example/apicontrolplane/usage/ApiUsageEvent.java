package com.example.apicontrolplane.usage;

import com.example.apicontrolplane.portal.DeveloperApplication;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_usage_events")
public class ApiUsageEvent {
  @Id
  private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "application_id")
  private DeveloperApplication application;
  @Column(nullable = false)
  private String endpoint;
  @Column(nullable = false)
  private String method;
  @Column(nullable = false)
  private int statusCode;
  @Column(nullable = false)
  private String requestId;
  @Column(nullable = false)
  private Instant occurredAt;

  protected ApiUsageEvent() {}

  public ApiUsageEvent(DeveloperApplication application, String endpoint, String method, int statusCode, String requestId) {
    this.id = UUID.randomUUID();
    this.application = application;
    this.endpoint = endpoint;
    this.method = method;
    this.statusCode = statusCode;
    this.requestId = requestId;
    this.occurredAt = Instant.now();
  }

  public DeveloperApplication getApplication() { return application; }
  public String getEndpoint() { return endpoint; }
  public String getMethod() { return method; }
  public int getStatusCode() { return statusCode; }
  public String getRequestId() { return requestId; }
  public Instant getOccurredAt() { return occurredAt; }
}
