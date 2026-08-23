import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
    setMobileMenuOpen(false);
  };

  const isActive = (path) => location.pathname === path ? 'active' : '';

  const handleLinkClick = () => {
    setMobileMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand" onClick={handleLinkClick}>
          <span className="brand-logo-icon">🛒</span>
          <span>Mini <span style={{ color: 'var(--secondary)' }}>D-Mart</span></span>
        </Link>

        <button
          className="mobile-menu-btn"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle navigation menu"
        >
          {mobileMenuOpen ? '✕' : '☰'}
        </button>

        <ul className={`navbar-links ${mobileMenuOpen ? 'open' : ''}`}>
          {(!user || user.role === 'CUSTOMER') && (
            <>
              <li>
                <Link to="/" className={`nav-link ${isActive('/')}`} onClick={handleLinkClick}>
                  🏠 Home
                </Link>
              </li>
              <li>
                <Link to="/products" className={`nav-link ${isActive('/products')}`} onClick={handleLinkClick}>
                  🛍️ Products
                </Link>
              </li>
              <li>
                <Link to="/categories" className={`nav-link ${isActive('/categories')}`} onClick={handleLinkClick}>
                  🏷️ Categories
                </Link>
              </li>
              {isAuthenticated && (
                <>
                  <li>
                    <Link to="/cart" className={`nav-link ${isActive('/cart')}`} onClick={handleLinkClick}>
                      🛒 Cart {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
                    </Link>
                  </li>
                  <li>
                    <Link to="/orders" className={`nav-link ${isActive('/orders')}`} onClick={handleLinkClick}>
                      📦 My Orders
                    </Link>
                  </li>
                  <li>
                    <Link to="/returns" className={`nav-link ${isActive('/returns')}`} onClick={handleLinkClick}>
                      🔄 My Returns
                    </Link>
                  </li>
                </>
              )}
            </>
          )}

          {user?.role === 'STAFF' && (
            <>
              <li>
                <Link to="/staff" className={`nav-link ${isActive('/staff')}`} onClick={handleLinkClick}>
                  📋 Staff Dashboard
                </Link>
              </li>
            </>
          )}

          {user?.role === 'ADMIN' && (
            <>
              <li>
                <Link to="/admin" className={`nav-link ${isActive('/admin')}`} onClick={handleLinkClick}>
                  📊 Dashboard
                </Link>
              </li>
              <li>
                <Link to="/admin/categories" className={`nav-link ${isActive('/admin/categories')}`} onClick={handleLinkClick}>
                  🏷️ Categories
                </Link>
              </li>
              <li>
                <Link to="/admin/products" className={`nav-link ${isActive('/admin/products')}`} onClick={handleLinkClick}>
                  🛍️ Products
                </Link>
              </li>
              <li>
                <Link to="/admin/inventory" className={`nav-link ${isActive('/admin/inventory')}`} onClick={handleLinkClick}>
                  📦 Inventory
                </Link>
              </li>
              <li>
                <Link to="/admin/orders" className={`nav-link ${isActive('/admin/orders')}`} onClick={handleLinkClick}>
                  📋 Orders
                </Link>
              </li>
              <li>
                <Link to="/admin/returns" className={`nav-link ${isActive('/admin/returns')}`} onClick={handleLinkClick}>
                  🔄 Returns
                </Link>
              </li>
            </>
          )}

          {isAuthenticated ? (
            <li style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginLeft: 'auto', flexWrap: 'wrap' }}>
              <span className="user-badge">{user.role}</span>
              <span style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--text-main)' }}>{user.name}</span>
              <button onClick={handleLogout} className="btn btn-outline btn-sm" style={{ color: 'var(--danger)', borderColor: 'rgba(239, 68, 68, 0.3)' }}>
                Logout
              </button>
            </li>
          ) : (
            <li style={{ display: 'flex', gap: '0.6rem', marginLeft: 'auto' }}>
              <Link to="/login" className="btn btn-outline btn-sm" onClick={handleLinkClick}>Login</Link>
              <Link to="/register" className="btn btn-primary btn-sm" onClick={handleLinkClick}>Register</Link>
            </li>
          )}
        </ul>
      </div>
    </nav>
  );
};