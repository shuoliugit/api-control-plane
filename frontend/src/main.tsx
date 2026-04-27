import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Activity, CheckCircle2, KeyRound, LogOut, Plus, RefreshCcw, ShieldCheck, Trash2, XCircle } from 'lucide-react';
import { api, AuthResponse, CredentialResponse, DeveloperApp, UsageEvent } from './api/client';
import './styles/app.css';

function App() {
  const [session, setSession] = useState<AuthResponse | null>(() => {
    const saved = localStorage.getItem('control-plane-session');
    return saved ? JSON.parse(saved) : null;
  });

  useEffect(() => {
    if (session) localStorage.setItem('control-plane-session', JSON.stringify(session));
    else localStorage.removeItem('control-plane-session');
  }, [session]);

  if (!session) return <AuthScreen onSession={setSession} />;

  return (
    <Shell session={session} onLogout={() => setSession(null)}>
      <DeveloperPortal token={session.token} />
      {session.role === 'ADMIN' && <AdminPortal token={session.token} />}
    </Shell>
  );
}

function AuthScreen({ onSession }: { onSession: (session: AuthResponse) => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('dev@example.com');
  const [password, setPassword] = useState('Password123!');
  const [displayName, setDisplayName] = useState('Developer User');
  const [error, setError] = useState('');

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setError('');
    try {
      onSession(mode === 'login' ? await api.login(email, password) : await api.register(email, password, displayName));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Authentication failed');
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-panel">
        <div>
          <p className="eyebrow">Developer Platform</p>
          <h1>API Control Plane</h1>
          <p className="lede">Register applications, issue credentials, provision access, and inspect sandbox traffic from one operational console.</p>
        </div>
        <form onSubmit={submit} className="form">
          <div className="segmented">
            <button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Login</button>
            <button type="button" className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Register</button>
          </div>
          {mode === 'register' && <Field label="Display name" value={displayName} onChange={setDisplayName} />}
          <Field label="Email" value={email} onChange={setEmail} type="email" />
          <Field label="Password" value={password} onChange={setPassword} type="password" />
          {error && <p className="error">{error}</p>}
          <button className="primary" type="submit">{mode === 'login' ? 'Sign in' : 'Create account'}</button>
        </form>
      </section>
    </main>
  );
}

function Shell({ session, onLogout, children }: { session: AuthResponse; onLogout: () => void; children: React.ReactNode }) {
  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">API Control Plane</p>
          <h1>Platform Console</h1>
        </div>
        <div className="identity">
          <span>{session.email}</span>
          <span className="pill">{session.role}</span>
          <button className="icon-button" onClick={onLogout} title="Log out"><LogOut size={18} /></button>
        </div>
      </header>
      {children}
    </div>
  );
}

function DeveloperPortal({ token }: { token: string }) {
  const [apps, setApps] = useState<DeveloperApp[]>([]);
  const [selected, setSelected] = useState<DeveloperApp | null>(null);
  const [usage, setUsage] = useState<UsageEvent[]>([]);
  const [credential, setCredential] = useState<CredentialResponse | null>(null);
  const [error, setError] = useState('');
  const [name, setName] = useState('Treasury Sandbox');
  const [description, setDescription] = useState('Payment and account API integration validation.');

  async function load() {
    const data = await api.listApps(token);
    setApps(data);
    setSelected((current) => current ? data.find((app) => app.id === current.id) ?? data[0] ?? null : data[0] ?? null);
  }

  useEffect(() => { load().catch((err) => setError(err.message)); }, [token]);
  useEffect(() => {
    if (selected) api.usage(token, selected.id).then(setUsage).catch(() => setUsage([]));
  }, [selected, token]);

  async function createApp(event: React.FormEvent) {
    event.preventDefault();
    setError('');
    try {
      await api.createApp(token, name, description);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create application');
    }
  }

  async function mutate(action: () => Promise<unknown>) {
    setError('');
    try {
      await action();
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Action failed');
    }
  }

  return (
    <section className="workspace">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Developer Portal</p>
          <h2>Applications</h2>
        </div>
      </div>
      <div className="grid two">
        <form className="panel form" onSubmit={createApp}>
          <h3>Register application</h3>
          <Field label="Name" value={name} onChange={setName} />
          <label>Description<textarea value={description} onChange={(event) => setDescription(event.target.value)} /></label>
          {error && <p className="error">{error}</p>}
          <button className="primary" type="submit"><Plus size={16} /> Create app</button>
        </form>
        <div className="panel">
          <h3>Registered apps</h3>
          <div className="app-list">
            {apps.map((app) => (
              <button key={app.id} className={selected?.id === app.id ? 'app-row selected' : 'app-row'} onClick={() => setSelected(app)}>
                <span><strong>{app.name}</strong><small>{app.status} {app.provisioned ? '• provisioned' : ''}</small></span>
                <Status status={app.status} />
              </button>
            ))}
            {!apps.length && <p className="muted">No applications yet.</p>}
          </div>
        </div>
      </div>
      {selected && (
        <div className="grid two">
          <div className="panel">
            <h3>{selected.name}</h3>
            <p className="muted">{selected.description}</p>
            <div className="actions">
              <button onClick={() => mutate(async () => setCredential(await api.generateCredentials(token, selected.id)))}><KeyRound size={16} /> Generate credentials</button>
              <button onClick={() => mutate(async () => setCredential(await api.rotateCredentials(token, selected.id)))}><RefreshCcw size={16} /> Rotate secret</button>
              <button disabled={selected.provisioned} onClick={() => mutate(() => api.deleteApp(token, selected.id))}><Trash2 size={16} /> Delete</button>
            </div>
            {credential && <div className="secret-box"><code>{credential.clientId}</code><code>{credential.clientSecret}</code><small>Secret is shown once. Store it in a vault.</small></div>}
          </div>
          <div className="panel">
            <h3><Activity size={18} /> Sandbox usage</h3>
            <UsageTable usage={usage} />
          </div>
        </div>
      )}
    </section>
  );
}

function AdminPortal({ token }: { token: string }) {
  const [apps, setApps] = useState<DeveloperApp[]>([]);
  const [error, setError] = useState('');
  const load = () => api.adminApps(token).then(setApps).catch((err) => setError(err.message));
  useEffect(() => { load(); }, [token]);

  async function action(fn: () => Promise<unknown>) {
    setError('');
    try {
      await fn();
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Admin action failed');
    }
  }

  return (
    <section className="workspace">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Admin Portal</p>
          <h2>Provisioning queue</h2>
        </div>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="panel table-panel">
        <table>
          <thead><tr><th>Application</th><th>Owner</th><th>Status</th><th>Provisioned</th><th>Actions</th></tr></thead>
          <tbody>
            {apps.map((app) => (
              <tr key={app.id}>
                <td>{app.name}</td>
                <td>{app.ownerEmail}</td>
                <td><Status status={app.status} /></td>
                <td>{app.provisioned ? 'Yes' : 'No'}</td>
                <td className="actions compact">
                  <button disabled={app.provisioned} onClick={() => action(() => api.approve(token, app.id))}><CheckCircle2 size={15} /> Approve</button>
                  <button disabled={app.provisioned} onClick={() => action(() => api.reject(token, app.id))}><XCircle size={15} /> Reject</button>
                  <button disabled={app.provisioned || app.status !== 'APPROVED'} onClick={() => action(() => api.provision(token, app.id))}><ShieldCheck size={15} /> Provision</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function UsageTable({ usage }: { usage: UsageEvent[] }) {
  const rows = useMemo(() => usage.slice(0, 8), [usage]);
  if (!rows.length) return <p className="muted">No sandbox calls logged yet.</p>;
  return (
    <table>
      <tbody>
        {rows.map((event) => (
          <tr key={event.requestId}>
            <td>{event.method}</td>
            <td>{event.endpoint}</td>
            <td>{event.statusCode}</td>
            <td>{new Date(event.occurredAt).toLocaleString()}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function Field({ label, value, onChange, type = 'text' }: { label: string; value: string; onChange: (value: string) => void; type?: string }) {
  return <label>{label}<input value={value} type={type} onChange={(event) => onChange(event.target.value)} /></label>;
}

function Status({ status }: { status: string }) {
  return <span className={`status ${status.toLowerCase()}`}>{status.replace('_', ' ')}</span>;
}

createRoot(document.getElementById('root')!).render(<App />);
