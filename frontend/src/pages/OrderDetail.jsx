import React, { useEffect, useState } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import { orderService } from '../services/orderService';
import { returnService } from '../services/returnService';
import { Alert } from '../components/Alert';

export const OrderDetail = () => {
  const { id } = useParams();
  const location = useLocation();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(location.state?.justPlaced ? 'Order placed successfully! Thank you for shopping with Mini D-Mart.' : '');
  const [cancelling, setCancelling] = useState(false);

  // Return / Exchange modal state
  const [showReturnModal, setShowReturnModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const [returnType, setReturnType] = useState('RETURN');
  const [returnQty, setReturnQty] = useState(1);
  const [returnReason, setReturnReason] = useState('Damaged Product');
  const [returnNote, setReturnNote] = useState('');
  const [submittingReturn, setSubmittingReturn] = useState(false);

  const fetchOrder = async () => {
    try {
      setLoading(true);
      const data = await orderService.getMyOrderById(id);
      setOrder(data);
    } catch (err) {
      setError(err.message || 'Failed to load order details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrder();
  }, [id]);

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

  if (loading) return <div style={{ textAlign: 'center', padding: '3rem' }}>Loading order details...</div>;
  if (!order) return <div style={{ textAlign: 'center', padding: '3rem' }}>Order not found.</div>;

  const isCancellable = order.orderStatus === 'PLACED' || order.orderStatus === 'CONFIRMED';
  
  // Return/Exchange eligibility
  const isDelivered = order.orderStatus === 'COMPLETED';
  const orderDate = new Date(order.createdAt);
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
  const isWithin7Days = orderDate >= sevenDaysAgo;
  const isReturnEligible = isDelivered && isWithin7Days;

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div className="page-header">
        <h1 className="page-title">Order #{order.id}</h1>
        <Link to="/orders" style={{ color: 'var(--primary)', fontWeight: 600 }}>&larr; Back to Orders</Link>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      <div className="card" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem', borderBottom: '1px solid var(--border)', paddingBottom: '1rem', marginBottom: '1.25rem' }}>
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Order Placed:</div>
            <div style={{ fontWeight: 600 }}>{new Date(order.createdAt).toLocaleString()}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Status:</div>
            <span className={`badge badge-${order.orderStatus}`}>{order.orderStatus}</span>
          </div>
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Fulfillment:</div>
            <div style={{ fontWeight: 600 }}>{order.orderType}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Total Amount:</div>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--primary-dark)' }}>₹{Number(order.totalAmount).toFixed(2)}</div>
          </div>
        </div>

        {order.orderType === 'PICKUP' && order.pickupTimeSlot && (
          <div style={{ marginBottom: '1rem', fontSize: '0.9rem' }}>
            <strong>Pickup Slot: </strong> {order.pickupTimeSlot}
          </div>
        )}

        {order.orderType === 'DELIVERY' && order.deliveryAddress && (
          <div style={{ marginBottom: '1rem', fontSize: '0.9rem' }}>
            <strong>Delivery Address: </strong> {order.deliveryAddress}
          </div>
        )}

        {isCancellable && (
          <div style={{ marginTop: '1rem', borderTop: '1px solid var(--border)', paddingTop: '1rem', display: 'flex', justifyContent: 'flex-end' }}>
            <button
              onClick={handleCancelOrder}
              disabled={cancelling}
              className="btn btn-danger btn-sm"
            >
              {cancelling ? 'Cancelling...' : 'Cancel Order'}
            </button>
          </div>
        )}

        {!isDelivered && (
          <div style={{ marginTop: '0.75rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            ℹ️ Return/Exchange is available only after an order is marked <strong>COMPLETED</strong> (Delivered).
          </div>
        )}
        {isDelivered && !isWithin7Days && (
          <div style={{ marginTop: '0.75rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            ⚠️ The 7-day return and exchange window for this order has expired.
          </div>
        )}
      </div>

      {/* Return/Exchange Modal */}
      {showReturnModal && selectedItem && (
        <div className="card" style={{ marginBottom: '2rem', border: '2px solid var(--primary)' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1rem' }}>
            Request {returnType} for {selectedItem.productName}
          </h2>
          <form onSubmit={handleSubmitReturn}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
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
                placeholder="Provide any specific details (e.g. seal broken, expired date)"
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

      {/* Items table */}
      <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '1rem' }}>Purchased Items</h2>
      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Unit Price (Snapshot)</th>
              <th>Quantity</th>
              <th>Subtotal</th>
              {isReturnEligible && <th>Return / Exchange</th>}
            </tr>
          </thead>
          <tbody>
            {order.items?.map((item) => (
              <tr key={item.id}>
                <td style={{ fontWeight: 700 }}>{item.productName}</td>
                <td>₹{Number(item.unitPrice).toFixed(2)}</td>
                <td>{item.quantity}</td>
                <td style={{ fontWeight: 800 }}>₹{Number(item.subtotal).toFixed(2)}</td>
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