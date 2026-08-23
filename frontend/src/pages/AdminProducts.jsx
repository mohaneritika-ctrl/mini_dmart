import React, { useEffect, useState } from 'react';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import { Alert } from '../components/Alert';

export const AdminProducts = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    stock: '',
    categoryId: '',
    imageUrl: '',
    active: true,
  });

  const loadData = async () => {
    try {
      setLoading(true);
      const [prodData, catData] = await Promise.all([
        productService.getProducts({ size: 50 }),
        categoryService.getCategories(),
      ]);
      setProducts(prodData.content || []);
      setCategories(catData || []);
    } catch (err) {
      setError(err.message || 'Failed to load products.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleStartCreate = () => {
    setEditingId(null);
    setFormData({
      name: '',
      description: '',
      price: '',
      stock: '10',
      categoryId: categories[0]?.id || '',
      imageUrl: '',
      active: true,
    });
    setShowForm(true);
  };

  const handleStartEdit = (prod) => {
    setEditingId(prod.id);
    setFormData({
      name: prod.name,
      description: prod.description || '',
      price: prod.price,
      stock: prod.stock,
      categoryId: prod.categoryId || categories[0]?.id || '',
      imageUrl: prod.imageUrl || '',
      active: prod.active ?? true,
    });
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const payload = {
        name: formData.name,
        description: formData.description,
        price: Number(formData.price),
        stock: Number(formData.stock),
        categoryId: Number(formData.categoryId),
        imageUrl: formData.imageUrl || null,
        active: formData.active,
      };

      if (editingId) {
        await productService.updateProduct(editingId, payload);
        setSuccess('Product updated successfully!');
      } else {
        await productService.createProduct(payload);
        setSuccess('Product created successfully!');
      }
      setShowForm(false);
      await loadData();
    } catch (err) {
      setError(err.message || 'Failed to save product.');
    }
  };

  const handleToggleStatus = async (prod) => {
    try {
      await productService.updateStatus(prod.id, !prod.active);
      setSuccess(`Product status toggled to ${!prod.active ? 'Active' : 'Inactive'}`);
      await loadData();
    } catch (err) {
      setError(err.message || 'Failed to update status.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    try {
      await productService.deleteProduct(id);
      setSuccess('Product deleted successfully.');
      await loadData();
    } catch (err) {
      setError(err.message || 'Failed to delete product.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Product Catalog Management</h1>
        <button onClick={handleStartCreate} className="btn btn-primary btn-sm">
          + Add New Product
        </button>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {showForm && (
        <div className="card" style={{ marginBottom: '2rem', border: '2px solid var(--primary)' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 800, marginBottom: '1rem' }}>
            {editingId ? 'Edit Product' : 'Create New Product'}
          </h2>
          <form onSubmit={handleSubmit}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="form-group">
                <label className="form-label">Product Name</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Category</label>
                <select
                  required
                  className="form-select"
                  value={formData.categoryId}
                  onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                >
                  <option value="">Select Category</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Price (₹)</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  className="form-control"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Initial Stock</label>
                <input
                  type="number"
                  required
                  className="form-control"
                  value={formData.stock}
                  onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Description</label>
              <textarea
                className="form-control"
                rows={2}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </div>

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <button type="submit" className="btn btn-primary btn-sm">Save Product</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn btn-outline btn-sm">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>Loading products...</div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Product</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((prod) => (
                <tr key={prod.id}>
                  <td>#{prod.id}</td>
                  <td style={{ fontWeight: 700 }}>{prod.name}</td>
                  <td>{prod.categoryName || '—'}</td>
                  <td style={{ fontWeight: 700 }}>₹{Number(prod.price).toFixed(2)}</td>
                  <td>{prod.stock}</td>
                  <td>
                    <span className={`badge ${prod.active ? 'badge-COMPLETED' : 'badge-CANCELLED'}`}>
                      {prod.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => handleStartEdit(prod)} className="btn btn-outline btn-sm">Edit</button>
                      <button onClick={() => handleToggleStatus(prod)} className="btn btn-outline btn-sm">
                        {prod.active ? 'Deactivate' : 'Activate'}
                      </button>
                      <button onClick={() => handleDelete(prod.id)} className="btn btn-danger btn-sm">Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
