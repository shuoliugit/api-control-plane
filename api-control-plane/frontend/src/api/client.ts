export type Role = 'DEVELOPER' | 'ADMIN';
export type AppStatus = 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED';

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  displayName: string;
  role: Role;
}

export interface DeveloperApp {
  id: string;
  name: string;
  description?: string;
  status: AppStatus;
  provisioned: boolean;
  createdAt: string;
  updatedAt?: string;
  ownerEmail?: string;
}

export interface CredentialResponse {
  clientId: string;
  clientSecret: string;
  lastRotatedAt: string;
}

export interface UsageEvent {
  endpoint: string;
  method: string;
  statusCode: number;
  requestId: string;
  occurredAt: string;
}

const baseUrl = import.meta.env.VITE_API_URL ?? '';

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    }
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    throw new Error(payload.message ?? `Request failed with ${response.status}`);
  }

  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  register: (email: string, password: string, displayName: string) =>
    request<AuthResponse>('/api/auth/register', { method: 'POST', body: JSON.stringify({ email, password, displayName }) }),
  login: (email: string, password: string) =>
    request<AuthResponse>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  listApps: (token: string) => request<DeveloperApp[]>('/api/apps', {}, token),
  createApp: (token: string, name: string, description: string) =>
    request<DeveloperApp>('/api/apps', { method: 'POST', body: JSON.stringify({ name, description }) }, token),
  updateApp: (token: string, id: string, name: string, description: string) =>
    request<DeveloperApp>(`/api/apps/${id}`, { method: 'PUT', body: JSON.stringify({ name, description }) }, token),
  deleteApp: (token: string, id: string) => request<void>(`/api/apps/${id}`, { method: 'DELETE' }, token),
  generateCredentials: (token: string, id: string) =>
    request<CredentialResponse>(`/api/apps/${id}/credentials`, { method: 'POST' }, token),
  rotateCredentials: (token: string, id: string) =>
    request<CredentialResponse>(`/api/apps/${id}/credentials/rotate`, { method: 'POST' }, token),
  usage: (token: string, id: string) => request<UsageEvent[]>(`/api/apps/${id}/usage`, {}, token),
  adminApps: (token: string) => request<DeveloperApp[]>('/api/admin/apps', {}, token),
  approve: (token: string, id: string) => request<DeveloperApp>(`/api/admin/apps/${id}/approve`, { method: 'POST' }, token),
  reject: (token: string, id: string) => request<DeveloperApp>(`/api/admin/apps/${id}/reject`, { method: 'POST' }, token),
  provision: (token: string, id: string) => request<DeveloperApp>(`/api/admin/apps/${id}/provision`, { method: 'POST' }, token)
};
