import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path ? 'active' : '';

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          🛒 Mini <span>D-Mart</span>
        </Link>

        <ul className="navbar-links">
          {(!user || user.role === 'CUSTOMER') && (
            <>
              <li>
                <Link to="/" className={`nav-link ${isActive('/')}`}>Home</Link>
              </li>
              <li>
                <Link to="/products" className={`nav-link ${isActive('/products')}`}>Products</Link>
              </li>
              <li>
                <Link to="/categories" className={`nav-link ${isActive('/categories')}`}>Categories</Link>
              </li>
              {isAuthenticated && (
                <>
                  <li>
                    <Link to="/cart" className={`nav-link ${isActive('/cart')}`}>
                      Cart {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
                    </Link>
                  </li>
                  <li>
                    <Link to="/orders" className={`nav-link ${isActive('/orders')}`}>My Orders</Link>
                  </li>
                  <li>
                    <Link to="/returns" className={`nav-link ${isActive('/returns')}`}>My Returns</Link>
                  </li>
                </>
              )}
            </>
          )}

          {user?.role === 'STAFF' && (
            <>
              <li>
                <Link to="/staff" className={`nav-link ${isActive('/staff')}`}>Staff Dashboard</Link>
              </li>
            </>
          )}

          {user?.role === 'ADMIN' && (
            <>
              <li>
                <Link to="/admin" className={`nav-link ${isActive('/admin')}`}>Dashboard</Link>
              </li>
              <li>
                <Link to="/admin/categories" className={`nav-link ${isActive('/admin/categories')}`}>Categories</Link>
              </li>
              <li>
                <Link to="/admin/products" className={`nav-link ${isActive('/admin/products')}`}>Products</Link>
              </li>
              <li>
                <Link to="/admin/inventory" className={`nav-link ${isActive('/admin/inventory')}`}>Inventory</Link>
              </li>
              <li>
                <Link to="/admin/orders" className={`nav-link ${isActive('/admin/orders')}`}>Orders</Link>
              </li>
              <li>
                <Link to="/admin/returns" className={`nav-link ${isActive('/admin/returns')}`}>Returns & Exchanges</Link>
              </li>
            </>
          )}

          {isAuthenticated ? (
            <li style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginLeft: '0.5rem' }}>
              <span className="user-badge">{user.role}</span>
              <span style={{ fontSize: '0.875rem', fontWeight: 600 }}>{user.name}</span>
              <button onClick={handleLogout} className="btn btn-outline btn-sm">Logout</button>
            </li>
          ) : (
            <li style={{ display: 'flex', gap: '0.5rem' }}>
              <Link to="/login" className="btn btn-outline btn-sm">Login</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Register</Link>
            </li>
          )}
        </ul>
      </div>
    </nav>
  );
};