import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Alert } from '../components/Alert';

export const Login = () => {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const executeLogin = async (credentials) => {
    setError('');
    setLoading(true);
    try {
      const user = await login(credentials);
      if (user.role === 'ADMIN') navigate('/admin');
      else if (user.role === 'STAFF') navigate('/staff');
      else navigate('/');
    } catch (err) {
      setError(err.message || 'Login failed. Please check credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await executeLogin(formData);
  };

  const handleQuickLogin = (email, password) => {
    setFormData({ email, password });
    executeLogin({ email, password });
  };

  return (
    <div className="card form-card">
      <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
        <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>🛒</div>
        <h2 style={{ fontWeight: 900, fontSize: '1.65rem', color: 'var(--text-main)' }}>Welcome Back</h2>
        <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Sign in to continue to Mini D-Mart</p>
      </div>

      <Alert type="error" message={error} onClose={() => setError('')} />

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Email Address</label>
          <input
            type="email"
            name="email"
            required
            className="form-control"
            value={formData.email}
            onChange={handleChange}
            placeholder="admin@dmart.com"
          />
        </div>

        <div className="form-group">
          <label className="form-label">Password</label>
          <input
            type="password"
            name="password"
            required
            className="form-control"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
          />
        </div>

        <button type="submit" disabled={loading} className="btn btn-primary btn-block btn-lg" style={{ marginTop: '0.5rem' }}>
          {loading ? 'Signing In...' : 'Sign In'}
        </button>
      </form>

      {/* Quick Demo Logins */}
      <div style={{ marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px dashed var(--border)' }}>
        <p style={{ textAlign: 'center', fontSize: '0.8rem', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '0.75rem' }}>
          ⚡ 1-Click Quick Demo Login
        </p>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.5rem' }}>
          <button
            type="button"
            className="btn btn-outline btn-sm"
            onClick={() => handleQuickLogin('customer@dmart.com', 'customer123')}
            style={{ fontSize: '0.75rem', padding: '0.4rem 0.2rem' }}
          >
            🛒 Customer
          </button>
          <button
            type="button"
            className="btn btn-outline btn-sm"
            onClick={() => handleQuickLogin('staff@dmart.com', 'staff123')}
            style={{ fontSize: '0.75rem', padding: '0.4rem 0.2rem' }}
          >
            🛡️ Staff
          </button>
          <button
            type="button"
            className="btn btn-outline btn-sm"
            onClick={() => handleQuickLogin('admin@dmart.com', 'admin123')}
            style={{ fontSize: '0.75rem', padding: '0.4rem 0.2rem' }}
          >
            👑 Admin
          </button>
        </div>
      </div>

      <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
        Don't have an account?{' '}
        <Link to="/register" style={{ color: 'var(--primary)', fontWeight: 800 }}>
          Create an account
        </Link>
      </p>
    </div>
  );
};