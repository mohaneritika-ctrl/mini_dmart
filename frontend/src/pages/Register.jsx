import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Alert } from '../components/Alert';

export const Register = () => {
  const [formData, setFormData] = useState({ name: '', email: '', password: '', phone: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(formData);
      navigate('/');
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card form-card">
      <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
        <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>✨</div>
        <h2 style={{ fontWeight: 900, fontSize: '1.65rem', color: 'var(--text-main)' }}>Create Account</h2>
        <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Join Mini D-Mart for fresh grocery deliveries</p>
      </div>

      <Alert type="error" message={error} onClose={() => setError('')} />

      <form onSubmit={handleSubmit} autoComplete="off">
        <div className="form-group">
          <label className="form-label">Full Name</label>
          <input
            type="text"
            name="name"
            required
            autoComplete="off"
            className="form-control"
            value={formData.name}
            onChange={handleChange}
            placeholder="Your Name"
          />
        </div>

        <div className="form-group">
          <label className="form-label">Email Address</label>
          <input
            type="text"
            name="email"
            required
            autoComplete="off"
            className="form-control"
            value={formData.email}
            onChange={handleChange}
            placeholder="name@example.com"
          />
        </div>

        <div className="form-group">
          <label className="form-label">Password</label>
          <input
            type="password"
            name="password"
            required
            minLength={6}
            autoComplete="new-password"
            className="form-control"
            value={formData.password}
            onChange={handleChange}
            placeholder="At least 6 characters"
          />
        </div>

        <div className="form-group">
          <label className="form-label">Phone Number (Optional)</label>
          <input
            type="tel"
            name="phone"
            autoComplete="off"
            className="form-control"
            value={formData.phone}
            onChange={handleChange}
            placeholder="10-digit mobile number"
          />
        </div>

        <button type="submit" disabled={loading} className="btn btn-primary btn-block btn-lg" style={{ marginTop: '0.5rem' }}>
          {loading ? 'Creating Account...' : 'Register'}
        </button>
      </form>

      <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
        Already have an account?{' '}
        <Link to="/login" style={{ color: 'var(--primary)', fontWeight: 800 }}>
          Sign in here
        </Link>
      </p>
    </div>
  );
};