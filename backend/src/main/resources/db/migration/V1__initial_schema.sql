CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  role VARCHAR(40) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE developer_applications (
  id UUID PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES users(id),
  name VARCHAR(120) NOT NULL,
  description TEXT,
  status VARCHAR(40) NOT NULL,
  provisioned BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE api_credentials (
  id UUID PRIMARY KEY,
  application_id UUID NOT NULL REFERENCES developer_applications(id) ON DELETE CASCADE,
  client_id VARCHAR(80) NOT NULL UNIQUE,
  client_secret_hash VARCHAR(255) NOT NULL,
  last_rotated_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE api_usage_events (
  id UUID PRIMARY KEY,
  application_id UUID NOT NULL REFERENCES developer_applications(id) ON DELETE CASCADE,
  endpoint VARCHAR(160) NOT NULL,
  method VARCHAR(12) NOT NULL,
  status_code INTEGER NOT NULL,
  request_id VARCHAR(80) NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_developer_applications_owner ON developer_applications(owner_id);
CREATE INDEX idx_usage_application_occurred ON api_usage_events(application_id, occurred_at DESC);
