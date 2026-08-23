import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { orderService } from '../services/orderService';
import { Alert } from '../components/Alert';

export const Checkout = () => {
  const { cart, fetchCart } = useCart();
  const navigate = useNavigate();

  const [orderType, setOrderType] = useState('PICKUP');
  const [pickupTimeSlot, setPickupTimeSlot] = useState('10:00 AM - 12:00 PM');
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const items = cart?.items || [];
  const totalAmount = Number(cart?.totalAmount || 0).toFixed(2);

  if (items.length === 0) {
    return (
      <div className="card" style={{ maxWidth: '600px', margin: '3rem auto', textAlign: 'center', padding: '3rem' }}>
        <h2>Your Cart is Empty</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>Add items to your cart before proceeding to checkout.</p>
        <Link to="/products" className="btn btn-primary">Browse Products</Link>
      </div>
    );
  }

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const payload = {
        orderType,
        pickupTimeSlot: orderType === 'PICKUP' ? pickupTimeSlot : null,
        deliveryAddress: orderType === 'DELIVERY' ? deliveryAddress : null,
      };

      const order = await orderService.checkout(payload);
      await fetchCart();
      navigate(`/orders/${order.id}`, { state: { justPlaced: true } });
    } catch (err) {
      setError(err.message || 'Checkout failed. Please review your order.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div className="page-header">
        <h1 className="page-title">Order Checkout</h1>
        <Link to="/cart" style={{ color: 'var(--primary)', fontWeight: 600 }}>&larr; Back to Cart</Link>
      </div>

      <Alert type="error" message={error} onClose={() => setError('')} />

      <div className="checkout-layout">
        <div className="card">
          <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1.25rem' }}>1. Choose Fulfillment Method</h2>
          
          <form onSubmit={handlePlaceOrder}>
            <div className="form-group">
              <label className="form-label">Order Type</label>
              <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="orderType"
                    value="PICKUP"
                    checked={orderType === 'PICKUP'}
                    onChange={() => setOrderType('PICKUP')}
                  />
                  <span>🏬 Store Pickup</span>
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="orderType"
                    value="DELIVERY"
                    checked={orderType === 'DELIVERY'}
                    onChange={() => setOrderType('DELIVERY')}
                  />
                  <span>🚚 Home Delivery</span>
                </label>
              </div>
            </div>

            {orderType === 'PICKUP' ? (
              <div className="form-group">
                <label className="form-label">Pickup Time Slot</label>
                <select
                  className="form-select"
                  value={pickupTimeSlot}
                  onChange={(e) => setPickupTimeSlot(e.target.value)}
                >
                  <option value="09:00 AM - 11:00 AM">09:00 AM - 11:00 AM</option>
                  <option value="11:00 AM - 01:00 PM">11:00 AM - 01:00 PM</option>
                  <option value="02:00 PM - 04:00 PM">02:00 PM - 04:00 PM</option>
                  <option value="05:00 PM - 07:00 PM">05:00 PM - 07:00 PM</option>
                  <option value="07:00 PM - 09:00 PM">07:00 PM - 09:00 PM</option>
                </select>
                <small style={{ color: 'var(--text-muted)', marginTop: '0.25rem', display: 'block' }}>
                  Orders will be packed and ready for pickup at our main store desk.
                </small>
              </div>
            ) : (
              <div className="form-group">
                <label className="form-label">Delivery Address</label>
                <textarea
                  className="form-control"
                  rows={3}
                  required
                  placeholder="Enter complete delivery street address, landmark and pincode"
                  value={deliveryAddress}
                  onChange={(e) => setDeliveryAddress(e.target.value)}
                />
              </div>
            )}

            <button type="submit" disabled={loading} className="btn btn-primary btn-block" style={{ marginTop: '1.5rem' }}>
              {loading ? 'Processing Order...' : `Place Order (₹${totalAmount})`}
            </button>
          </form>
        </div>

        <div className="card">
          <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1.25rem' }}>2. Order Review</h2>
          <div style={{ marginBottom: '1.25rem' }}>
            {items.map((item) => (
              <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem', fontSize: '0.9rem' }}>
                <span>{item.quantity}x {item.productName}</span>
                <span style={{ fontWeight: 600 }}>₹{Number(item.subtotal).toFixed(2)}</span>
              </div>
            ))}
          </div>

          <div style={{ borderTop: '1px solid var(--border)', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', fontSize: '1.2rem', fontWeight: 800 }}>
            <span>Final Amount:</span>
            <span style={{ color: 'var(--primary-dark)' }}>₹{totalAmount}</span>
          </div>
        </div>
      </div>
    </div>
  );
};
