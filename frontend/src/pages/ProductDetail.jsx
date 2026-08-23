import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { productService } from '../services/productService';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { Alert } from '../components/Alert';
import { getProductImageUrl } from '../utils/productImages';

export const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { addToCart } = useCart();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        setLoading(true);
        const data = await productService.getProductById(id);
        setProduct(data);
      } catch (err) {
        setError(err.message || 'Product not found.');
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (user?.role !== 'CUSTOMER') return;

    try {
      setAdding(true);
      setError('');
      await addToCart(product.id, quantity);
      setSuccess(`Added ${quantity} item(s) to cart!`);
      setTimeout(() => setSuccess(''), 2500);
    } catch (err) {
      setError(err.message || 'Failed to add item to cart.');
    } finally {
      setAdding(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '3rem' }}>Loading product details...</div>;
  if (!product) return <div style={{ textAlign: 'center', padding: '3rem' }}>Product not found.</div>;

  const isOutOfStock = product.stock <= 0;
  const isInactive = !product.active;

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ marginBottom: '1rem' }}>
        <Link to="/products" style={{ color: 'var(--primary)', fontWeight: 600 }}>&larr; Back to Products</Link>
      </div>

      <Alert type="error" message={error} onClose={() => setError('')} />
      <Alert type="success" message={success} onClose={() => setSuccess('')} />

      <div className="card" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        <div className="product-img-box" style={{ height: '320px', background: '#ffffff', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img
            src={getProductImageUrl(product)}
            alt={product.name}
            className="product-img"
            style={{ width: '100%', height: '100%', objectFit: 'contain', padding: '1rem' }}
          />
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <span className="badge" style={{ backgroundColor: 'var(--primary-light)', color: 'var(--primary-dark)', marginBottom: '0.5rem' }}>
              {product.categoryName || 'Grocery'}
            </span>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.75rem' }}>{product.name}</h1>
            <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary)', marginBottom: '1rem' }}>
              ₹{Number(product.price).toFixed(2)}
            </div>

            <p style={{ color: 'var(--text-muted)', lineHeight: '1.6', marginBottom: '1.5rem' }}>
              {product.description || 'Premium quality grocery product packaged fresh for you.'}
            </p>

            <div style={{ marginBottom: '1.5rem' }}>
              <strong>Availability: </strong>
              {isInactive ? (
                <span className="stock-tag stock-out">Unavailable</span>
              ) : isOutOfStock ? (
                <span className="stock-tag stock-out">Out of Stock</span>
              ) : product.stock <= 5 ? (
                <span className="stock-tag stock-low">Only {product.stock} left</span>
              ) : (
                <span className="stock-tag stock-in">In Stock ({product.stock} available)</span>
              )}
            </div>
          </div>

          {(!user || user.role === 'CUSTOMER') && !isOutOfStock && !isInactive && (
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
              <div className="qty-control">
                <button
                  type="button"
                  className="qty-btn"
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  disabled={quantity <= 1}
                >
                  -
                </button>
                <span className="qty-val">{quantity}</span>
                <button
                  type="button"
                  className="qty-btn"
                  onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                  disabled={quantity >= product.stock}
                >
                  +
                </button>
              </div>

              <button
                onClick={handleAddToCart}
                disabled={adding}
                className="btn btn-primary"
                style={{ flex: 1 }}
              >
                {adding ? 'Adding...' : 'Add to Cart'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
