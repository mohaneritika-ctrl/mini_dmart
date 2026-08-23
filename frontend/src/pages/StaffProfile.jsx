import React, { useEffect, useState } from 'react';
import { userService } from '../services/userService';
import { Alert } from '../components/Alert';

export const StaffProfile = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({ name: '', phone: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        const data = await userService.getProfile();
        setProfile(data);
        setFormData({ name: data.name || '', phone: data.phone || '' });
      } catch (err) {
        setError(err.message || 'Failed to load profile details.');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleUpdate = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      setSaving(true);
      const updated = await userService.updateProfile(formData);
      setProfile((prev) => ({ ...prev, ...updated }));
      setSuccess('Profile updated successfully!');
      setEditing(false);
    } catch (err) {
      setError(err.message || 'Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '4rem' }}>Loading profile...</div>;
  if (!profile) return <div style={{ textAlign: 'center', padding: '4rem' }}>Profile not found.</div>;

  const isStaff = profile.role === 'STAFF';

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div className="page-header">
        <div>
          <h1 className="page-title">{isStaff ? '🛡️ Staff Employee Profile' : '👤 My Profile & Account'}</h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            {isStaff ? 'Store staff member information, shift status, and activity' : 'Personal account details and order records'}
          </p>
        </div>
        {!editing && (
          <button onClick={() => setEditing(true)} className="btn btn-outline btn-sm">
            ✏️ Edit Profile
          </button>
        )}
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {/* Profile ID Card */}
      <div className="card" style={{ marginBottom: '2rem', background: 'linear-gradient(135deg, #ffffff 0%, #f8fafc 100%)', border: '1.5px solid var(--border)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', flexWrap: 'wrap', borderBottom: '1px solid var(--border)', paddingBottom: '1.5rem', marginBottom: '1.5rem' }}>
          <div style={{
            width: '80px',
            height: '80px',
            borderRadius: '50%',
            background: isStaff ? 'linear-gradient(135deg, #f97316, #ea580c)' : 'linear-gradient(135deg, #059669, #047857)',
            color: 'white',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '2.25rem',
            fontWeight: 800,
            boxShadow: '0 4px 10px rgba(0,0,0,0.15)'
          }}>
            {profile.name ? profile.name.charAt(0).toUpperCase() : 'U'}
          </div>

          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.3rem' }}>
              <h2 style={{ fontSize: '1.45rem', fontWeight: 900, color: 'var(--text-main)' }}>{profile.name}</h2>
              <span className="user-badge" style={{
                background: isStaff ? '#ffedd5' : '#ecfdf5',
                color: isStaff ? '#9a3412' : '#065f46'
              }}>
                {profile.role}
              </span>
            </div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>{profile.email}</div>
          </div>
        </div>

        {/* Details Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem' }}>
          <div>
            <div style={{ fontSize: '0.75rem', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Employee / User ID</div>
            <div style={{ fontWeight: 800, fontSize: '1.1rem', marginTop: '0.2rem' }}>DMART-USR-{profile.id}</div>
          </div>

          <div>
            <div style={{ fontSize: '0.75rem', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Phone Number</div>
            <div style={{ fontWeight: 700, fontSize: '1rem', marginTop: '0.2rem' }}>{profile.phone || 'Not provided'}</div>
          </div>

          <div>
            <div style={{ fontSize: '0.75rem', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Last Active / Login</div>
            <div style={{ fontWeight: 700, fontSize: '0.95rem', marginTop: '0.2rem' }}>
              {profile.lastLoginAt ? new Date(profile.lastLoginAt).toLocaleString() : 'Just now'}
            </div>
          </div>

          <div>
            <div style={{ fontSize: '0.75rem', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Account Created</div>
            <div style={{ fontWeight: 700, fontSize: '0.95rem', marginTop: '0.2rem' }}>
              {profile.createdAt ? new Date(profile.createdAt).toLocaleDateString() : 'N/A'}
            </div>
          </div>

          {isStaff && (
            <>
              <div>
                <div style={{ fontSize: '0.75rem', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Assigned Department</div>
                <div style={{ fontWeight: 800, color: 'var(--primary-dark)', fontSize: '1rem', marginTop: '0.2rem' }}>
                  🏬 {profile.assignedDepartment || 'Fulfillment Desk'}
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Current Duty Status</div>
                <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: '#15803d', fontWeight: 800, marginTop: '0.2rem' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#15803d' }}></span>
                  ON DUTY (Active)
                </div>
              </div>
            </>
          )}

          {profile.role === 'CUSTOMER' && (
            <div>
              <div style={{ fontSize: '0.75rem', fontWeight: 800, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Total Orders Placed</div>
              <div style={{ fontWeight: 900, color: 'var(--primary-dark)', fontSize: '1.25rem', marginTop: '0.2rem' }}>
                🛒 {profile.totalOrdersPlaced || 0} Orders
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Edit Form Modal/Card */}
      {editing && (
        <div className="card" style={{ border: '2px solid var(--primary)', marginBottom: '2rem' }}>
          <h3 style={{ fontSize: '1.15rem', fontWeight: 800, marginBottom: '1rem' }}>Edit Personal Details</h3>
          <form onSubmit={handleUpdate}>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                type="text"
                required
                className="form-control"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <input
                type="tel"
                className="form-control"
                placeholder="10-digit mobile number"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              />
            </div>

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <button type="submit" disabled={saving} className="btn btn-primary btn-sm">
                {saving ? 'Saving...' : 'Save Changes'}
              </button>
              <button type="button" onClick={() => setEditing(false)} className="btn btn-outline btn-sm">
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
