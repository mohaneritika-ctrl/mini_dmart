import React, { useEffect, useState } from 'react';
import { orderService } from '../services/orderService';
import { Alert } from '../components/Alert';

export const AdminOrders = () => {
  const [orders, setOrders] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [updatingId, setUpdatingId] = useState(null);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const data = await orderService.getAdminOrders(statusFilter);
      setOrders(data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch admin orders.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [statusFilter]);

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      setUpdatingId(orderId);
      setError('');
      await orderService.updateOrderStatusByAdmin(orderId, newStatus);
      setSuccess(`Order #${orderId} status successfully updated to ${newStatus}`);
      await fetchOrders();
    } catch (err) {
      setError(err.message || 'Failed to update order status.');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Admin Order Management</h1>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <label style={{ fontSize: '0.875rem', fontWeight: 600 }}>Filter Status:</label>
          <select
            className="form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{ width: 'auto' }}
          >
            <option value="">All Orders</option>
            <option value="PLACED">PLACED</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="PREPARING">PREPARING</option>
            <option value="READY_FOR_PICKUP">READY FOR PICKUP</option>
            <option value="OUT_FOR_DELIVERY">OUT FOR DELIVERY</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </div>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>Loading orders...</div>
      ) : orders.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
          <h3>No store orders found</h3>
        </div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Order #</th>
                <th>Customer ID</th>
                <th>Date</th>
                <th>Type</th>
                <th>Items</th>
                <th>Total</th>
                <th>Status</th>
                <th>Status Control</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td style={{ fontWeight: 700 }}>#{order.id}</td>
                  <td>User #{order.userId}</td>
                  <td>{new Date(order.createdAt).toLocaleString()}</td>
                  <td>{order.orderType}</td>
                  <td>{order.items?.length || 0} line item(s)</td>
                  <td style={{ fontWeight: 800 }}>₹{Number(order.totalAmount).toFixed(2)}</td>
                  <td>
                    <span className={`badge badge-${order.orderStatus}`}>{order.orderStatus}</span>
                  </td>
                  <td>
                    {order.orderStatus === 'COMPLETED' || order.orderStatus === 'CANCELLED' ? (
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Terminal State</span>
                    ) : (
                      <select
                        className="form-select"
                        style={{ fontSize: '0.8rem', padding: '0.3rem 0.5rem' }}
                        value={order.orderStatus}
                        disabled={updatingId === order.id}
                        onChange={(e) => handleStatusChange(order.id, e.target.value)}
                      >
                        <option value={order.orderStatus} disabled>{order.orderStatus}</option>
                        {order.orderStatus === 'CONFIRMED' && <option value="PREPARING">PREPARING</option>}
                        {order.orderStatus === 'PREPARING' && <option value="READY_FOR_PICKUP">READY_FOR_PICKUP</option>}
                        {order.orderStatus === 'PREPARING' && <option value="OUT_FOR_DELIVERY">OUT_FOR_DELIVERY</option>}
                        {order.orderStatus === 'READY_FOR_PICKUP' && <option value="COMPLETED">COMPLETED</option>}
                        {order.orderStatus === 'OUT_FOR_DELIVERY' && <option value="COMPLETED">COMPLETED</option>}
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
