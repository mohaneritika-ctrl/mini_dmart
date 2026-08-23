import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { returnService } from '../services/returnService';
import { Alert } from '../components/Alert';

export const MyReturns = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [cancellingId, setCancellingId] = useState(null);

  const fetchRequests = async () => {
    try {
      setLoading(true);
      const data = await returnService.getMyReturns();
      setRequests(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load returns and exchanges.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this return/exchange request?')) return;
    try {
      setCancellingId(id);
      setError('');
      await returnService.cancelMyReturn(id);
      setSuccess('Request successfully cancelled.');
      await fetchRequests();
    } catch (err) {
      setError(err.message || 'Failed to cancel request.');
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">My Returns & Exchanges</h1>
        <Link to="/orders" className="btn btn-primary btn-sm">View Orders</Link>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>Loading your return requests...</div>
      ) : requests.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🔄</div>
          <h2>No Return or Exchange Requests</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
            You haven't requested any product returns or exchanges.
          </p>
          <Link to="/orders" className="btn btn-primary">Check Delivered Orders</Link>
        </div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Request #</th>
                <th>Order #</th>
                <th>Product</th>
                <th>Type</th>
                <th>Qty</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Date</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((req) => (
                <tr key={req.id}>
                  <td style={{ fontWeight: 700 }}>#{req.id}</td>
                  <td>
                    <Link to={`/orders/${req.orderId}`} style={{ color: 'var(--primary)', fontWeight: 600 }}>
                      #{req.orderId}
                    </Link>
                  </td>
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
                    {req.staffComment && (
                      <div style={{ fontSize: '0.75rem', color: '#b91c1c', marginTop: '0.25rem' }}>
                        Staff remark: {req.staffComment}
                      </div>
                    )}
                  </td>
                  <td>
                    <span className={`badge badge-${req.status}`}>{req.status}</span>
                  </td>
                  <td>{new Date(req.createdAt).toLocaleDateString()}</td>
                  <td>
                    {(req.status === 'REQUESTED' || req.status === 'PENDING') && (
                      <button
                        onClick={() => handleCancel(req.id)}
                        disabled={cancellingId === req.id}
                        className="btn btn-danger btn-sm"
                      >
                        {cancellingId === req.id ? 'Cancelling...' : 'Cancel'}
                      </button>
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