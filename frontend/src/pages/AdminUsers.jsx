import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { userService } from '../services/userService';
import { Alert } from '../components/Alert';

export const AdminUsers = () => {
  const [users, setUsers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [stats, setStats] = useState({ totalUsers: 0, totalCustomers: 0, totalStaff: 0, totalAdmins: 0 });
  const [selectedRole, setSelectedRole] = useState('');
  const [activeTab, setActiveTab] = useState('users'); // 'users' or 'logs'
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const [userData, logData, statData] = await Promise.all([
        userService.getUsers(selectedRole),
        userService.getAuditLogs(),
        userService.getUserStats(),
      ]);
      setUsers(userData || []);
      setAuditLogs(logData || []);
      setStats(statData || { totalUsers: 0, totalCustomers: 0, totalStaff: 0, totalAdmins: 0 });
    } catch (err) {
      setError(err.message || 'Failed to load user and login data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedRole]);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">User Management & Login Audit</h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Track who logs in as Staff or Customer, view activity timestamps and profiles
          </p>
        </div>
        <button onClick={loadData} className="btn btn-outline btn-sm">
          🔄 Refresh Activity
        </button>
      </div>

      <Alert type="error" message={error} onClose={() => setError('')} />

      {/* Overview Stat Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.25rem', marginBottom: '2rem' }}>
        <div className="card" style={{ borderLeft: '4px solid #059669' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>Total Registered Users</div>
          <div style={{ fontSize: '1.75rem', fontWeight: 900, color: 'var(--text-main)', marginTop: '0.25rem' }}>{stats.totalUsers}</div>
          <div style={{ fontSize: '0.75rem', color: '#059669', marginTop: '0.25rem' }}>Across all system roles</div>
        </div>

        <div className="card" style={{ borderLeft: '4px solid #3b82f6' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>Active Customers</div>
          <div style={{ fontSize: '1.75rem', fontWeight: 900, color: '#3b82f6', marginTop: '0.25rem' }}>{stats.totalCustomers}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>Shoppers & buyers</div>
        </div>

        <div className="card" style={{ borderLeft: '4px solid #f97316' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>Staff Members</div>
          <div style={{ fontSize: '1.75rem', fontWeight: 900, color: '#f97316', marginTop: '0.25rem' }}>{stats.totalStaff}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>Fulfillment & order desk</div>
        </div>

        <div className="card" style={{ borderLeft: '4px solid #8b5cf6' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>Admin Accounts</div>
          <div style={{ fontSize: '1.75rem', fontWeight: 900, color: '#8b5cf6', marginTop: '0.25rem' }}>{stats.totalAdmins}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>Full store managers</div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem' }}>
        <button
          className={`btn btn-sm ${activeTab === 'users' ? 'btn-primary' : 'btn-outline'}`}
          onClick={() => setActiveTab('users')}
        >
          👥 All User Accounts ({users.length})
        </button>
        <button
          className={`btn btn-sm ${activeTab === 'logs' ? 'btn-primary' : 'btn-outline'}`}
          onClick={() => setActiveTab('logs')}
        >
          📜 Live Login & Audit Logs ({auditLogs.length})
        </button>
      </div>

      {/* TAB 1: USERS DIRECTORY */}
      {activeTab === 'users' && (
        <>
          <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem', display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>Filter by Role:</span>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              <button
                className={`pill-btn ${selectedRole === '' ? 'active' : ''}`}
                onClick={() => setSelectedRole('')}
              >
                All Roles
              </button>
              <button
                className={`pill-btn ${selectedRole === 'STAFF' ? 'active' : ''}`}
                onClick={() => setSelectedRole('STAFF')}
              >
                🛡️ Staff Only
              </button>
              <button
                className={`pill-btn ${selectedRole === 'CUSTOMER' ? 'active' : ''}`}
                onClick={() => setSelectedRole('CUSTOMER')}
              >
                🛒 Customers Only
              </button>
              <button
                className={`pill-btn ${selectedRole === 'ADMIN' ? 'active' : ''}`}
                onClick={() => setSelectedRole('ADMIN')}
              >
                👑 Admins
              </button>
            </div>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: '3rem' }}>Loading user data...</div>
          ) : users.length === 0 ? (
            <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
              <h3>No users found for selected role</h3>
            </div>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>User ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Role</th>
                    <th>Account Status</th>
                    <th>Last Active / Login</th>
                    <th>Registered On</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td style={{ fontWeight: 800 }}>#{u.id}</td>
                      <td style={{ fontWeight: 700 }}>{u.name}</td>
                      <td>{u.email}</td>
                      <td>{u.phone || '—'}</td>
                      <td>
                        <span className="user-badge" style={{
                          backgroundColor: u.role === 'ADMIN' ? '#f3e8ff' : u.role === 'STAFF' ? '#ffedd5' : '#ecfdf5',
                          color: u.role === 'ADMIN' ? '#6b21a8' : u.role === 'STAFF' ? '#9a3412' : '#065f46',
                        }}>
                          {u.role}
                        </span>
                      </td>
                      <td>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                          color: u.active ? '#15803d' : '#b91c1c',
                          fontWeight: 700,
                          fontSize: '0.8rem'
                        }}>
                          <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: u.active ? '#15803d' : '#b91c1c' }}></span>
                          {u.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td>
                        {u.lastLoginAt ? (
                          <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                            {new Date(u.lastLoginAt).toLocaleString()}
                          </span>
                        ) : (
                          <span style={{ color: 'var(--text-muted)' }}>Never logged in</span>
                        )}
                      </td>
                      <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                        {new Date(u.createdAt).toLocaleDateString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {/* TAB 2: AUDIT LOGS */}
      {activeTab === 'logs' && (
        <div>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '3rem' }}>Loading activity logs...</div>
          ) : auditLogs.length === 0 ? (
            <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
              <h3>No audit logs recorded yet</h3>
            </div>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Log ID</th>
                    <th>User & Role</th>
                    <th>Action</th>
                    <th>Details</th>
                    <th>Timestamp</th>
                  </tr>
                </thead>
                <tbody>
                  {auditLogs.map((log) => (
                    <tr key={log.id}>
                      <td style={{ fontWeight: 800 }}>#{log.id}</td>
                      <td>
                        {log.userName ? (
                          <div>
                            <div style={{ fontWeight: 700 }}>{log.userName}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                              {log.userEmail} ({log.userRole})
                            </div>
                          </div>
                        ) : (
                          <span style={{ color: 'var(--text-muted)' }}>System Event</span>
                        )}
                      </td>
                      <td>
                        <span style={{
                          background: log.action.includes('LOGIN') ? '#ecfdf5' : '#eff6ff',
                          color: log.action.includes('LOGIN') ? '#065f46' : '#1e40af',
                          padding: '0.2rem 0.5rem',
                          borderRadius: '4px',
                          fontWeight: 800,
                          fontSize: '0.75rem',
                          display: 'inline-block'
                        }}>
                          {log.action}
                        </span>
                      </td>
                      <td style={{ fontSize: '0.85rem' }}>{log.description}</td>
                      <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                        {new Date(log.createdAt).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
