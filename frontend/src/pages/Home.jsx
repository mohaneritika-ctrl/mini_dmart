import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import { ProductCard } from '../components/ProductCard';

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
      <section className="hero">
        <h1>Welcome to Mini D-Mart</h1>
        <p>Your one-stop destination for daily grocery essentials, fresh pantry staples, and beverages at everyday low prices.</p>
        <div className="hero-actions">
          <Link to="/products" className="btn btn-secondary">Shop All Products</Link>
          <Link to="/categories" className="btn btn-outline" style={{ color: 'white', borderColor: 'rgba(255,255,255,0.4)' }}>Browse Categories</Link>
        </div>
      </section>

      {/* Categories highlight */}
      <section style={{ marginBottom: '3rem' }}>
        <div className="page-header">
          <h2 className="page-title">Top Categories</h2>
          <Link to="/categories" style={{ color: 'var(--primary)', fontWeight: 600 }}>View All &rarr;</Link>
        </div>
        <div className="grid grid-4">
          {categories.slice(0, 4).map((cat) => (
            <Link key={cat.id} to={`/products?categoryId=${cat.id}`} className="card" style={{ textAlign: 'center', padding: '1.25rem' }}>
              <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>🛒</div>
              <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>{cat.name}</h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{cat.description}</p>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured products */}
      <section>
        <div className="page-header">
          <h2 className="page-title">Featured Products</h2>
          <Link to="/products" style={{ color: 'var(--primary)', fontWeight: 600 }}>See More &rarr;</Link>
        </div>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '2rem' }}>Loading products...</div>
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
