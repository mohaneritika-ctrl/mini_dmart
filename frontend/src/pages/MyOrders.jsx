import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderService } from '../services/orderService';

export const MyOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    orderService.getMyOrders()
      .then(setOrders)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">My Order History & Tracking</h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Track live package deliveries and view previous purchases
          </p>
        </div>
        <Link to="/products" className="btn btn-primary btn-sm">
          🛒 Shop More Groceries
        </Link>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem 2rem', background: '#ffffff', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
          <div style={{ fontSize: '2.5rem', marginBottom: '0.75rem' }}>⏳</div>
          <div style={{ fontSize: '1.1rem', fontWeight: 800 }}>Loading your orders...</div>
        </div>
      ) : orders.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <div style={{ fontSize: '3.5rem', marginBottom: '1rem' }}>📦</div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800, marginBottom: '0.5rem' }}>No Orders Placed Yet</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', maxWidth: '450px', margin: '0 auto 1.5rem' }}>
            You haven't placed any orders with Mini D-Mart yet. Start adding fresh groceries to your cart!
          </p>
          <Link to="/products" className="btn btn-primary">Start Shopping Now</Link>
        </div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Order #</th>
                <th>Order Date</th>
                <th>Fulfillment</th>
                <th>Items</th>
                <th>Total Paid</th>
                <th>Live Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => {
                const isActive = order.orderStatus !== 'COMPLETED' && order.orderStatus !== 'CANCELLED';
                return (
                  <tr key={order.id}>
                    <td style={{ fontWeight: 800, color: 'var(--primary-dark)' }}>#{order.id}</td>
                    <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td>
                      <span style={{ fontWeight: 700 }}>
                        {order.orderType === 'DELIVERY' ? '🚚 Home Delivery' : '🏬 Store Pickup'}
                      </span>
                    </td>
                    <td>{order.items?.length || 0} product(s)</td>
                    <td style={{ fontWeight: 900, color: '#065f46' }}>₹{Number(order.totalAmount).toFixed(2)}</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                        {isActive && (
                          <span style={{ width: '8px', height: '8px', background: '#10b981', borderRadius: '50%', display: 'inline-block' }}></span>
                        )}
                        <span className={`badge badge-${order.orderStatus}`}>
                          {order.orderStatus.replace(/_/g, ' ')}
                        </span>
                      </div>
                    </td>
                    <td>
                      <Link
                        to={`/orders/${order.id}`}
                        className={`btn btn-sm ${isActive ? 'btn-primary' : 'btn-outline'}`}
                        style={{ fontSize: '0.8rem' }}
                      >
                        {isActive ? '📍 Live Track' : 'View Order'}
                      </Link>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
