/* ================================
   Checkout Page JavaScript
   Handles order placement and form validation
   ================================ */

// ================================
// Page Initialization
// ================================

document.addEventListener('DOMContentLoaded', () => {
  console.log('Checkout page loaded');
  
  // Check user session
  checkUserSession();
  
  // Load order summary
  loadOrderSummary();
  
  // Load user info if available
  loadUserInfo();
  
  // Update cart count
  updateCartCount();
});

// ================================
// Session Check
// ================================

async function checkUserSession() {
  try {
    const response = await fetch('/201Project/LoginServlet?action=checkSession', {
      method: 'GET',
      credentials: 'include'
    });
    
    const data = await response.json();
    
    if (!data.success || !data.user) {
      // User not logged in, redirect to login
      showToast('Please login to continue');
      setTimeout(() => {
        window.location.href = 'login.html?redirect=checkout.html';
      }, 1500);
      return;
    }
    
    // User is logged in, update navbar
    updateNavbarForLoggedInUser(data.user);
    
  } catch (error) {
    console.error('Error checking session:', error);
    showToast('Please login to continue');
    setTimeout(() => {
      window.location.href = 'login.html?redirect=checkout.html';
    }, 1500);
  }
}

// ================================
// Load User Information
// ================================

async function loadUserInfo() {
  try {
    const response = await fetch('/201Project/LoginServlet?action=checkSession', {
      method: 'GET',
      credentials: 'include'
    });
    
    const data = await response.json();
    
    if (data.success && data.user) {
      // Pre-fill form with user data
      const fullNameInput = document.getElementById('fullName');
      const phoneInput = document.getElementById('phone');
      
      if (data.user.fullName && fullNameInput) {
        fullNameInput.value = data.user.fullName;
      }
      
      if (data.user.phone && phoneInput) {
        phoneInput.value = data.user.phone;
      }
    }
  } catch (error) {
    console.error('Error loading user info:', error);
  }
}

// ================================
// Load Order Summary
// ================================

function loadOrderSummary() {
  const cart = getCart();
  const summaryItems = document.getElementById('summaryItems');
  
  if (cart.length === 0) {
    summaryItems.innerHTML = '<p class="empty-cart-message">Your cart is empty</p>';
    updateOrderTotals(0);
    return;
  }
  
  let itemsHTML = '';
  let subtotal = 0;
  
  cart.forEach(item => {
    const itemTotal = item.price * item.quantity;
    subtotal += itemTotal;
    const emoji = getCategoryEmoji(item.category || 'other');
    
    itemsHTML += `
      <div class="summary-item">
        <div class="summary-item-info">
          <span class="summary-item-emoji">${emoji}</span>
          <div class="summary-item-details">
            <h4>${escapeHtml(item.productName)}</h4>
            <p>$${item.price.toFixed(2)} × ${item.quantity}</p>
          </div>
        </div>
        <div class="summary-item-total">
          $${itemTotal.toFixed(2)}
        </div>
      </div>
    `;
  });
  
  summaryItems.innerHTML = itemsHTML;
  updateOrderTotals(subtotal);
}

function updateOrderTotals(subtotal) {
  const deliveryFee = 5.00;
  const total = subtotal + deliveryFee;
  
  document.getElementById('summarySubtotal').textContent = `$${subtotal.toFixed(2)}`;
  document.getElementById('summaryDelivery').textContent = `$${deliveryFee.toFixed(2)}`;
  document.getElementById('summaryTotal').textContent = `$${total.toFixed(2)}`;
}

// ================================
// Place Order
// ================================

async function placeOrder() {
  const form = document.getElementById('checkoutForm');
  
  // Validate form
  if (!form.checkValidity()) {
    form.reportValidity();
    return;
  }
  
  const cart = getCart();
  
  if (cart.length === 0) {
    showToast('Your cart is empty!');
    return;
  }
  
  // Get form data
  const fullName = document.getElementById('fullName').value.trim();
  const phone = document.getElementById('phone').value.trim();
  const address = document.getElementById('address').value.trim();
  const notes = document.getElementById('notes').value.trim();
  const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
  
  // Calculate total
  const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const deliveryFee = 5.00;
  const totalAmount = subtotal + deliveryFee;
  
  // Prepare order data
  const orderData = {
    deliveryAddress: address,
    paymentMethod: paymentMethod,
    totalAmount: totalAmount.toFixed(2),
    notes: notes,
    items: cart.map(item => ({
      productId: item.productId,
      quantity: item.quantity,
      unitPrice: item.price.toFixed(2)
    }))
  };
  
  try {
    // Show loading state
    const placeOrderBtn = document.querySelector('.btn-place-order');
    const originalText = placeOrderBtn.innerHTML;
    placeOrderBtn.disabled = true;
    placeOrderBtn.innerHTML = '<span>Processing...</span>';
    
    // Send order to backend
    const response = await fetch('/201Project/OrderServlet?action=create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify(orderData)
    });
    
    const data = await response.json();
    
    if (data.success) {
      // Order placed successfully
      showToast('Order placed successfully!');
      
      // Clear cart
      localStorage.removeItem('foodhub_cart');
      updateCartCount();
      
      // Redirect to order confirmation page
      setTimeout(() => {
        window.location.href = `orders.html?orderId=${data.orderId}`;
      }, 1500);
      
    } else {
      // Order failed
      showToast(data.message || 'Failed to place order. Please try again.');
      placeOrderBtn.disabled = false;
      placeOrderBtn.innerHTML = originalText;
    }
    
  } catch (error) {
    console.error('Error placing order:', error);
    showToast('Failed to place order. Please try again.');
    
    const placeOrderBtn = document.querySelector('.btn-place-order');
    placeOrderBtn.disabled = false;
    placeOrderBtn.innerHTML = '<span>Place Order</span><span class="btn-icon">✓</span>';
  }
}

// ================================
// Cart Management Functions
// ================================

function getCart() {
  const cartData = localStorage.getItem('foodhub_cart');
  return cartData ? JSON.parse(cartData) : [];
}

function updateCartCount() {
  const cart = getCart();
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  
  const cartCount = document.getElementById('cartCount');
  if (cartCount) {
    cartCount.textContent = totalItems;
  }
}

// ================================
// Navbar User Management
// ================================

function updateNavbarForLoggedInUser(user) {
  const userSection = document.getElementById('userSection');
  if (!userSection) return;
  
  // Check if user info already exists
  if (userSection.querySelector('.user-info')) {
    return;
  }
  
  const userInfo = document.createElement('div');
  userInfo.className = 'user-info';
  userInfo.innerHTML = `
    <div class="user-avatar">${user.username.charAt(0).toUpperCase()}</div>
    <span class="user-name">${user.username}</span>
  `;
  
  userInfo.addEventListener('click', () => {
    showUserMenu(user);
  });
  
  userSection.appendChild(userInfo);
}

function showUserMenu(user) {
  const existingMenu = document.querySelector('.user-dropdown');
  if (existingMenu) {
    existingMenu.remove();
    return;
  }
  
  const dropdown = document.createElement('div');
  dropdown.className = 'user-dropdown';
  dropdown.innerHTML = `
    <div class="dropdown-header">
      <strong>${user.fullName || user.username}</strong>
      <small>${user.email}</small>
    </div>
    <ul class="dropdown-menu">
      <li><a href="orders.html">My Orders</a></li>
      ${user.role === 'admin' ? '<li><a href="admin.html">Admin Panel</a></li>' : ''}
      <li><a href="#" id="logoutLink">Logout</a></li>
    </ul>
  `;
  
  const userInfo = document.querySelector('.user-info');
  const rect = userInfo.getBoundingClientRect();
  dropdown.style.position = 'fixed';
  dropdown.style.top = `${rect.bottom + 10}px`;
  dropdown.style.right = `${window.innerWidth - rect.right}px`;
  
  document.body.appendChild(dropdown);
  
  const logoutLink = dropdown.querySelector('#logoutLink');
  logoutLink.addEventListener('click', (e) => {
    e.preventDefault();
    handleLogout();
  });
  
  setTimeout(() => {
    document.addEventListener('click', function closeDropdown(e) {
      if (!dropdown.contains(e.target) && !userInfo.contains(e.target)) {
        dropdown.remove();
        document.removeEventListener('click', closeDropdown);
      }
    });
  }, 100);
}

async function handleLogout() {
  try {
    await fetch('/201Project/LoginServlet?action=logout', {
      method: 'GET',
      credentials: 'include'
    });
    window.location.href = 'login.html';
  } catch (error) {
    console.error('Logout error:', error);
    window.location.href = 'login.html';
  }
}

// ================================
// Cart Sidebar Functions (for navbar cart icon)
// ================================

function openCartSidebar() {
  const cartSidebar = document.getElementById('cartSidebar');
  if (cartSidebar) {
    cartSidebar.classList.add('active');
    document.body.style.overflow = 'hidden';
    renderCartItems();
  }
}

function closeCartSidebar() {
  const cartSidebar = document.getElementById('cartSidebar');
  if (cartSidebar) {
    cartSidebar.classList.remove('active');
    document.body.style.overflow = 'auto';
  }
}

function renderCartItems() {
  const cart = getCart();
  const cartContent = document.getElementById('cartContent');
  
  if (!cartContent) return;
  
  if (cart.length === 0) {
    cartContent.innerHTML = `
      <div class="cart-empty">
        <div class="cart-empty-icon">🛒</div>
        <h3>Your cart is empty</h3>
        <p>Add some delicious items to get started!</p>
      </div>
    `;
    updateCartSummary(0);
    return;
  }
  
  let cartHTML = '<div class="cart-items">';
  
  cart.forEach(item => {
    const itemTotal = item.price * item.quantity;
    const emoji = getCategoryEmoji(item.category || 'other');
    
    cartHTML += `
      <div class="cart-item">
        <div class="cart-item-image">
          <div class="cart-item-emoji">${emoji}</div>
        </div>
        <div class="cart-item-details">
          <h4 class="cart-item-name">${escapeHtml(item.productName)}</h4>
          <p class="cart-item-price">$${item.price.toFixed(2)} each</p>
          <div class="cart-item-controls">
            <div class="quantity-controls">
              <button class="qty-btn" onclick="updateCartItemQuantity(${item.productId}, -1)">−</button>
              <input type="number" class="qty-input" value="${item.quantity}" readonly>
              <button class="qty-btn" onclick="updateCartItemQuantity(${item.productId}, 1)">+</button>
            </div>
            <button class="btn-remove-item" onclick="removeCartItem(${item.productId})">🗑️</button>
          </div>
        </div>
        <div class="cart-item-total">
          <span class="item-total-price">$${itemTotal.toFixed(2)}</span>
        </div>
      </div>
    `;
  });
  
  cartHTML += '</div>';
  cartContent.innerHTML = cartHTML;
  
  const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  updateCartSummary(total);
}

function updateCartSummary(total) {
  const subtotalEl = document.getElementById('cartSubtotal');
  const totalEl = document.getElementById('cartTotal');
  
  if (subtotalEl) subtotalEl.textContent = `$${total.toFixed(2)}`;
  if (totalEl) totalEl.textContent = `$${total.toFixed(2)}`;
}

function updateCartItemQuantity(productId, change) {
  let cart = getCart();
  const item = cart.find(i => i.productId === productId);
  
  if (!item) return;
  
  item.quantity += change;
  
  if (item.quantity <= 0) {
    cart = cart.filter(i => i.productId !== productId);
    showToast('Item removed from cart');
  } else if (item.quantity > 99) {
    item.quantity = 99;
  }
  
  localStorage.setItem('foodhub_cart', JSON.stringify(cart));
  updateCartCount();
  renderCartItems();
  loadOrderSummary();
}

function removeCartItem(productId) {
  let cart = getCart();
  const item = cart.find(i => i.productId === productId);
  
  if (!item) return;
  
  cart = cart.filter(i => i.productId !== productId);
  localStorage.setItem('foodhub_cart', JSON.stringify(cart));
  
  updateCartCount();
  renderCartItems();
  loadOrderSummary();
  showToast(`${item.productName} removed from cart`);
}

// ================================
// Utility Functions
// ================================

function getCategoryEmoji(category) {
  const emojiMap = {
    'appetizer': '🥗',
    'main_course': '🍔',
    'dessert': '🍰',
    'beverage': '🥤',
    'other': '🍽️'
  };
  return emojiMap[category.toLowerCase()] || '🍽️';
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function showToast(message) {
  const toast = document.getElementById('toast');
  const toastMessage = document.getElementById('toastMessage');
  
  if (toast && toastMessage) {
    toastMessage.textContent = message;
    toast.classList.add('show');
    
    setTimeout(() => {
      toast.classList.remove('show');
    }, 3000);
  }
}