import { request } from './api';

export const productService = {
  getProducts: (params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size !== undefined) query.append('size', params.size);
    if (params.keyword) query.append('keyword', params.keyword);
    if (params.categoryId) query.append('categoryId', params.categoryId);
    const queryString = query.toString() ? `?${query.toString()}` : '';
    return request(`/products${queryString}`);
  },
  getProductById: (id) => request(`/products/${id}`),
  createProduct: (data) => request('/products', { method: 'POST', body: JSON.stringify(data) }),
  updateProduct: (id, data) => request(`/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  updateStock: (id, stock) => request(`/products/${id}/stock`, { method: 'PATCH', body: JSON.stringify({ stock: Number(stock) }) }),
  updateStatus: (id, active) => request(`/products/${id}/status?active=${active}`, { method: 'PATCH' }),
  deleteProduct: (id) => request(`/products/${id}`, { method: 'DELETE' }),
};