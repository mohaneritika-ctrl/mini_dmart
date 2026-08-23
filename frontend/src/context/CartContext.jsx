import React, { createContext, useContext, useState, useEffect } from 'react';
import { cartService } from '../services/cartService';
import { useAuth } from './AuthContext';

const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const { user, isAuthenticated } = useAuth();
  const [cart, setCart] = useState({ items: [], totalItems: 0, totalAmount: 0 });
  const [loading, setLoading] = useState(false);

  const fetchCart = async () => {
    if (!isAuthenticated || user?.role !== 'CUSTOMER') {
      setCart({ items: [], totalItems: 0, totalAmount: 0 });
      return;
    }
    try {
      setLoading(true);
      const data = await cartService.getCart();
      setCart(data || { items: [], totalItems: 0, totalAmount: 0 });
    } catch (err) {
      console.error('Failed to fetch cart:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, [isAuthenticated, user?.role]);

  const addToCart = async (productId, quantity = 1) => {
    const updated = await cartService.addToCart(productId, quantity);
    setCart(updated);
    return updated;
  };

  const updateItem = async (itemId, quantity) => {
    const updated = await cartService.updateCartItem(itemId, quantity);
    setCart(updated);
    return updated;
  };

  const removeItem = async (itemId) => {
    const updated = await cartService.removeCartItem(itemId);
    setCart(updated);
    return updated;
  };

  const clearCart = async () => {
    await cartService.clearCart();
    setCart({ items: [], totalItems: 0, totalAmount: 0 });
  };

  return (
    <CartContext.Provider
      value={{
        cart,
        cartCount: cart?.totalItems || 0,
        fetchCart,
        addToCart,
        updateItem,
        removeItem,
        clearCart,
        loading,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);