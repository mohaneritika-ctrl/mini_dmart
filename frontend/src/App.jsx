import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';

// Pages
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Products } from './pages/Products';
import { ProductDetail } from './pages/ProductDetail';
import { Categories } from './pages/Categories';
import { Cart } from './pages/Cart';
import { Checkout } from './pages/Checkout';
import { MyOrders } from './pages/MyOrders';
import { OrderDetail } from './pages/OrderDetail';
import { MyReturns } from './pages/MyReturns';
import { StaffDashboard } from './pages/StaffDashboard';
import { AdminDashboard } from './pages/AdminDashboard';
import { AdminCategories } from './pages/AdminCategories';
import { AdminProducts } from './pages/AdminProducts';
import { AdminInventory } from './pages/AdminInventory';
import { AdminOrders } from './pages/AdminOrders';
import { AdminReturns } from './pages/AdminReturns';
import { AdminUsers } from './pages/AdminUsers';
import { StaffProfile } from './pages/StaffProfile';

export const App = () => {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <div className="app-container">
            <Navbar />
            <main className="main-content">
              <Routes>
                {/* Public / Customer Routes */}
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/products" element={<Products />} />
                <Route path="/products/:id" element={<ProductDetail />} />
                <Route path="/categories" element={<Categories />} />

                {/* Protected Customer Routes */}
                <Route
                  path="/cart"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <Cart />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/checkout"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <Checkout />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/orders"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <MyOrders />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/orders/:id"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <OrderDetail />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/returns"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <MyReturns />
                    </ProtectedRoute>
                  }
                />

                {/* Protected Staff Routes */}
                <Route
                  path="/staff"
                  element={
                    <ProtectedRoute allowedRoles={['STAFF', 'ADMIN']}>
                      <StaffDashboard />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/staff/profile"
                  element={
                    <ProtectedRoute allowedRoles={['STAFF', 'ADMIN']}>
                      <StaffProfile />
                    </ProtectedRoute>
                  }
                />

                {/* Protected Admin Routes */}
                <Route
                  path="/admin"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                      <AdminDashboard />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin/users"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                      <AdminUsers />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin/categories"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                      <AdminCategories />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin/products"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                      <AdminProducts />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin/inventory"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                      <AdminInventory />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin/orders"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                      <AdminOrders />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin/returns"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                      <AdminReturns />
                    </ProtectedRoute>
                  }
                />

                {/* Profile Route for all authenticated users */}
                <Route
                  path="/profile"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER', 'STAFF', 'ADMIN']}>
                      <StaffProfile />
                    </ProtectedRoute>
                  }
                />

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </main>
          </div>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
};
export default App;