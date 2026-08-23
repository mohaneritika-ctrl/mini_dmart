import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { Alert } from '../components/Alert';

export const Cart = () => {
  const { cart, updateItem, removeItem, clearCart, loading } = useCart();
  const [error, setError] = useState('');
  const [updatingId, setUpdatingId] = useState(null);
  const navigate = useNavigate();

  const handleQtyChange = async (itemId, newQty) => {
    if (newQty < 1) return;
    try {
      setUpdatingId(itemId);
      setError('');
      await updateItem(itemId, newQty);
    } catch (err) {
      setError(err.message || 'Failed to update item quantity.');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleRemove = async (itemId) => {
    try {
      setError('');
      await removeItem(itemId);
    } catch (err) {
      setError(err.message || 'Failed to remove item.');
    }
  };

  const handleClear = async () => {
    if (!window.confirm('Are you sure you want to clear your shopping cart?')) return;
    try {
      setError('');
      await clearCart();
    } catch (err) {
      setError(err.message || 'Failed to clear cart.');
    }
  };

  const items = cart?.items || [];
  const totalAmount = Number(cart?.totalAmount || 0).toFixed(2);

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
      <div className="page-header">
        <h1 className="page-title">Shopping Cart</h1>
        {items.length > 0 && (
          <button onClick={handleClear} className="btn btn-outline btn-sm" style={{ color: 'var(--danger)' }}>
            Clear Cart
          </button>
        )}
      </div>

      <Alert type="error" message={error} onClose={() => setError('')} />

      {items.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>🛒</div>
          <h2 style={{ marginBottom: '0.5rem' }}>Your Cart is Empty</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>Looks like you haven't added any groceries to your cart yet.</p>
          <Link to="/products" className="btn btn-primary">Start Shopping</Link>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '2rem', alignItems: 'start' }}>
          {/* Cart items list */}
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Price</th>
                  <th>Quantity</th>
                  <th>Subtotal</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <div style={{ fontWeight: 700 }}>{item.productName}</div>
                    </td>
                    <td>₹{Number(item.price).toFixed(2)}</td>
                    <td>
                      <div className="qty-control">
                        <button
                          className="qty-btn"
                          onClick={() => handleQtyChange(item.id, item.quantity - 1)}
                          disabled={item.quantity <= 1 || updatingId === item.id}
                        >
                          -
                        </button>
                        <span className="qty-val">{item.quantity}</span>
                        <button
                          className="qty-btn"
                          onClick={() => handleQtyChange(item.id, item.quantity + 1)}
                          disabled={updatingId === item.id}
                        >
                          +
                        </button>
                      </div>
                    </td>
                    <td style={{ fontWeight: 700 }}>₹{Number(item.subtotal).toFixed(2)}</td>
                    <td>
                      <button
                        onClick={() => handleRemove(item.id)}
                        className="btn btn-outline btn-sm"
                        style={{ color: 'var(--danger)', borderColor: 'transparent' }}
                      >
                        ✕
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Cart Summary */}
          <div className="card">
            <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1.25rem' }}>Order Summary</h2>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem', color: 'var(--text-muted)' }}>
              <span>Total Items:</span>
              <span>{cart.totalItems}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem', fontSize: '1.25rem', fontWeight: 800, borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
              <span>Total Amount:</span>
              <span style={{ color: 'var(--primary-dark)' }}>₹{totalAmount}</span>
            </div>

            <button
              onClick={() => navigate('/checkout')}
              className="btn btn-primary btn-block"
              style={{ marginBottom: '0.75rem' }}
            >
              Proceed to Checkout &rarr;
            </button>

            <Link to="/products" className="btn btn-outline btn-block btn-sm" style={{ textAlign: 'center' }}>
              Continue Shopping
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};