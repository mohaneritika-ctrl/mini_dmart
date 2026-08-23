import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { getProductImageUrl } from '../utils/productImages';

export const ProductCard = ({ product }) => {
  const { isAuthenticated, user } = useAuth();
  const { addToCart } = useCart();
  const navigate = useNavigate();
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  const isOutOfStock = product.stock <= 0;
  const isInactive = !product.active;

  const handleAddToCart = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (user?.role !== 'CUSTOMER') return;

    try {
      setAdding(true);
      await addToCart(product.id, 1);
      setAdded(true);
      setTimeout(() => setAdded(false), 1500);
    } catch (err) {
      alert(err.message || 'Failed to add to cart');
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="card product-card">
      <Link to={`/products/${product.id}`}>
        <div className="product-img-box" style={{ background: '#ffffff', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img
            src={getProductImageUrl(product)}
            alt={product.name}
            className="product-img"
            style={{ width: '100%', height: '100%', objectFit: 'contain', padding: '0.5rem' }}
            loading="lazy"
          />
        </div>
        <h3 className="product-title">{product.name}</h3>
        {product.categoryName && (
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
            🏷️ {product.categoryName}
          </div>
        )}
        <p className="product-desc">{product.description || 'Fresh and quality grocery item.'}</p>
      </Link>

      <div className="product-price-row">
        <div>
          <span className="product-price">₹{Number(product.price).toFixed(2)}</span>
          <div style={{ marginTop: '0.25rem' }}>
            {isInactive ? (
              <span className="stock-tag stock-out">Unavailable</span>
            ) : isOutOfStock ? (
              <span className="stock-tag stock-out">Out of Stock</span>
            ) : product.stock <= 5 ? (
              <span className="stock-tag stock-low">Only {product.stock} left</span>
            ) : (
              <span className="stock-tag stock-in">In Stock ({product.stock})</span>
            )}
          </div>
        </div>

        {(!user || user.role === 'CUSTOMER') && (
          <button
            onClick={handleAddToCart}
            disabled={isOutOfStock || isInactive || adding}
            className={`btn btn-sm ${added ? 'btn-secondary' : 'btn-primary'}`}
          >
            {added ? '✓ Added' : adding ? 'Adding...' : 'Add to Cart'}
          </button>
        )}
      </div>
    </div>
  );
};
