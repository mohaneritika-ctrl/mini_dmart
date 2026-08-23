import React, { useEffect, useState } from 'react';
import { returnService } from '../services/returnService';
import { Alert } from '../components/Alert';

export const AdminReturns = () => {
  const [returns, setReturns] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [updatingId, setUpdatingId] = useState(null);

  const fetchReturns = async () => {
    try {
      setLoading(true);
      const data = await returnService.getAdminReturns(statusFilter, typeFilter);
      setReturns(data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch return requests.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReturns();
  }, [statusFilter, typeFilter]);

  const handleStatusChange = async (id, newStatus, type) => {
    try {
      setUpdatingId(id);
      setError('');
      await returnService.updateReturnStatusByAdmin(id, { status: newStatus });
      setSuccess(`${type} request #${id} status updated to ${newStatus}`);
      await fetchReturns();
    } catch (err) {
      setError(err.message || 'Failed to update request status.');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Global Return & Exchange Management</h1>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <select
            className="form-select"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            style={{ width: 'auto' }}
          >
            <option value="">All Types</option>
            <option value="RETURN">RETURN</option>
            <option value="EXCHANGE">EXCHANGE</option>
          </select>
          <select
            className="form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{ width: 'auto' }}
          >
            <option value="">All Statuses</option>
            <option value="REQUESTED">REQUESTED</option>
            <option value="APPROVED">APPROVED</option>
            <option value="REJECTED">REJECTED</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </div>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>Loading returns data...</div>
      ) : returns.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
          <h3>No return/exchange requests found</h3>
        </div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Req #</th>
                <th>Customer</th>
                <th>Order #</th>
                <th>Product</th>
                <th>Type</th>
                <th>Qty</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Status Control</th>
              </tr>
            </thead>
            <tbody>
              {returns.map((req) => (
                <tr key={req.id}>
                  <td style={{ fontWeight: 700 }}>#{req.id}</td>
                  <td>{req.customerName || `User #${req.userId}`}</td>
                  <td>#{req.orderId}</td>
                  <td style={{ fontWeight: 700 }}>{req.productName}</td>
                  <td>
                    <span className="badge" style={{ backgroundColor: req.type === 'EXCHANGE' ? '#e0e7ff' : '#fef3c7', color: req.type === 'EXCHANGE' ? '#3730a3' : '#92400e' }}>
                      {req.type}
                    </span>
                  </td>
                  <td style={{ fontWeight: 800 }}>{req.quantity}</td>
                  <td>
                    <div>{req.reason}</div>
                    {req.note && <small style={{ color: 'var(--text-muted)' }}>Note: {req.note}</small>}
                    {req.staffComment && <div style={{ fontSize: '0.75rem', color: '#b91c1c' }}>Staff: {req.staffComment}</div>}
                  </td>
                  <td>
                    <span className={`badge badge-${req.status}`}>{req.status}</span>
                  </td>
                  <td>
                    {req.status === 'COMPLETED' || req.status === 'REJECTED' || req.status === 'CANCELLED' ? (
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Terminal State</span>
                    ) : (
                      <select
                        className="form-select"
                        style={{ fontSize: '0.8rem', padding: '0.3rem 0.5rem' }}
                        value={req.status}
                        disabled={updatingId === req.id}
                        onChange={(e) => handleStatusChange(req.id, e.target.value, req.type)}
                      >
                        <option value={req.status} disabled>{req.status}</option>
                        {(req.status === 'REQUESTED' || req.status === 'PENDING') && <option value="APPROVED">APPROVED</option>}
                        {req.status === 'APPROVED' && <option value="COMPLETED">COMPLETED (Update Stock)</option>}
                        <option value="REJECTED">REJECTED</option>
                        <option value="CANCELLED">CANCELLED</option>
                      </select>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};