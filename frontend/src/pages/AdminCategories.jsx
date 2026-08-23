import React, { useEffect, useState } from 'react';
import { categoryService } from '../services/categoryService';
import { Alert } from '../components/Alert';

export const AdminCategories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ name: '', description: '', active: true });

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const data = await categoryService.getCategories();
      setCategories(data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch categories.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const handleStartCreate = () => {
    setEditingId(null);
    setFormData({ name: '', description: '', active: true });
    setShowForm(true);
  };

  const handleStartEdit = (cat) => {
    setEditingId(cat.id);
    setFormData({ name: cat.name, description: cat.description || '', active: cat.active ?? true });
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editingId) {
        await categoryService.updateCategory(editingId, formData);
        setSuccess('Category updated successfully!');
      } else {
        await categoryService.createCategory(formData);
        setSuccess('Category created successfully!');
      }
      setShowForm(false);
      await fetchCategories();
    } catch (err) {
      setError(err.message || 'Failed to save category.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this category?')) return;
    setError('');
    try {
      await categoryService.deleteCategory(id);
      setSuccess('Category deleted successfully.');
      await fetchCategories();
    } catch (err) {
      setError(err.message || 'Failed to delete category.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Category Management</h1>
        <button onClick={handleStartCreate} className="btn btn-primary btn-sm">
          + Add New Category
        </button>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {showForm && (
        <div className="card" style={{ marginBottom: '2rem', border: '2px solid var(--primary)' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 800, marginBottom: '1rem' }}>
            {editingId ? 'Edit Category' : 'Create New Category'}
          </h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Category Name</label>
              <input
                type="text"
                required
                className="form-control"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
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
              <button type="submit" className="btn btn-primary btn-sm">Save</button>
              <button type="button" onClick={() => setShowForm(false)} className="btn btn-outline btn-sm">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>Loading categories...</div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Category Name</th>
                <th>Description</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((cat) => (
                <tr key={cat.id}>
                  <td>#{cat.id}</td>
                  <td style={{ fontWeight: 700 }}>{cat.name}</td>
                  <td style={{ color: 'var(--text-muted)' }}>{cat.description || '—'}</td>
                  <td>
                    <span className={`badge ${cat.active ? 'badge-COMPLETED' : 'badge-CANCELLED'}`}>
                      {cat.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => handleStartEdit(cat)} className="btn btn-outline btn-sm">Edit</button>
                      <button onClick={() => handleDelete(cat.id)} className="btn btn-danger btn-sm">Delete</button>
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
