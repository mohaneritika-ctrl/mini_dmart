import { request, setAuthData, clearAuthData } from './api';

export const authService = {
  login: async (credentials) => {
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    setAuthData(data.token, {
      id: data.id,
      name: data.name,
      email: data.email,
      role: data.role,
    });
    return data;
  },

  register: async (userData) => {
    const data = await request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
    setAuthData(data.token, {
      id: data.id,
      name: data.name,
      email: data.email,
      role: data.role,
    });
    return data;
  },

  logout: () => {
    clearAuthData();
  }
};