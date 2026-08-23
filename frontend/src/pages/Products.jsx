import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productService } from '../services/productService';
import { categoryService } from '../services/categoryService';
import { ProductCard } from '../components/ProductCard';

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
    if (searchInput) params.set('keyword', searchInput);
    else params.delete('keyword');
    params.set('page', '0');
    setSearchParams(params);
  };

  const handleCategoryChange = (e) => {
    const newCat = e.target.value;
    const params = new URLSearchParams(searchParams);
    if (newCat) params.set('categoryId', newCat);
    else params.delete('categoryId');
    params.set('page', '0');
    setSearchParams(params);
  };

  const handlePageChange = (newPage) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', newPage.toString());
    setSearchParams(params);
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Grocery Products</h1>
        <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          Showing {products.length} of {totalElements} items
        </span>
      </div>

      {/* Filters & Search */}
      <div className="card" style={{ marginBottom: '2rem', padding: '1rem' }}>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', flex: '1 1 300px', gap: '0.5rem' }}>
            <input
              type="text"
              className="form-control"
              placeholder="Search products by name..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
            />
            <button type="submit" className="btn btn-primary">Search</button>
          </form>

          <div style={{ flex: '0 1 250px' }}>
            <select className="form-select" value={categoryId} onChange={handleCategoryChange}>
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Product List */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>Loading groceries...</div>
      ) : products.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
          <h3>No products found</h3>
          <p style={{ color: 'var(--text-muted)' }}>Try adjusting your search query or category filter.</p>
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
        <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '2.5rem' }}>
          <button
            onClick={() => handlePageChange(page - 1)}
            disabled={page === 0}
            className="btn btn-outline btn-sm"
          >
            &laquo; Previous
          </button>
          <span style={{ display: 'flex', alignItems: 'center', padding: '0 0.75rem', fontWeight: 600, fontSize: '0.9rem' }}>
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