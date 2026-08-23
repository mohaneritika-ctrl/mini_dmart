import React from 'react';
import { Link } from 'react-router-dom';

export const AdminDashboard = () => {
  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Admin Management Portal</h1>
      </div>

      <div className="grid grid-3">
        <div className="card" style={{ textAlign: 'center', padding: '2rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🗂️</div>
          <h2>Categories</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.25rem' }}>Create, update, and manage store product categories.</p>
          <Link to="/admin/categories" className="btn btn-primary btn-sm">Manage Categories</Link>
        </div>

        <div className="card" style={{ textAlign: 'center', padding: '2rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📦</div>
          <h2>Product Catalog</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.25rem' }}>Add new items, modify prices, descriptions, and visibility.</p>
          <Link to="/admin/products" className="btn btn-primary btn-sm">Manage Products</Link>
        </div>

        <div className="card" style={{ textAlign: 'center', padding: '2rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📊</div>
          <h2>Inventory & Warehouse</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.25rem' }}>Monitor real-time inventory counts and update store stock.</p>
          <Link to="/admin/inventory" className="btn btn-primary btn-sm">Manage Inventory</Link>
        </div>

        <div className="card" style={{ textAlign: 'center', padding: '2rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📑</div>
          <h2>All Store Orders</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.25rem' }}>View global customer orders and oversee fulfillment workflows.</p>
          <Link to="/admin/orders" className="btn btn-primary btn-sm">Manage Orders</Link>
        </div>

        <div className="card" style={{ textAlign: 'center', padding: '2rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🔄</div>
          <h2>Returns & Exchanges</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.25rem' }}>Audit and resolve customer return requests and monitor inventory impact.</p>
          <Link to="/admin/returns" className="btn btn-primary btn-sm">Manage Returns</Link>
        </div>
      </div>
    </div>
  );
};