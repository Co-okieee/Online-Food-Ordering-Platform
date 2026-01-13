/* ================================
   Admin Dashboard JavaScript
   Connected to Backend Servlets
   ================================ */

// ================================
// Authentication Check
// ================================
function checkAuthentication() {
  // Check if user is logged in and is admin
  fetch('/201Project/LoginServlet?action=checkSession')
    .then(res => res.json())
    .then(data => {
      if (!data.loggedIn || data.user.role !== 'admin') {
        location.href = 'login.html';
      } else {
        document.getElementById('adminName').textContent = data.user.username;
      }
    })
    .catch(() => {
      location.href = 'login.html';
    });
}

// ================================
// Logout Handler
// ================================
document.getElementById('logoutBtn').addEventListener('click', () => {
  if (confirm('Are you sure you want to logout?')) {
    fetch('/201Project/LoginServlet?action=logout', { method: 'POST' })
      .then(() => {
        location.href = 'login.html';
      })
      .catch(() => {
        location.href = 'login.html';
      });
  }
});

// ================================
// Load Dashboard Statistics
// ================================
function loadStatistics() {
  // Update last update time
  const now = new Date();
  document.getElementById('lastUpdate').textContent = now.toLocaleTimeString();

  // Load products count
  loadProducts();
  
  // Load orders count
  loadOrders();
  
  // Load users count
  loadUsers();
}

// ================================
// Product Management
// ================================
let currentProducts = [];

function loadProducts() {
  fetch('/201Project/ProductServlet?action=list')
    .then(res => res.json())
    .then(data => {
      currentProducts = data.products || [];
      document.getElementById('totalProducts').textContent = currentProducts.length;
      renderProductsTable(currentProducts);
    })
    .catch(error => {
      console.error('Error loading products:', error);
      document.getElementById('productsTableBody').innerHTML = 
        '<tr><td colspan="7" class="loading-cell">Failed to load products</td></tr>';
    });
}

function renderProductsTable(products) {
  const tbody = document.getElementById('productsTableBody');
  
  if (products.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="loading-cell">No products found</td></tr>';
    return;
  }
  
  tbody.innerHTML = products.map(product => `
    <tr>
      <td>${product.productId}</td>
      <td>${product.productName}</td>
      <td>${product.category}</td>
      <td>$${parseFloat(product.price).toFixed(2)}</td>
      <td>${product.stock || 0}</td>
      <td>
        <span class="status-badge ${product.status || 'available'}">
          ${product.status || 'available'}
        </span>
      </td>
      <td>
        <div class="action-buttons">
          <button class="btn-action btn-edit" onclick="editProduct(${product.productId})">Edit</button>
          <button class="btn-action btn-delete" onclick="deleteProduct(${product.productId})">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

// Add Product Button Handler
document.getElementById('addProductBtn').addEventListener('click', () => {
  document.getElementById('productModalTitle').textContent = 'Add New Product';
  document.getElementById('productForm').reset();
  document.getElementById('productId').value = '';
  document.getElementById('productModal').classList.add('active');
});

// Close Modal Handlers
document.getElementById('closeProductModal').addEventListener('click', () => {
  document.getElementById('productModal').classList.remove('active');
});

document.getElementById('cancelProductBtn').addEventListener('click', () => {
  document.getElementById('productModal').classList.remove('active');
});

// Edit Product Function
function editProduct(productId) {
  const product = currentProducts.find(p => p.productId === productId);
  if (!product) return;
  
  document.getElementById('productModalTitle').textContent = 'Edit Product';
  document.getElementById('productId').value = product.productId;
  document.getElementById('productName').value = product.productName;
  document.getElementById('productCategory').value = product.category;
  document.getElementById('productPrice').value = product.price;
  document.getElementById('productDescription').value = product.description || '';
  document.getElementById('productStock').value = product.stock || 0;
  document.getElementById('productStatus').value = product.status || 'available';
  
  document.getElementById('productModal').classList.add('active');
}

// Delete Product Function
function deleteProduct(productId) {
  if (!confirm('Are you sure you want to delete this product?')) return;
  
  const params = new URLSearchParams();
  params.append('action', 'delete');
  params.append('productId', productId);
  
  fetch('/201Project/ProductServlet', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
    body: params
  })
    .then(res => res.json())
    .then(data => {
      if (data.success) {
        showToast('Product deleted successfully', 'success');
        loadProducts();
      } else {
        showToast(data.message || 'Failed to delete product', 'error');
      }
    })
    .catch(error => {
      console.error('Error deleting product:', error);
      showToast('Failed to delete product', 'error');
    });
}

// Product Form Submit Handler
document.getElementById('productForm').addEventListener('submit', (e) => {
  e.preventDefault();
  
  const formData = new FormData(e.target);
  const params = new URLSearchParams();
  
  const productId = document.getElementById('productId').value;
  params.append('action', productId ? 'update' : 'add');
  
  if (productId) {
    params.append('productId', productId);
  }
  
  params.append('name', formData.get('productName'));
  params.append('category', formData.get('productCategory'));
  params.append('price', formData.get('productPrice'));
  params.append('description', formData.get('productDescription'));
  params.append('stock', formData.get('productStock'));
  params.append('status', formData.get('productStatus'));
  
  fetch('/201Project/ProductServlet', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
    body: params
  })
    .then(res => res.json())
    .then(data => {
      if (data.success) {
        showToast(productId ? 'Product updated successfully' : 'Product added successfully', 'success');
        document.getElementById('productModal').classList.remove('active');
        loadProducts();
      } else {
        showToast(data.message || 'Failed to save product', 'error');
      }
    })
    .catch(error => {
      console.error('Error saving product:', error);
      showToast('Failed to save product', 'error');
    });
});

// ================================
// Order Management
// ================================
let currentOrders = [];
let orderFilter = 'all';

function loadOrders() {
  fetch('/201Project/OrderServlet?action=listAll')
    .then(res => res.json())
    .then(data => {
      currentOrders = data.orders || [];
      document.getElementById('totalOrders').textContent = currentOrders.length;
      
      // Calculate revenue
      const revenue = currentOrders.reduce((sum, order) => 
        sum + parseFloat(order.totalAmount || 0), 0
      );
      document.getElementById('totalRevenue').textContent = '$' + revenue.toFixed(2);
      
      filterOrders(orderFilter);
    })
    .catch(error => {
      console.error('Error loading orders:', error);
      document.getElementById('ordersTableBody').innerHTML = 
        '<tr><td colspan="6" class="loading-cell">Failed to load orders</td></tr>';
    });
}

function filterOrders(status) {
  orderFilter = status;
  
  // Update filter button states
  document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.status === status);
  });
  
  const filtered = status === 'all' 
    ? currentOrders 
    : currentOrders.filter(order => order.status === status);
  
  renderOrdersTable(filtered);
}

function renderOrdersTable(orders) {
  const tbody = document.getElementById('ordersTableBody');
  
  if (orders.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="loading-cell">No orders found</td></tr>';
    return;
  }
  
  tbody.innerHTML = orders.map(order => `
  <tr>
    <td>#${order.orderId}</td>
    <td>${order.username || 'Guest'}</td>
    <td>${new Date(order.orderDate).toLocaleDateString()}</td>
    <td>$${parseFloat(order.totalAmount).toFixed(2)}</td>
    <td>
      <span class="status-badge ${order.status}">
        ${order.status}
      </span>
    </td>
    <td>
      <div class="action-buttons">
        <button class="btn-action btn-view" onclick="viewOrder(${order.orderId})">View</button>
      </div>
    </td>
  </tr>
`).join('');
}

// Filter Button Handlers
document.querySelectorAll('.filter-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    filterOrders(btn.dataset.status);
  });
});

function viewOrder(orderId) {
  const order = currentOrders.find(o => o.orderId === orderId);
  if (!order) return;
  
  // Fill order details
  document.getElementById('orderDetailId').textContent = '#' + order.orderId;
  document.getElementById('orderDetailCustomer').textContent = order.username || 'Guest';
  document.getElementById('orderDetailDate').textContent = new Date(order.orderDate).toLocaleString();
  
  // Set status with proper styling
  const statusElement = document.getElementById('orderDetailStatus');
  statusElement.textContent = order.status;
  statusElement.className = 'status-badge ' + order.status;
  
  document.getElementById('orderDetailPayment').textContent = order.paymentMethod || 'N/A';
  document.getElementById('orderDetailAddress').textContent = order.deliveryAddress || 'N/A';
  document.getElementById('orderDetailTotal').textContent = '$' + parseFloat(order.totalAmount).toFixed(2);
  
  // Load order items
  const itemsList = document.getElementById('orderItemsList');
  itemsList.innerHTML = '<div class="loading-spinner">Loading items...</div>';
  
  fetch(`/201Project/OrderServlet?action=get&orderId=${orderId}`)
    .then(res => res.json())
    .then(data => {
      if (data.success && data.order && data.order.orderItems) {
        itemsList.innerHTML = data.order.orderItems.map(item => `
          <div class="order-item-card">
            <div class="item-info">
              <div class="item-name">${item.productName}</div>
              <div class="item-quantity">Quantity: ${item.quantity}</div>
            </div>
            <div class="item-pricing">
              <div class="item-unit-price">$${item.unitPrice.toFixed(2)} each</div>
              <div class="item-subtotal">$${item.subtotal.toFixed(2)}</div>
            </div>
          </div>
        `).join('');
      } else {
        itemsList.innerHTML = '<div class="error-message">Failed to load order items</div>';
      }
    })
    .catch(error => {
      console.error('Error loading order items:', error);
      itemsList.innerHTML = '<div class="error-message">Failed to load order items</div>';
    });
  
  document.getElementById('orderModal').classList.add('active');
}

function closeOrderModal() {
  document.getElementById('orderModal').classList.remove('active');
}

// ================================
// User Management
// ================================
let currentUsers = [];

function loadUsers() {
  fetch('/201Project/LoginServlet?action=listUsers')
    .then(res => res.json())
    .then(data => {
      currentUsers = data.users || [];
      document.getElementById('totalUsers').textContent = currentUsers.length;
      renderUsersTable(currentUsers);
    })
    .catch(error => {
      console.error('Error loading users:', error);
      document.getElementById('usersTableBody').innerHTML = 
        '<tr><td colspan="6" class="loading-cell">Failed to load users</td></tr>';
    });
}

function renderUsersTable(users) {
  const tbody = document.getElementById('usersTableBody');
  
  if (users.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="loading-cell">No users found</td></tr>';
    return;
  }
  
  tbody.innerHTML = users.map(user => `
    <tr>
      <td>${user.userId}</td>
      <td>${user.username}</td>
      <td>${user.email || 'N/A'}</td>
      <td>
        <span class="status-badge ${user.role === 'admin' ? 'admin-role' : 'user-role'}">
          ${user.role}
        </span>
      </td>
      <td>${user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}</td>
      <td>
        <div class="action-buttons">
          <button class="btn-action btn-view" onclick="viewUser(${user.userId})">View</button>
        </div>
      </td>
    </tr>
  `).join('');
}

// User Search Handler
document.getElementById('userSearch').addEventListener('input', (e) => {
  const searchTerm = e.target.value.toLowerCase();
  const filtered = currentUsers.filter(user => 
    user.username.toLowerCase().includes(searchTerm) ||
    (user.email && user.email.toLowerCase().includes(searchTerm))
  );
  renderUsersTable(filtered);
});

function viewUser(userId) {
  const user = currentUsers.find(u => u.userId === userId);
  if (!user) return;
  
  // Fill user details
  document.getElementById('userDetailId').textContent = user.userId;
  document.getElementById('userDetailUsername').textContent = user.username;
  document.getElementById('userDetailEmail').textContent = user.email || 'N/A';
  document.getElementById('userDetailFullName').textContent = user.fullName || 'N/A';
  document.getElementById('userDetailPhone').textContent = user.phone || 'N/A';
  
  // Set role with proper styling
  const roleElement = document.getElementById('userDetailRole');
  roleElement.textContent = user.role;
  roleElement.className = 'status-badge ' + (user.role === 'admin' ? 'admin-role' : 'user-role');
  
  document.getElementById('userDetailStatus').textContent = user.status || 'Active';
  document.getElementById('userDetailJoined').textContent = user.createdAt ? 
    new Date(user.createdAt).toLocaleDateString() : 'N/A';
  
  document.getElementById('userModal').classList.add('active');
}

function closeUserModal() {
  document.getElementById('userModal').classList.remove('active');
}

// ================================
// Toast Notification
// ================================
function showToast(message, type = 'success') {
  const toast = document.getElementById('toast');
  const toastMessage = toast.querySelector('.toast-message');
  
  toastMessage.textContent = message;
  toast.classList.remove('error', 'success');
  toast.classList.add(type);
  toast.classList.add('show');
  
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

// ================================
// Smooth Scroll Navigation
// ================================
document.querySelectorAll('.nav-link').forEach(link => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    const target = link.getAttribute('href').substring(1);
    
    // Update active state
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    link.classList.add('active');
    
    // Scroll to section
    if (target === 'dashboard') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else if (target === 'products') {
      document.getElementById('productManagement').scrollIntoView({ behavior: 'smooth' });
    } else if (target === 'orders') {
      document.getElementById('orderManagement').scrollIntoView({ behavior: 'smooth' });
    } else if (target === 'users') {
      document.getElementById('userManagement').scrollIntoView({ behavior: 'smooth' });
    }
  });
});

// ================================
// Modal Overlay Click Handlers
// ================================
document.addEventListener('DOMContentLoaded', () => {
  // Close modals when clicking overlay
  const overlays = document.querySelectorAll('.modal-overlay');
  overlays.forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        const modal = overlay.closest('.modal');
        if (modal) {
          modal.classList.remove('active');
        }
      }
    });
  });
});

// ================================
// Initialize Dashboard
// ================================
document.addEventListener('DOMContentLoaded', () => {
  checkAuthentication();
  loadStatistics();
  
  // Refresh data every 30 seconds
  setInterval(() => {
    loadStatistics();
  }, 30000);
});