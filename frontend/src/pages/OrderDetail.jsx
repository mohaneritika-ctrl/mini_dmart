import React, { useEffect, useState, useRef } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import { orderService } from '../services/orderService';
import { returnService } from '../services/returnService';
import { Alert } from '../components/Alert';

const DELIVERY_STEPS = [
  { status: 'PLACED', label: 'Order Placed', icon: '📝', desc: 'Order received & verified' },
  { status: 'CONFIRMED', label: 'Confirmed', icon: '👨‍🍳', desc: 'Store preparing groceries' },
  { status: 'PACKED', label: 'Packed & Ready', icon: '📦', desc: 'Sealed with care' },
  { status: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: '🛵', desc: 'Partner on the way' },
  { status: 'COMPLETED', label: 'Delivered', icon: '🎉', desc: 'Handed over at doorstep' }
];

const PICKUP_STEPS = [
  { status: 'PLACED', label: 'Order Placed', icon: '📝', desc: 'Order received & verified' },
  { status: 'CONFIRMED', label: 'Confirmed', icon: '🏪', desc: 'Store preparing items' },
  { status: 'READY_FOR_PICKUP', label: 'Ready for Pickup', icon: '📦', desc: 'Waiting at store counter' },
  { status: 'COMPLETED', label: 'Picked Up', icon: '🎉', desc: 'Order fulfilled' }
];

export const OrderDetail = () => {
  const { id } = useParams();
  const location = useLocation();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(location.state?.justPlaced ? 'Order placed successfully! Thank you for shopping with Mini D-Mart.' : '');
  const [cancelling, setCancelling] = useState(false);
  const [lastRefreshed, setLastRefreshed] = useState(new Date());

  // Return / Exchange modal state
  const [showReturnModal, setShowReturnModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const [returnType, setReturnType] = useState('RETURN');
  const [returnQty, setReturnQty] = useState(1);
  const [returnReason, setReturnReason] = useState('Damaged Product');
  const [returnNote, setReturnNote] = useState('');
  const [submittingReturn, setSubmittingReturn] = useState(false);

  const fetchOrder = async (isBackground = false) => {
    try {
      if (!isBackground) setLoading(true);
      const data = await orderService.getMyOrderById(id);
      setOrder(data);
      setLastRefreshed(new Date());
    } catch (err) {
      if (!isBackground) setError(err.message || 'Failed to load order details.');
    } finally {
      if (!isBackground) setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrder();
  }, [id]);

  // Live Auto-Refresh Polling every 6 seconds while order is active
  useEffect(() => {
    if (!order) return;
    if (order.orderStatus === 'COMPLETED' || order.orderStatus === 'CANCELLED') return;

    const interval = setInterval(() => {
      fetchOrder(true);
    }, 6000);

    return () => clearInterval(interval);
  }, [order?.orderStatus, id]);

  const handleCancelOrder = async () => {
    if (!window.confirm('Are you sure you want to cancel this order? Stock will be restored to store inventory.')) {
      return;
    }

    try {
      setCancelling(true);
      setError('');
      const updated = await orderService.cancelMyOrder(id);
      setOrder(updated);
      setSuccess('Order has been successfully cancelled and stock restored.');
    } catch (err) {
      setError(err.message || 'Failed to cancel order.');
    } finally {
      setCancelling(false);
    }
  };

  const openReturnModal = (item, type = 'RETURN') => {
    setSelectedItem(item);
    setReturnType(type);
    setReturnQty(1);
    setReturnReason('Damaged Product');
    setReturnNote('');
    setShowReturnModal(true);
  };

  const handleSubmitReturn = async (e) => {
    e.preventDefault();
    setError('');
    try {
      setSubmittingReturn(true);
      const payload = {
        orderId: Number(order.id),
        orderItemId: Number(selectedItem.id),
        type: returnType,
        quantity: Number(returnQty),
        reason: returnReason,
        note: returnNote || null,
      };

      await returnService.createReturnRequest(payload);
      setSuccess(`Successfully submitted ${returnType} request for ${selectedItem.productName}!`);
      setShowReturnModal(false);
    } catch (err) {
      setError(err.message || 'Failed to submit return/exchange request.');
    } finally {
      setSubmittingReturn(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '4rem' }}>Loading order details...</div>;
  if (!order) return <div style={{ textAlign: 'center', padding: '4rem' }}>Order not found.</div>;

  const isCancellable = order.orderStatus === 'PLACED' || order.orderStatus === 'CONFIRMED';
  const isDelivered = order.orderStatus === 'COMPLETED';
  const isCancelled = order.orderStatus === 'CANCELLED';
  const isDelivery = order.orderType === 'DELIVERY';

  const steps = isDelivery ? DELIVERY_STEPS : PICKUP_STEPS;

  // Compute active step index
  const getStepIndex = (status) => {
    switch (status) {
      case 'PLACED': return 0;
      case 'CONFIRMED':
      case 'PREPARING': return 1;
      case 'PACKED':
      case 'READY_FOR_PICKUP': return isDelivery ? 2 : 2;
      case 'OUT_FOR_DELIVERY': return 3;
      case 'COMPLETED': return steps.length - 1;
      default: return 0;
    }
  };

  const currentStepIdx = isCancelled ? -1 : getStepIndex(order.orderStatus);
  const progressPercent = isCancelled ? 0 : Math.min(100, Math.round((currentStepIdx / (steps.length - 1)) * 100));

  // Return/Exchange eligibility (7 days from delivery)
  const orderDate = new Date(order.createdAt);
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
  const isWithin7Days = orderDate >= sevenDaysAgo;
  const isReturnEligible = isDelivered && isWithin7Days;

  return (
    <div style={{ maxWidth: '950px', margin: '0 auto' }}>
      <div className="page-header">
        <div>
          <h1 className="page-title">Order #{order.id}</h1>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Placed on {new Date(order.createdAt).toLocaleString()}
          </p>
        </div>
        <Link to="/orders" className="btn btn-outline btn-sm">
          &larr; Back to My Orders
        </Link>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {/* LIVE ORDER TRACKER CARD */}
      <div className="live-tracker-card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <span className="live-badge-pulse">
              <span className="pulse-circle"></span>
              {isCancelled ? 'ORDER CANCELLED' : isDelivered ? 'ORDER DELIVERED' : 'LIVE TRACKING ACTIVE'}
            </span>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Updated {lastRefreshed.toLocaleTimeString()}
            </span>
          </div>

          <span className={`badge badge-${order.orderStatus}`}>
            {order.orderStatus.replace(/_/g, ' ')}
          </span>
        </div>

        {/* Visual Stepper */}
        {!isCancelled ? (
          <>
            <div className="tracking-steps-container">
              <div
                className="tracking-progress-bar"
                style={{ width: `${progressPercent}%` }}
              ></div>

              {steps.map((step, idx) => {
                const isCompleted = idx < currentStepIdx || isDelivered;
                const isActive = idx === currentStepIdx && !isDelivered;

                return (
                  <div
                    key={step.status}
                    className={`tracking-step-item ${isCompleted ? 'completed' : ''} ${isActive ? 'active' : ''}`}
                  >
                    <div className="tracking-step-icon">
                      {isCompleted ? '✓' : step.icon}
                    </div>
                    <div className="tracking-step-title">{step.label}</div>
                    <div className="tracking-step-desc">{step.desc}</div>
                  </div>
                );
              })}
            </div>

            {/* Real-time ETA & Status Banner */}
            <div className="delivery-eta-box">
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <div style={{ fontSize: '2rem' }}>
                  {isDelivered ? '🎉' : isDelivery ? '🛵' : '🏬'}
                </div>
                <div>
                  <h4 style={{ fontWeight: 800, color: 'var(--text-main)', fontSize: '1rem', marginBottom: '0.2rem' }}>
                    {isDelivered
                      ? 'Package Delivered Successfully'
                      : isDelivery
                        ? order.orderStatus === 'OUT_FOR_DELIVERY'
                          ? 'Delivery Partner is on the way!'
                          : 'Preparing your grocery delivery'
                        : order.orderStatus === 'READY_FOR_PICKUP'
                          ? 'Your package is ready at the store counter!'
                          : 'Store team is packing your groceries'}
                  </h4>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    {isDelivered
                      ? 'Thank you for choosing Mini D-Mart! Rate your experience.'
                      : isDelivery
                        ? `Estimated Arrival: 15-25 Mins to ${order.deliveryAddress || 'your address'}`
                        : `Pickup Window: ${order.pickupTimeSlot || 'Available Today'}`}
                  </p>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => fetchOrder(false)}
                  className="btn btn-outline btn-sm"
                  style={{ fontSize: '0.8rem' }}
                >
                  🔄 Refresh Status
                </button>
              </div>
            </div>
          </>
        ) : (
          <div style={{ padding: '2rem 1rem', textAlign: 'center', background: '#fee2e2', borderRadius: 'var(--radius)', marginTop: '1.25rem', color: '#991b1b' }}>
            <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>🛑</div>
            <h3 style={{ fontWeight: 800 }}>Order Cancelled</h3>
            <p style={{ fontSize: '0.85rem' }}>This order was cancelled. Any reserved stock has been returned to inventory.</p>
          </div>
        )}
      </div>

      {/* ORDER SUMMARY & DETAILS */}
      <div className="card" style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1.25rem' }}>Order Details</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.25rem', borderBottom: '1px solid var(--border)', paddingBottom: '1.25rem', marginBottom: '1.25rem' }}>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>Fulfillment Method</div>
            <div style={{ fontWeight: 800, color: 'var(--text-main)', marginTop: '0.2rem' }}>
              {isDelivery ? '🚚 Home Delivery' : '🏬 Store Pickup'}
            </div>
          </div>

          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>
              {isDelivery ? 'Delivery Location' : 'Pickup Slot'}
            </div>
            <div style={{ fontWeight: 600, color: 'var(--text-main)', marginTop: '0.2rem', fontSize: '0.9rem' }}>
              {isDelivery ? (order.deliveryAddress || 'Standard Address') : (order.pickupTimeSlot || 'Store Counter')}
            </div>
          </div>

          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase' }}>Total Amount Paid</div>
            <div style={{ fontSize: '1.35rem', fontWeight: 900, color: 'var(--primary-dark)', marginTop: '0.2rem' }}>
              ₹{Number(order.totalAmount).toFixed(2)}
            </div>
          </div>
        </div>

        {isCancellable && (
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              Need to make changes? You can cancel your order while it is still being confirmed.
            </span>
            <button
              onClick={handleCancelOrder}
              disabled={cancelling}
              className="btn btn-danger btn-sm"
            >
              {cancelling ? 'Cancelling...' : 'Cancel Order'}
            </button>
          </div>
        )}
      </div>

      {/* Return/Exchange Modal */}
      {showReturnModal && selectedItem && (
        <div className="card" style={{ marginBottom: '2rem', border: '2px solid var(--primary)', boxShadow: 'var(--shadow-md)' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1rem' }}>
            Request {returnType} for {selectedItem.productName}
          </h2>
          <form onSubmit={handleSubmitReturn}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
              <div className="form-group">
                <label className="form-label">Request Type</label>
                <select
                  className="form-select"
                  value={returnType}
                  onChange={(e) => setReturnType(e.target.value)}
                >
                  <option value="RETURN">Return (Refund / Restock)</option>
                  <option value="EXCHANGE">Exchange (Replacement Item)</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Quantity to Return (Max {selectedItem.quantity})</label>
                <input
                  type="number"
                  min="1"
                  max={selectedItem.quantity}
                  required
                  className="form-control"
                  value={returnQty}
                  onChange={(e) => setReturnQty(e.target.value)}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Reason for {returnType}</label>
              <select
                className="form-select"
                value={returnReason}
                onChange={(e) => setReturnReason(e.target.value)}
              >
                <option value="Damaged Product">Damaged Product</option>
                <option value="Wrong Product">Wrong Product</option>
                <option value="Expired Product">Expired Product</option>
                <option value="Product Not as Expected">Product Not as Expected</option>
                <option value="Other">Other</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Additional Comments / Details</label>
              <textarea
                className="form-control"
                rows={2}
                placeholder="Provide specific details (e.g., package seal open, wrong flavor)"
                value={returnNote}
                onChange={(e) => setReturnNote(e.target.value)}
              />
            </div>

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <button type="submit" disabled={submittingReturn} className="btn btn-primary btn-sm">
                {submittingReturn ? 'Submitting...' : `Submit ${returnType} Request`}
              </button>
              <button type="button" onClick={() => setShowReturnModal(false)} className="btn btn-outline btn-sm">
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Purchased Items Table */}
      <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1rem' }}>Items in this Order</h2>
      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Unit Price</th>
              <th>Quantity</th>
              <th>Subtotal</th>
              {isReturnEligible && <th>Return / Exchange</th>}
            </tr>
          </thead>
          <tbody>
            {order.items?.map((item) => (
              <tr key={item.id}>
                <td style={{ fontWeight: 800, color: 'var(--text-main)' }}>{item.productName}</td>
                <td>₹{Number(item.unitPrice).toFixed(2)}</td>
                <td><span style={{ background: '#f1f5f9', padding: '0.2rem 0.5rem', borderRadius: '4px', fontWeight: 700 }}>x{item.quantity}</span></td>
                <td style={{ fontWeight: 900, color: '#065f46' }}>₹{Number(item.subtotal).toFixed(2)}</td>
                {isReturnEligible && (
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button
                        onClick={() => openReturnModal(item, 'RETURN')}
                        className="btn btn-outline btn-sm"
                      >
                        Return
                      </button>
                      <button
                        onClick={() => openReturnModal(item, 'EXCHANGE')}
                        className="btn btn-secondary btn-sm"
                      >
                        Exchange
                      </button>
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};