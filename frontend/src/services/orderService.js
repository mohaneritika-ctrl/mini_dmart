import { request } from './api';

export const orderService = {
  // Customer
  checkout: (dto = {}) => request('/orders/checkout', { method: 'POST', body: JSON.stringify(dto) }),
  getMyOrders: () => request('/orders'),
  getMyOrderById: (orderId) => request(`/orders/${orderId}`),
  cancelMyOrder: (orderId) => request(`/orders/${orderId}/cancel`, { method: 'PUT' }),

  // Staff
  getStaffOrders: (status) => {
    const q = status ? `?status=${status}` : '';
    return request(`/staff/orders${q}`);
  },
  getStaffOrderById: (orderId) => request(`/staff/orders/${orderId}`),
  updateOrderStatusByStaff: (orderId, status) => request(`/staff/orders/${orderId}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),

  // Admin
  getAdminOrders: (status) => {
    const q = status ? `?status=${status}` : '';
    return request(`/admin/orders${q}`);
  },
  getAdminOrderById: (orderId) => request(`/admin/orders/${orderId}`),
  updateOrderStatusByAdmin: (orderId, status) => request(`/admin/orders/${orderId}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
};