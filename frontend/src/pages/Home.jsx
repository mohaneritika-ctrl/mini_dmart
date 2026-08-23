import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import { ProductCard } from '../components/ProductCard';

const CATEGORY_ICONS = {
  'Fruits': '🍎',
  'Vegetables': '🥦',
  'Dairy': '🥛',
  'Beverages': '☕',
  'Snacks': '🥨',
  'Bakery': '🍞',
  'Personal Care': '🧴',
  'Household': '🧼',
  'Pantry Staples': '🌾'
};

export const Home = () => {
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadHomeData = async () => {
      try {
        setLoading(true);
        const [prodData, catData] = await Promise.all([
          productService.getProducts({ size: 8 }),
          categoryService.getCategories()
        ]);
        setFeaturedProducts(prodData.content || []);
        setCategories(catData || []);
      } catch (err) {
        console.error('Error loading home data:', err);
      } finally {
        setLoading(false);
      }
    };
    loadHomeData();
  }, []);

  return (
    <div>
      {/* Hero Section */}
      <section className="hero">
        <div style={{ display: 'inline-block', background: 'rgba(255, 255, 255, 0.18)', backdropFilter: 'blur(8px)', padding: '0.4rem 1rem', borderRadius: '999px', fontSize: '0.85rem', fontWeight: 800, marginBottom: '1.25rem', letterSpacing: '0.5px' }}>
          ✨ India's Trusted Daily Online Grocery
        </div>
        <h1>Fresh Groceries & Pantry Essentials<br />Delivered to Your Doorstep</h1>
        <p>Explore over 50+ handpicked daily essentials, dairy, farm-fresh produce, and household brands at everyday low supermarket prices.</p>
        <div className="hero-actions">
          <Link to="/products" className="btn btn-secondary btn-lg">
            🛒 Shop All Products
          </Link>
          <Link to="/categories" className="btn btn-outline btn-lg" style={{ color: 'white', borderColor: 'rgba(255,255,255,0.6)' }}>
            🏷️ Browse Categories
          </Link>
        </div>
      </section>

      {/* Trust Features Row */}
      <section className="trust-features">
        <div className="trust-feature-card">
          <div className="trust-icon">⚡</div>
          <div>
            <h4 style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--text-main)' }}>Superfast Delivery</h4>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Get essentials at your door in 30 mins</p>
          </div>
        </div>
        <div className="trust-feature-card">
          <div className="trust-icon">💰</div>
          <div>
            <h4 style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--text-main)' }}>Everyday Low Prices</h4>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Direct supermarket pricing & offers</p>
          </div>
        </div>
        <div className="trust-feature-card">
          <div className="trust-icon">🛡️</div>
          <div>
            <h4 style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--text-main)' }}>100% Quality Assured</h4>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Fresh farm harvest & authentic brands</p>
          </div>
        </div>
        <div className="trust-feature-card">
          <div className="trust-icon">🔄</div>
          <div>
            <h4 style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--text-main)' }}>7-Day Easy Returns</h4>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Hassle-free refunds & exchanges</p>
          </div>
        </div>
      </section>

      {/* Top Categories Grid */}
      <section style={{ marginBottom: '3.5rem' }}>
        <div className="page-header">
          <div>
            <h2 className="page-title">Shop by Category</h2>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Select a department to explore products</p>
          </div>
          <Link to="/categories" style={{ color: 'var(--primary)', fontWeight: 800, fontSize: '0.95rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
            View All Categories &rarr;
          </Link>
        </div>
        <div className="grid grid-4">
          {categories.slice(0, 8).map((cat) => (
            <Link
              key={cat.id}
              to={`/products?categoryId=${cat.id}`}
              className="card"
              style={{
                textAlign: 'center',
                padding: '1.5rem 1rem',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                background: '#ffffff',
                border: '1.5px solid var(--border)'
              }}
            >
              <div style={{
                fontSize: '2.5rem',
                marginBottom: '0.75rem',
                background: 'var(--primary-light)',
                width: '70px',
                height: '70px',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 2px 8px var(--primary-glow)'
              }}>
                {CATEGORY_ICONS[cat.name] || '🛒'}
              </div>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 800, color: 'var(--text-main)', marginBottom: '0.25rem' }}>
                {cat.name}
              </h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: '-webkit-box', WebkitLineClamp: 1, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                {cat.description}
              </p>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Products Showcase */}
      <section style={{ marginBottom: '2rem' }}>
        <div className="page-header">
          <div>
            <h2 className="page-title">Featured Daily Essentials</h2>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Top picked items based on customer favorites</p>
          </div>
          <Link to="/products" style={{ color: 'var(--primary)', fontWeight: 800, fontSize: '0.95rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
            View All 56 Items &rarr;
          </Link>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', background: '#ffffff', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>⏳</div>
            <div style={{ fontWeight: 700, color: 'var(--text-muted)' }}>Loading fresh grocery items...</div>
          </div>
        ) : (
          <div className="grid grid-4">
            {featuredProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
};
