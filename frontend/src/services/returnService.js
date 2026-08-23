import { request } from './api';

export const returnService = {
  // Customer
  createReturnRequest: (data) => request('/returns', { method: 'POST', body: JSON.stringify(data) }),
  getMyReturns: () => request('/returns'),
  getMyReturnById: (id) => request(`/returns/${id}`),
  cancelMyReturn: (id) => request(`/returns/${id}/cancel`, { method: 'PUT' }),

  // Staff
  getStaffReturns: (status, type) => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (type) params.append('type', type);
    const q = params.toString() ? `?${params.toString()}` : '';
    return request(`/staff/returns${q}`);
  },
  getStaffReturnById: (id) => request(`/staff/returns/${id}`),
  updateReturnStatusByStaff: (id, data) => request(`/staff/returns/${id}/status`, { method: 'PUT', body: JSON.stringify(data) }),
  approveReturnByStaff: (id) => request(`/staff/returns/${id}/approve`, { method: 'PUT' }),
  rejectReturnByStaff: (id, comment) => request(`/staff/returns/${id}/reject`, { method: 'PUT', body: JSON.stringify({ staffComment: comment }) }),
  completeReturnByStaff: (id) => request(`/staff/returns/${id}/complete`, { method: 'PUT' }),

  // Admin
  getAdminReturns: (status, type) => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (type) params.append('type', type);
    const q = params.toString() ? `?${params.toString()}` : '';
    return request(`/admin/returns${q}`);
  },
  getAdminReturnById: (id) => request(`/admin/returns/${id}`),
  updateReturnStatusByAdmin: (id, data) => request(`/admin/returns/${id}/status`, { method: 'PUT', body: JSON.stringify(data) }),
};