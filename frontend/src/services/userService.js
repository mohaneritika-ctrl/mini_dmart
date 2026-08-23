import { request } from './api';

export const userService = {
  getUsers: async (role = '') => {
    const url = role ? `/admin/users?role=${role}` : '/admin/users';
    return request(url);
  },

  getAuditLogs: async () => {
    return request('/admin/audit-logs');
  },

  getUserStats: async () => {
    return request('/admin/users/stats');
  },

  getProfile: async () => {
    return request('/profile');
  },

  updateProfile: async (data) => {
    return request('/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },
};
