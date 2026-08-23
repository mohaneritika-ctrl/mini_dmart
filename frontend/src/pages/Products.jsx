import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
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

export const Products = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const keyword = searchParams.get('keyword') || '';
  const categoryId = searchParams.get('categoryId') || '';
  const page = parseInt(searchParams.get('page') || '0', 10);

  const [searchInput, setSearchInput] = useState(keyword);

  useEffect(() => {
    setSearchInput(keyword);
  }, [keyword]);

  useEffect(() => {
    categoryService.getCategories().then(setCategories).catch(console.error);
  }, []);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const data = await productService.getProducts({
          page,
          size: 24,
          keyword,
          categoryId,
        });
        setProducts(data.content || []);
        setTotalPages(data.totalPages || 1);
        setTotalElements(data.totalElements || 0);
      } catch (err) {
        console.error('Failed to load products:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchProducts();
  }, [keyword, categoryId, page]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    const params = new URLSearchParams(searchParams);
    if (searchInput.trim()) params.set('keyword', searchInput.trim());
    else params.delete('keyword');
    params.set('page', '0');
    setSearchParams(params);
  };

  const handleClearSearch = () => {
    setSearchInput('');
    const params = new URLSearchParams(searchParams);
    params.delete('keyword');
    params.set('page', '0');
    setSearchParams(params);
  };

  const handleCategorySelect = (catId) => {
    const params = new URLSearchParams(searchParams);
    if (catId) {
      params.set('categoryId', catId);
    } else {
      params.delete('categoryId');
    }
    params.set('page', '0');
    setSearchParams(params);
  };

  const handleClearAll = () => {
    setSearchInput('');
    setSearchParams(new URLSearchParams());
  };

  const handlePageChange = (newPage) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', newPage.toString());
    setSearchParams(params);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Grocery & Pantry Catalog</h1>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
            Fresh daily staples, snacks, personal care, and household essentials
          </p>
        </div>
        <span style={{
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          padding: '0.4rem 0.85rem',
          borderRadius: '999px',
          color: 'var(--text-main)',
          fontSize: '0.85rem',
          fontWeight: 700,
          boxShadow: 'var(--shadow-sm)'
        }}>
          Showing <span style={{ color: 'var(--primary)' }}>{products.length}</span> of {totalElements} items
        </span>
      </div>

      {/* Category Pills Filter Bar */}
      <div className="category-pills">
        <button
          className={`pill-btn ${!categoryId ? 'active' : ''}`}
          onClick={() => handleCategorySelect('')}
        >
          🌟 All Items
        </button>
        {categories.map((c) => (
          <button
            key={c.id}
            className={`pill-btn ${categoryId === c.id.toString() ? 'active' : ''}`}
            onClick={() => handleCategorySelect(c.id.toString())}
          >
            {CATEGORY_ICONS[c.name] || '🛒'} {c.name}
          </button>
        ))}
      </div>

      {/* Search Bar */}
      <div className="card" style={{ marginBottom: '2rem', padding: '1rem' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <div style={{ position: 'relative', flex: '1 1 300px' }}>
            <input
              type="text"
              className="form-control"
              placeholder="Search products by name (e.g. Atta, Oil, Ghee, Butter)..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              style={{ paddingRight: searchInput ? '2.5rem' : '1rem' }}
            />
            {searchInput && (
              <button
                type="button"
                onClick={handleClearSearch}
                style={{
                  position: 'absolute',
                  right: '10px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none',
                  border: 'none',
                  color: 'var(--text-muted)',
                  cursor: 'pointer',
                  fontSize: '1.1rem',
                  fontWeight: 'bold'
                }}
              >
                ✕
              </button>
            )}
          </div>
          <button type="submit" className="btn btn-primary">
            🔍 Search
          </button>
          {(keyword || categoryId) && (
            <button type="button" onClick={handleClearAll} className="btn btn-outline">
              Clear Filters
            </button>
          )}
        </form>
      </div>

      {/* Product Grid */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem 2rem', background: '#ffffff', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)' }}>
          <div style={{ fontSize: '2.5rem', marginBottom: '0.75rem' }}>⏳</div>
          <div style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--text-main)' }}>Loading groceries...</div>
          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Fetching live stock and prices from database</div>
        </div>
      ) : products.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🔍</div>
          <h3 style={{ fontSize: '1.35rem', fontWeight: 800, marginBottom: '0.5rem' }}>No products found</h3>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', maxWidth: '400px', margin: '0 auto 1.5rem' }}>
            We couldn't find any groceries matching your search or category filter.
          </p>
          <button onClick={handleClearAll} className="btn btn-primary">
            Reset All Filters
          </button>
        </div>
      ) : (
        <div className="grid grid-4">
          {products.map((prod) => (
            <ProductCard key={prod.id} product={prod} />
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.6rem', marginTop: '3rem' }}>
          <button
            onClick={() => handlePageChange(page - 1)}
            disabled={page === 0}
            className="btn btn-outline btn-sm"
          >
            &laquo; Previous
          </button>
          <span style={{ display: 'flex', alignItems: 'center', padding: '0.4rem 1rem', fontWeight: 800, fontSize: '0.9rem', background: '#ffffff', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => handlePageChange(page + 1)}
            disabled={page >= totalPages - 1}
            className="btn btn-outline btn-sm"
          >
            Next &raquo;
          </button>
        </div>
      )}
    </div>
  );
};