/* ================================
   Orders Page JavaScript
   Handles order listing, filtering, and detail view
   ================================ */

// Global variables
let allOrders = [];
let currentFilter = 'all';
let currentOrder = null;

// ================================
// Page Initialization
// ================================

document.addEventListener('DOMContentLoaded', () => {
  console.log('Orders page loaded');
  
  // Check user session
  checkUserSession();
  
  // Load orders
  loadOrders();
  
  // Setup event listeners
  setupEventListeners();
});

// ================================
// Event Listeners
// ================================

function setupEventListeners() {
  // Filter tabs
  const filterTabs = document.querySelectorAll('.filter-tab');
  filterTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      filterTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      currentFilter = tab.dataset.status;
      filterOrders();
    });
  });
  
  // Close modal on escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeOrderModal();
    }
  });
}

// ================================
// Session Management
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
      showToast('Please login to view your orders');
      setTimeout(() => {
        window.location.href = 'login.html?redirect=orders.html';
      }, 1500);
      return;
    }
    
    // User is logged in, update navbar
    updateNavbarForLoggedInUser(data.user);
    
  } catch (error) {
    console.error('Error checking session:', error);
    showToast('Please login to continue');
    setTimeout(() => {
      window.location.href = 'login.html?redirect=orders.html';
    }, 1500);
  }
}

function updateNavbarForLoggedInUser(user) {
  const userSection = document.getElementById('userSection');
  if (!userSection) return;
  
  const loginBtn = userSection.querySelector('.btn-login');
  if (loginBtn) {
    loginBtn.remove();
  }
  
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
  dropdown.style.cssText = `
    position: fixed;
    background: white;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    padding: 0.5rem;
    min-width: 200px;
    z-index: 1001;
  `;
  
  dropdown.innerHTML = `
    <div style="padding: 1rem; border-bottom: 1px solid #E5E5E5;">
      <strong style="display: block; color: #1A1A1A; margin-bottom: 0.25rem;">${user.fullName || user.username}</strong>
      <small style="display: block; color: #7A7A7A; font-size: 0.875rem;">${user.email}</small>
    </div>
    <ul style="list-style: none; padding: 0.5rem 0; margin: 0;">
      <li><a href="product.html" style="display: block; padding: 0.75rem 1rem; color: #4A4A4A; text-decoration: none; border-radius: 8px; transition: all 0.2s ease; font-weight: 500;">Menu</a></li>
      <li><a href="orders.html" style="display: block; padding: 0.75rem 1rem; color: #FF6B35; text-decoration: none; border-radius: 8px; transition: all 0.2s ease; font-weight: 500;">My Orders</a></li>
      ${user.role === 'admin' ? '<li><a href="admin.html" style="display: block; padding: 0.75rem 1rem; color: #4A4A4A; text-decoration: none; border-radius: 8px; transition: all 0.2s ease; font-weight: 500;">Admin Panel</a></li>' : ''}
      <li><a href="#" id="logoutLink" style="display: block; padding: 0.75rem 1rem; color: #4A4A4A; text-decoration: none; border-radius: 8px; transition: all 0.2s ease; font-weight: 500;">Logout</a></li>
    </ul>
  `;
  
  const userInfo = document.querySelector('.user-info');
  const rect = userInfo.getBoundingClientRect();
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
// Load Orders
// ================================

async function loadOrders() {
  const loadingState = document.getElementById('loadingState');
  const emptyState = document.getElementById('emptyState');
  const ordersList = document.getElementById('ordersList');
  
  try {
    loadingState.style.display = 'block';
    emptyState.style.display = 'none';
    ordersList.innerHTML = '';
    
    // Fetch orders from backend
    const response = await fetch('/201Project/OrderServlet?action=list', {
      method: 'GET',
      credentials: 'include'
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch orders');
    }
    
    const data = await response.json();
    
    if (data.success && data.orders) {
      allOrders = data.orders;
      loadingState.style.display = 'none';
      
      if (allOrders.length === 0) {
        emptyState.style.display = 'block';
      } else {
        displayOrders(allOrders);
      }
    } else {
      throw new Error(data.message || 'Failed to load orders');
    }
    
  } catch (error) {
    console.error('Error loading orders:', error);
    loadingState.style.display = 'none';
    
    // Load mock data for demo
    console.log('Loading mock data for demo...');
    loadMockOrders();
  }
}

function loadMockOrders() {
  const loadingState = document.getElementById('loadingState');
  const emptyState = document.getElementById('emptyState');
  
  // Mock orders data
  allOrders = [
    {
      orderId: 1001,
      orderDate: '2024-01-12 14:30:00',
      totalAmount: 45.97,
      status: 'delivered',
      deliveryAddress: '123 Main Street, Penang, Malaysia',
      paymentMethod: 'card',
      paymentStatus: 'paid',
      notes: 'Please ring the doorbell',
      items: [
        { productName: 'Margherita Pizza', quantity: 2, unitPrice: 12.99, category: 'main_course' },
        { productName: 'Caesar Salad', quantity: 1, unitPrice: 8.99, category: 'appetizer' },
        { productName: 'Coke', quantity: 2, unitPrice: 2.50, category: 'beverage' }
      ]
    },
    {
      orderId: 1002,
      orderDate: '2024-01-11 18:45:00',
      totalAmount: 32.98,
      status: 'preparing',
      deliveryAddress: '123 Main Street, Penang, Malaysia',
      paymentMethod: 'cash',
      paymentStatus: 'pending',
      notes: '',
      items: [
        { productName: 'Classic Burger', quantity: 2, unitPrice: 10.99, category: 'main_course' },
        { productName: 'French Fries', quantity: 2, unitPrice: 5.50, category: 'appetizer' }
      ]
    },
    {
      orderId: 1003,
      orderDate: '2024-01-10 12:15:00',
      totalAmount: 58.94,
      status: 'confirmed',
      deliveryAddress: '123 Main Street, Penang, Malaysia',
      paymentMethod: 'online',
      paymentStatus: 'paid',
      notes: 'Extra spicy please',
      items: [
        { productName: 'Pad Thai', quantity: 3, unitPrice: 12.99, category: 'main_course' },
        { productName: 'Spring Rolls', quantity: 2, unitPrice: 6.99, category: 'appetizer' },
        { productName: 'Thai Iced Tea', quantity: 3, unitPrice: 3.99, category: 'beverage' }
      ]
    }
  ];
  
  loadingState.style.display = 'none';
  displayOrders(allOrders);
}

// ================================
// Display Orders
// ================================

function displayOrders(orders) {
  const ordersList = document.getElementById('ordersList');
  const emptyState = document.getElementById('emptyState');
  
  if (orders.length === 0) {
    ordersList.innerHTML = '';
    emptyState.style.display = 'block';
    return;
  }
  
  emptyState.style.display = 'none';
  ordersList.innerHTML = '';
  
  orders.forEach(order => {
    const orderCard = createOrderCard(order);
    ordersList.appendChild(orderCard);
  });
}

function createOrderCard(order) {
  const card = document.createElement('div');
  card.className = 'order-card';
  
  const orderDate = formatDate(order.orderDate);
  const itemsCount = order.items.reduce((sum, item) => sum + item.quantity, 0);
  
  card.innerHTML = `
    <div class="order-card-header">
      <div class="order-info">
        <div class="order-id">Order #${order.orderId}</div>
        <div class="order-date">📅 ${orderDate}</div>
      </div>
      <div class="order-status status-${order.status}">
        ${order.status}
      </div>
    </div>
    
    <div class="order-card-body">
      <div class="order-items-preview">
        ${order.items.slice(0, 3).map(item => `
          <div class="order-item-preview">
            <span class="item-emoji">${getCategoryEmoji(item.category)}</span>
            <div class="item-details">
              <div class="item-name">${escapeHtml(item.productName)}</div>
              <div class="item-quantity">Quantity: ${item.quantity}</div>
            </div>
          </div>
        `).join('')}
        ${order.items.length > 3 ? `<div style="color: #666; font-size: 0.875rem; margin-left: 2.5rem;">+${order.items.length - 3} more items</div>` : ''}
      </div>
      
      <div class="order-summary">
        <div class="order-total">$${parseFloat(order.totalAmount).toFixed(2)}</div>
        <div class="order-items-count">${itemsCount} items</div>
        <button class="btn-view-details">View Details</button>
      </div>
    </div>
  `;
  
  card.addEventListener('click', (e) => {
    if (!e.target.classList.contains('btn-view-details')) {
      openOrderModal(order);
    }
  });
  
  const viewDetailsBtn = card.querySelector('.btn-view-details');
  viewDetailsBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    openOrderModal(order);
  });
  
  return card;
}

// ================================
// Filter Orders
// ================================

function filterOrders() {
  let filteredOrders = allOrders;
  
  if (currentFilter !== 'all') {
    filteredOrders = allOrders.filter(order => 
      order.status.toLowerCase() === currentFilter.toLowerCase()
    );
  }
  
  displayOrders(filteredOrders);
}

// ================================
// Order Detail Modal
// ================================

function openOrderModal(order) {
  currentOrder = order;
  const modal = document.getElementById('orderModal');
  
  // Update modal header
  document.getElementById('modalOrderId').textContent = order.orderId;
  const statusBadge = document.getElementById('modalOrderStatus');
  statusBadge.className = `order-status-badge status-${order.status}`;
  statusBadge.textContent = order.status;
  
  // Update timeline
  updateTimeline(order);
  
  // Update order items
  const itemsList = document.getElementById('modalOrderItems');
  itemsList.innerHTML = order.items.map(item => `
    <div class="order-item">
      <span class="order-item-emoji">${getCategoryEmoji(item.category)}</span>
      <div class="order-item-info">
        <div class="order-item-name">${escapeHtml(item.productName)}</div>
        <div class="order-item-quantity">${item.quantity} × $${parseFloat(item.unitPrice).toFixed(2)}</div>
      </div>
      <div class="order-item-price">$${(item.quantity * parseFloat(item.unitPrice)).toFixed(2)}</div>
    </div>
  `).join('');
  
  // Update delivery info
  document.getElementById('modalAddress').textContent = order.deliveryAddress;
  document.getElementById('modalNotes').textContent = order.notes || 'No special instructions';
  
  // Update payment summary
  const deliveryFee = 5.00;
  const subtotal = parseFloat(order.totalAmount) - deliveryFee;
  document.getElementById('modalSubtotal').textContent = `$${subtotal.toFixed(2)}`;
  document.getElementById('modalDelivery').textContent = `$${deliveryFee.toFixed(2)}`;
  document.getElementById('modalTotal').textContent = `$${parseFloat(order.totalAmount).toFixed(2)}`;
  document.getElementById('modalPayment').textContent = order.paymentMethod.toUpperCase();
  
  // Show modal
  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
}

function closeOrderModal() {
  const modal = document.getElementById('orderModal');
  modal.classList.remove('active');
  document.body.style.overflow = 'auto';
  currentOrder = null;
}

function updateTimeline(order) {
  const statuses = ['pending', 'confirmed', 'preparing', 'ready', 'delivered'];
  const currentStatusIndex = statuses.indexOf(order.status.toLowerCase());
  
  const timelineItems = document.querySelectorAll('.timeline-item');
  
  timelineItems.forEach((item, index) => {
    item.classList.remove('completed', 'active');
    
    if (index < currentStatusIndex) {
      item.classList.add('completed');
    } else if (index === currentStatusIndex) {
      item.classList.add('active');
    }
  });
  
  // Update timeline text
  document.getElementById('timelinePlaced').textContent = formatDate(order.orderDate);
  document.getElementById('timelineConfirmed').textContent = currentStatusIndex >= 1 ? formatDate(order.orderDate) : 'Pending';
  document.getElementById('timelinePreparing').textContent = currentStatusIndex >= 2 ? formatDate(order.orderDate) : 'Pending';
  document.getElementById('timelineReady').textContent = currentStatusIndex >= 3 ? formatDate(order.orderDate) : 'Pending';
  document.getElementById('timelineDelivered').textContent = currentStatusIndex >= 4 ? formatDate(order.orderDate) : 'Pending';
}

// ================================
// Reorder Function
// ================================

function reorder() {
  if (!currentOrder) return;
  
  // Get cart from localStorage
  let cart = JSON.parse(localStorage.getItem('foodhub_cart') || '[]');
  
  // Add all items from the order to cart
  currentOrder.items.forEach(item => {
    const existingItem = cart.find(cartItem => cartItem.productName === item.productName);
    
    if (existingItem) {
      existingItem.quantity += item.quantity;
    } else {
      cart.push({
        productId: item.productId || Math.floor(Math.random() * 1000),
        productName: item.productName,
        price: parseFloat(item.unitPrice),
        quantity: item.quantity,
        category: item.category
      });
    }
  });
  
  // Save cart
  localStorage.setItem('foodhub_cart', JSON.stringify(cart));
  
  // Close modal and redirect
  showToast('Items added to cart!');
  setTimeout(() => {
    window.location.href = 'product.html';
  }, 1000);
}

// ================================
// Utility Functions
// ================================

function formatDate(dateString) {
  const date = new Date(dateString);
  const options = { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  };
  return date.toLocaleDateString('en-US', options);
}

function getCategoryEmoji(category) {
  const emojiMap = {
    'appetizer': '🥗',
    'main_course': '🍔',
    'dessert': '🍰',
    'beverage': '🥤',
    'other': '🍽️'
  };
  return emojiMap[category?.toLowerCase()] || '🍽️';
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