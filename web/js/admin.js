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

document.querySelector('.modal-overlay').addEventListener('click', () => {
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
  
  // Fill order details with styled HTML
  const orderInfoHTML = `
    <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 25px; border-radius: 12px; margin-bottom: 20px;">
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
        <div>
          <div style="opacity: 0.9; font-size: 13px; margin-bottom: 5px;">Order ID</div>
          <div style="font-size: 24px; font-weight: 700;">#${order.orderId}</div>
        </div>
        <div style="text-align: right;">
          <div style="opacity: 0.9; font-size: 13px; margin-bottom: 5px;">Total Amount</div>
          <div style="font-size: 24px; font-weight: 700;">$${parseFloat(order.totalAmount).toFixed(2)}</div>
        </div>
      </div>
    </div>
    
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 25px;">
      <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; border-left: 4px solid #667eea;">
        <div style="color: #666; font-size: 12px; text-transform: uppercase; margin-bottom: 5px;">Customer</div>
        <div style="font-size: 16px; font-weight: 600; color: #333;">${order.username || 'Guest'}</div>
      </div>
      
      <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; border-left: 4px solid #667eea;">
        <div style="color: #666; font-size: 12px; text-transform: uppercase; margin-bottom: 5px;">Order Date</div>
        <div style="font-size: 16px; font-weight: 600; color: #333;">${new Date(order.orderDate).toLocaleString()}</div>
      </div>
      
      <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; border-left: 4px solid #28a745;">
        <div style="color: #666; font-size: 12px; text-transform: uppercase; margin-bottom: 5px;">Status</div>
        <div style="display: inline-block; background: ${getStatusColor(order.status)}; color: white; padding: 5px 15px; border-radius: 20px; font-size: 14px; font-weight: 600;">${order.status}</div>
      </div>
      
      <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; border-left: 4px solid #ffc107;">
        <div style="color: #666; font-size: 12px; text-transform: uppercase; margin-bottom: 5px;">Payment Method</div>
        <div style="font-size: 16px; font-weight: 600; color: #333;">${order.paymentMethod}</div>
      </div>
    </div>
    
    <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 25px;">
      <div style="color: #666; font-size: 12px; text-transform: uppercase; margin-bottom: 8px;">Delivery Address</div>
      <div style="font-size: 15px; color: #333; line-height: 1.5;">📍 ${order.deliveryAddress || 'N/A'}</div>
    </div>
  `;
  
  document.getElementById('orderModalBody').innerHTML = orderInfoHTML;
  
  // Load order items with beautiful styling
  fetch(`/201Project/OrderServlet?action=get&orderId=${orderId}`)
    .then(res => res.json())
    .then(data => {
      if (data.success && data.order && data.order.orderItems) {
        const itemsHTML = `
          <div style="margin-top: 10px;">
            <h4 style="color: #333; margin-bottom: 15px; font-size: 18px; border-bottom: 2px solid #667eea; padding-bottom: 10px;">🍽️ Order Items</h4>
            <div style="background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
              ${data.order.orderItems.map((item, index) => `
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border-bottom: 1px solid #f0f0f0; ${index % 2 === 0 ? 'background: #fafafa;' : ''}">
                  <div style="flex: 2;">
                    <div style="font-weight: 600; color: #333; font-size: 15px;">${item.productName}</div>
                    <div style="color: #999; font-size: 13px; margin-top: 3px;">Unit Price: $${item.unitPrice.toFixed(2)}</div>
                  </div>
                  <div style="flex: 0.5; text-align: center;">
                    <span style="background: #667eea; color: white; padding: 4px 12px; border-radius: 12px; font-weight: 600; font-size: 14px;">×${item.quantity}</span>
                  </div>
                  <div style="flex: 1; text-align: right;">
                    <div style="font-weight: 700; color: #ff6b35; font-size: 18px;">$${item.subtotal.toFixed(2)}</div>
                  </div>
                </div>
              `).join('')}
            </div>
          </div>
        `;
        document.getElementById('orderModalBody').innerHTML += itemsHTML;
      }
    })
    .catch(error => {
      console.error('Error loading order items:', error);
    });
  
  document.getElementById('orderModal').classList.add('active');
}

// Helper function for status colors
function getStatusColor(status) {
  const colors = {
    'pending': '#ffc107',
    'confirmed': '#17a2b8',
    'preparing': '#ff6b35',
    'ready': '#28a745',
    'delivered': '#28a745',
    'cancelled': '#dc3545'
  };
  return colors[status] || '#6c757d';
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
        <span class="status-badge ${user.role === 'admin' ? 'unavailable' : 'available'}">
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
  
  // Create beautiful user details HTML
  const userDetailsHTML = `
    <div style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 30px; border-radius: 12px; margin-bottom: 25px; text-align: center;">
      <div style="width: 80px; height: 80px; background: white; border-radius: 50%; margin: 0 auto 15px; display: flex; align-items: center; justify-content: center; font-size: 36px; box-shadow: 0 4px 15px rgba(0,0,0,0.2);">
        ${user.role === 'admin' ? '👑' : '👤'}
      </div>
      <div style="font-size: 24px; font-weight: 700; margin-bottom: 5px;">${user.username}</div>
      <div style="opacity: 0.9; font-size: 14px;">${user.email || 'No email'}</div>
    </div>
    
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 20px;">
      <div style="background: #f8f9fa; padding: 18px; border-radius: 10px; border-left: 4px solid #f093fb;">
        <div style="color: #666; font-size: 12px; text-transform: uppercase; margin-bottom: 8px;">User ID</div>
        <div style="font-size: 18px; font-weight: 700; color: #333;">#${user.userId}</div>
      </div>
      
      <div style="background: #f8f9fa; padding: 18px; border-radius: 10px; border-left: 4px solid ${user.role === 'admin' ? '#dc3545' : '#28a745'};">
        <div style="color: #666; font-size: 12px; text-transform: uppercase; margin-bottom: 8px;">Role</div>
        <div style="display: inline-block; background: ${user.role === 'admin' ? '#dc3545' : '#28a745'}; color: white; padding: 6px 16px; border-radius: 20px; font-size: 14px; font-weight: 600; text-transform: capitalize;">
          ${user.role === 'admin' ? '🔑 Admin' : '👤 User'}
        </div>
      </div>
    </div>
    
    <div style="background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.08); margin-bottom: 15px;">
      <div style="border-bottom: 2px solid #f093fb; padding-bottom: 10px; margin-bottom: 20px;">
        <h4 style="color: #333; font-size: 16px; margin: 0;">📋 Personal Information</h4>
      </div>
      
      <div style="display: grid; gap: 15px;">
        <div style="display: flex; justify-content: space-between; padding: 12px; background: #fafafa; border-radius: 8px;">
          <span style="color: #666; font-weight: 500;">Full Name</span>
          <span style="color: #333; font-weight: 600;">${user.fullName || 'N/A'}</span>
        </div>
        
        <div style="display: flex; justify-content: space-between; padding: 12px; background: #fafafa; border-radius: 8px;">
          <span style="color: #666; font-weight: 500;">📧 Email</span>
          <span style="color: #333; font-weight: 600;">${user.email || 'N/A'}</span>
        </div>
        
        <div style="display: flex; justify-content: space-between; padding: 12px; background: #fafafa; border-radius: 8px;">
          <span style="color: #666; font-weight: 500;">📱 Phone</span>
          <span style="color: #333; font-weight: 600;">${user.phone || 'Not provided'}</span>
        </div>
        
        <div style="display: flex; justify-content: space-between; padding: 12px; background: #fafafa; border-radius: 8px;">
          <span style="color: #666; font-weight: 500;">Status</span>
          <span style="display: inline-block; background: ${user.status === 'active' ? '#28a745' : '#6c757d'}; color: white; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600; text-transform: capitalize;">
            ${user.status || 'active'}
          </span>
        </div>
        
        <div style="display: flex; justify-content: space-between; padding: 12px; background: #fafafa; border-radius: 8px;">
          <span style="color: #666; font-weight: 500;">📅 Joined</span>
          <span style="color: #333; font-weight: 600;">${user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}</span>
        </div>
      </div>
    </div>
    
    <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 15px; border-radius: 8px; text-align: center; color: white;">
      <div style="font-size: 13px; opacity: 0.9; margin-bottom: 5px;">Member since</div>
      <div style="font-size: 16px; font-weight: 600;">${user.createdAt ? new Date(user.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) : 'Unknown'}</div>
    </div>
  `;
  
  document.getElementById('userModal').querySelector('.modal-body').innerHTML = userDetailsHTML;
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
  toast.classList.remove('error');
  
  if (type === 'error') {
    toast.classList.add('error');
  }
  
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