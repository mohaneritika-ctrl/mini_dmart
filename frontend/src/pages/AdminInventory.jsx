import React, { useEffect, useState } from 'react';
import { productService } from '../services/productService';
import { Alert } from '../components/Alert';

export const AdminInventory = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [stockInputs, setStockInputs] = useState({});
  const [updatingId, setUpdatingId] = useState(null);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getProducts({ size: 100 });
      const list = data.content || [];
      setProducts(list);
      const initialInputs = {};
      list.forEach((p) => {
        initialInputs[p.id] = p.stock;
      });
      setStockInputs(initialInputs);
    } catch (err) {
      setError(err.message || 'Failed to fetch inventory.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleStockChange = (id, val) => {
    setStockInputs({ ...stockInputs, [id]: val });
  };

  const handleUpdateStock = async (id) => {
    const newStock = stockInputs[id];
    if (newStock === undefined || newStock < 0) return;
    try {
      setUpdatingId(id);
      setError('');
      await productService.updateStock(id, newStock);
      setSuccess(`Stock updated for product #${id} to ${newStock}`);
      await fetchProducts();
    } catch (err) {
      setError(err.message || 'Failed to update stock.');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Inventory & Warehouse Stock Management</h1>
      </div>

      <Alert type="success" message={success} onClose={() => setSuccess('')} />
      <Alert type="error" message={error} onClose={() => setError('')} />

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>Loading inventory...</div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Product</th>
                <th>Category</th>
                <th>Status</th>
                <th>Current Stock</th>
                <th>Update Stock</th>
              </tr>
            </thead>
            <tbody>
              {products.map((prod) => (
                <tr key={prod.id}>
                  <td>#{prod.id}</td>
                  <td style={{ fontWeight: 700 }}>{prod.name}</td>
                  <td>{prod.categoryName || '—'}</td>
                  <td>
                    {prod.stock <= 0 ? (
                      <span className="stock-tag stock-out">Out of Stock</span>
                    ) : prod.stock <= 5 ? (
                      <span className="stock-tag stock-low">Low Stock ({prod.stock})</span>
                    ) : (
                      <span className="stock-tag stock-in">Sufficient ({prod.stock})</span>
                    )}
                  </td>
                  <td style={{ fontWeight: 800 }}>{prod.stock}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                      <input
                        type="number"
                        min="0"
                        className="form-control"
                        style={{ width: '100px', padding: '0.35rem 0.5rem' }}
                        value={stockInputs[prod.id] ?? prod.stock}
                        onChange={(e) => handleStockChange(prod.id, e.target.value)}
                      />
                      <button
                        onClick={() => handleUpdateStock(prod.id)}
                        disabled={updatingId === prod.id}
                        className="btn btn-primary btn-sm"
                      >
                        {updatingId === prod.id ? 'Saving...' : 'Update'}
                      </button>
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
