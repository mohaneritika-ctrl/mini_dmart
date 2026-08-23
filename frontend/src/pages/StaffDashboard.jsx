import React, { useEffect, useState } from 'react';
import { orderService } from '../services/orderService';
import { returnService } from '../services/returnService';
import { Alert } from '../components/Alert';

export const StaffDashboard = () => {
  const [activeTab, setActiveTab] = useState('orders');

  // Orders state
  const [orders, setOrders] = useState([]);
  const [orderStatusFilter, setOrderStatusFilter] = useState('');
  const [loadingOrders, setLoadingOrders] = useState(true);
  const [updatingOrderId, setUpdatingOrderId] = useState(null);

  // Returns & Exchanges state
  const [returns, setReturns] = useState([]);
  const [returnStatusFilter, setReturnStatusFilter] = useState('');
  const [returnTypeFilter, setReturnTypeFilter] = useState('');
  const [loadingReturns, setLoadingReturns] = useState(true);
  const [updatingReturnId, setUpdatingReturnId] = useState(null);

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchOrders = async () => {
    try {
      setLoadingOrders(true);
      const data = await orderService.getStaffOrders(orderStatusFilter);
      setOrders(data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch orders.');
    } finally {
      setLoadingOrders(false);
    }
  };

  const fetchReturns = async () => {
    try {
      setLoadingReturns(true);
      const data = await returnService.getStaffReturns(returnStatusFilter, returnTypeFilter);
      setReturns(data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch returns/exchanges.');
    } finally {
      setLoadingReturns(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'orders') fetchOrders();
    else fetchReturns();
  }, [activeTab, orderStatusFilter, returnStatusFilter, returnTypeFilter]);

  const handleOrderStatusChange = async (orderId, newStatus) => {
    try {
      setUpdatingOrderId(orderId);
      setError('');
      await orderService.updateOrderStatusByStaff(orderId, newStatus);
      setSuccess(`Order #${orderId} status updated to ${newStatus}`);
      await fetchOrders();
    } catch (err) {
      setError(err.message || 'Failed to update order status.');
    } finally {
      setUpdatingOrderId(null);
    }
  };

  const handleApproveReturn = async (id) => {
    try {
      setUpdatingReturnId(id);
      setError('');
      await returnService.approveReturnByStaff(id);
      setSuccess(`Return/Exchange request #${id} APPROVED`);
      await fetchReturns();
    } catch (err) {
      setError(err.message || 'Failed to approve request.');
    } finally {
      setUpdatingReturnId(null);
    }
  };

  const handleRejectReturn = async (id) => {
    const comment = window.prompt('Enter rejection reason for customer:', 'Ineligible return');
    if (comment === null) return;
    try {
      setUpdatingReturnId(id);
      setError('');
      await returnService.rejectReturnByStaff(id, comment);
      setSuccess(`Return/Exchange request #${id} REJECTED`);
      await fetchReturns();
    } catch (err) {
      setError(err.message || 'Failed to reject request.');
    } finally {
      setUpdatingReturnId(null);
    }
  };

  const handleCompleteReturn = async (id, type) => {
    if (!window.confirm(`Complete this ${type}? ${type === 'RETURN' ? 'Inventory stock will be restored.' : 'Replacement stock will be deducted.'}`)) return;
    try {
      setUpdatingReturnId(id);
      setError('');
      await returnService.completeReturnByStaff(id);
      setSuccess(`${type} request #${id} COMPLETED and inventory safely updated.`);
      await fetchReturns();
    } catch (err) {
      setError(err.message || 'Failed to complete request.');
    } finally {
      setUpdatingReturnId(null);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Staff Operations Dashboard</h1>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            onClick={() => setActiveTab('orders')}
            className={`btn btn-sm ${activeTab === 'orders' ? 'btn-primary' : 'btn-outline'}`}
          >
            📦 Orders
          </button>
          <button
            onClick={() => setActiveTab('returns')}
            className={`btn btn-sm ${activeTab === 'returns' ? 'btn-primary' : 'btn-outline'}`}
          >
            🔄 Returns & Exchanges
          </button>
        </div>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {/* Orders Tab */}
      {activeTab === 'orders' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Orders Processing</h2>
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
              <label style={{ fontSize: '0.875rem', fontWeight: 600 }}>Filter Status:</label>
              <select
                className="form-select"
                value={orderStatusFilter}
                onChange={(e) => setOrderStatusFilter(e.target.value)}
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

          {loadingOrders ? (
            <div style={{ textAlign: 'center', padding: '3rem' }}>Loading orders...</div>
          ) : orders.length === 0 ? (
            <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
              <h3>No orders matching criteria</h3>
            </div>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Order #</th>
                    <th>Customer ID</th>
                    <th>Date</th>
                    <th>Fulfillment</th>
                    <th>Total</th>
                    <th>Current Status</th>
                    <th>Update Status</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order) => (
                    <tr key={order.id}>
                      <td style={{ fontWeight: 700 }}>#{order.id}</td>
                      <td>User #{order.userId}</td>
                      <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                      <td>{order.orderType}</td>
                      <td style={{ fontWeight: 700 }}>₹{Number(order.totalAmount).toFixed(2)}</td>
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
                            disabled={updatingOrderId === order.id}
                            onChange={(e) => handleOrderStatusChange(order.id, e.target.value)}
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
      )}

      {/* Returns & Exchanges Tab */}
      {activeTab === 'returns' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h2 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Returns & Exchanges Queue</h2>
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
              <select
                className="form-select"
                value={returnTypeFilter}
                onChange={(e) => setReturnTypeFilter(e.target.value)}
                style={{ width: 'auto' }}
              >
                <option value="">All Types</option>
                <option value="RETURN">RETURN</option>
                <option value="EXCHANGE">EXCHANGE</option>
              </select>
              <select
                className="form-select"
                value={returnStatusFilter}
                onChange={(e) => setReturnStatusFilter(e.target.value)}
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

          {loadingReturns ? (
            <div style={{ textAlign: 'center', padding: '3rem' }}>Loading returns queue...</div>
          ) : returns.length === 0 ? (
            <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
              <h3>No return or exchange requests found</h3>
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
                    <th>Actions</th>
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
                        <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
                          {(req.status === 'REQUESTED' || req.status === 'PENDING') && (
                            <>
                              <button
                                onClick={() => handleApproveReturn(req.id)}
                                disabled={updatingReturnId === req.id}
                                className="btn btn-primary btn-sm"
                              >
                                Approve
                              </button>
                              <button
                                onClick={() => handleRejectReturn(req.id)}
                                disabled={updatingReturnId === req.id}
                                className="btn btn-danger btn-sm"
                              >
                                Reject
                              </button>
                            </>
                          )}
                          {req.status === 'APPROVED' && (
                            <>
                              <button
                                onClick={() => handleCompleteReturn(req.id, req.type)}
                                disabled={updatingReturnId === req.id}
                                className="btn btn-secondary btn-sm"
                              >
                                Complete & Update Stock
                              </button>
                              <button
                                onClick={() => handleRejectReturn(req.id)}
                                disabled={updatingReturnId === req.id}
                                className="btn btn-danger btn-sm"
                              >
                                Reject
                              </button>
                            </>
                          )}
                          {(req.status === 'COMPLETED' || req.status === 'REJECTED' || req.status === 'CANCELLED') && (
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Finished</span>
                          )}
                        </div>
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