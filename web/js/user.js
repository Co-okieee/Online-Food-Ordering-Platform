/* ================================
   User Pages JavaScript
   (Products List, Product Detail, Orders, Create Order)
   (Connected to Real Backend)
   ================================ */

// ================================
// COMMON FUNCTIONS
// ================================

// Logout function (used by all user pages)
if (document.getElementById('logoutBtn')) {
  document.getElementById('logoutBtn').addEventListener('click', async () => {
    try {
      const res = await fetch('logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: new URLSearchParams()
      });
      const data = await res.json();
      if (data.success) {
        location.href = 'login.html';
      }
    } catch (error) {
      console.error('Logout error:', error);
      location.href = 'login.html';
    }
  });
}

// Home button (used by some pages)
if (document.getElementById('homeBtn')) {
  document.getElementById('homeBtn').addEventListener('click', () => {
    location.href = 'index.html';
  });
}

// ================================
// PRODUCTS LIST PAGE
// ================================
if (document.getElementById('products') && document.querySelector('.grid')) {
  async function loadUser() {
    try {
      const res = await fetch('me');
      const data = await res.json();
      if (!data.loggedIn) {
        location.href = 'login.html';
        return;
      }
      document.getElementById('userinfo').textContent = `Welcome, ${data.username} (${data.role})`;
    } catch (error) {
      console.error('Load user error:', error);
      location.href = 'login.html';
    }
  }

  async function loadProducts() {
    try {
      // 调用后端API获取产品列表
      const res = await fetch('products');
      const list = await res.json();
      const div = document.getElementById('products');
      div.innerHTML = '';
      
      if (!list || list.length === 0) {
        div.innerHTML = '<div class="loading">No products available</div>';
        return;
      }
      
      list.forEach(p => {
        const item = document.createElement('div');
        item.className = 'card';
        const stockClass = p.stock < 20 ? 'stock low' : 'stock';
        
        item.innerHTML = `
          <h3>${p.name}</h3>
          <p class="price">¥${parseFloat(p.price).toFixed(2)}</p>
          <p class="${stockClass}">Stock: ${p.stock}</p>
          <a href="product.html?productId=${p.id}">View Details →</a>
        `;
        div.appendChild(item);
      });
    } catch (error) {
      console.error('Load products error:', error);
      const div = document.getElementById('products');
      div.innerHTML = '<div class="loading">Failed to load products</div>';
    }
  }

  loadUser().then(loadProducts);
}

// ================================
// PRODUCT DETAIL PAGE
// ================================
if (document.getElementById('pinfo') && document.getElementById('orderForm')) {
  const id = new URLSearchParams(location.search).get('productId');
  
  if (!id) {
    location.href = 'index.html';
  } else {
    document.getElementById('pid').value = id;

    async function loadProduct() {
      try {
        // 调用后端API获取产品详情
        const res = await fetch('products?productId=' + id);
        const p = await res.json();
        
        if (!p || !p.name) {
          document.getElementById('pname').textContent = 'Product not found';
          return;
        }
        
        document.getElementById('pname').textContent = p.name;
        
        const stockClass = p.stock < 20 ? 'stock-value low' : 'stock-value';
        document.getElementById('pinfo').innerHTML = `
          <p>
            <strong>Price:</strong>
            <span class="price-value">¥${parseFloat(p.price).toFixed(2)}</span>
          </p>
          <p>
            <strong>Available Stock:</strong>
            <span class="${stockClass}">${p.stock} units</span>
          </p>
        `;
      } catch (error) {
        console.error('Load product error:', error);
        document.getElementById('pname').textContent = 'Failed to load product';
      }
    }

    loadProduct();

    document.getElementById('orderForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const form = e.target;
      const quantity = parseInt(form.quantity.value);
      const msg = document.getElementById('msg');
      
      try {
        // 调用后端API创建订单
        const params = new URLSearchParams();
        params.append('productId', form.productId.value);
        params.append('quantity', quantity);
        
        const res = await fetch('order', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
          body: params
        });
        
        const data = await res.json();
        
        if (data.success) {
          msg.className = 'show success';
          msg.innerHTML = `
            <p style="font-weight: 600; font-size: 20px; color: #2e7d32;">Order Placed Successfully!</p>
            <div class="order-id">Order #${data.orderId}</div>
            <p style="margin: 10px 0;">
              Your order has been confirmed and will be processed shortly.
            </p>
            <a href="orders.html">View My Orders</a>
          `;
          form.quantity.value = 1;
        } else {
          msg.className = 'show error';
          msg.innerHTML = `
            <p style="font-weight: 600; color: #c62828;">Order Failed!</p>
            <p>${data.message || 'Please try again.'}</p>
          `;
        }
      } catch (error) {
        console.error('Order error:', error);
        msg.className = 'show error';
        msg.innerHTML = `
          <p style="font-weight: 600; color: #c62828;">Order Failed!</p>
          <p>An error occurred. Please try again.</p>
        `;
      }
      
      msg.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  }
}

// ================================
// ORDERS PAGE
// ================================
if (document.getElementById('orders')) {
  async function loadUser() {
    try {
      const res = await fetch('me');
      const data = await res.json();
      if (!data.loggedIn) {
        location.href = 'login.html';
        return;
      }
      document.getElementById('userinfo').textContent = `Welcome, ${data.username} (${data.role})`;
    } catch (error) {
      console.error('Load user error:', error);
      location.href = 'login.html';
    }
  }

  async function loadOrders() {
    try {
      // 调用后端API获取订单列表
      const res = await fetch('orders');
      const list = await res.json();
      const div = document.getElementById('orders');
      div.innerHTML = '';
      
      if (!Array.isArray(list) || list.length === 0) {
        div.innerHTML = `
          <div class="no-orders">
            <p>No orders yet</p>
          </div>
        `;
        return;
      }
      
      list.forEach(o => {
        const wrap = document.createElement('div');
        wrap.className = 'card';
        const items = o.items.map(it => 
          `${it.name} × ${it.quantity} <strong>(¥${(it.price * it.quantity).toFixed(2)})</strong>`
        ).join('<br>');
        
        const totalAmount = o.items.reduce((sum, it) => sum + (it.price * it.quantity), 0);
        
        wrap.innerHTML = `
          <h3>
            <span>Order #${o.orderId}</span>
            <span class="order-badge">¥${totalAmount.toFixed(2)}</span>
          </h3>
          <p class="timestamp"><strong>Order Date:</strong> ${o.createTime}</p>
          <div class="items-list">
            <p style="margin-bottom: 8px;"><strong>Items:</strong></p>
            <p style="margin-bottom: 0;">${items}</p>
          </div>
        `;
        div.appendChild(wrap);
      });
    } catch (error) {
      console.error('Load orders error:', error);
      const div = document.getElementById('orders');
      div.innerHTML = '<div class="no-orders"><p>Failed to load orders</p></div>';
    }
  }

  loadUser().then(loadOrders);
}

// ================================
// CREATE ORDER PAGE (如果使用cart.html)
// ================================
if (document.getElementById('orderForm') && document.getElementById('productPreview')) {
  const form = document.getElementById('orderForm');
  const productIdInput = form.productId;
  const quantityInput = form.quantity;
  const previewDiv = document.getElementById('productPreview');

  // Show product preview when product ID changes
  productIdInput.addEventListener('input', updatePreview);
  quantityInput.addEventListener('input', updatePreview);

  async function updatePreview() {
    const productId = parseInt(productIdInput.value);
    const quantity = parseInt(quantityInput.value) || 1;
    
    if (!productId) {
      previewDiv.className = 'product-preview';
      return;
    }
    
    try {
      // 调用后端API获取产品信息
      const res = await fetch('products?productId=' + productId);
      const product = await res.json();
      
      if (product && product.name) {
        const total = (product.price * quantity).toFixed(2);
        
        previewDiv.className = 'product-preview show';
        previewDiv.innerHTML = `
          <h3>Order Preview</h3>
          <p><strong>Product:</strong> ${product.name}</p>
          <p><strong>Unit Price:</strong> ¥${parseFloat(product.price).toFixed(2)}</p>
          <p><strong>Quantity:</strong> ${quantity}</p>
          <p><strong>Available Stock:</strong> ${product.stock} units</p>
          <p class="total">Total Amount: ¥${total}</p>
        `;
      } else {
        previewDiv.className = 'product-preview';
      }
    } catch (error) {
      console.error('Preview error:', error);
      previewDiv.className = 'product-preview';
    }
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const productId = parseInt(form.productId.value);
    const quantity = parseInt(form.quantity.value);
    const msgDiv = document.getElementById('msg');
    
    if (!productId) {
      msgDiv.className = 'show error';
      msgDiv.innerHTML = `
        <p style="font-weight: 600; color: #c62828;">Order Failed!</p>
        <p>Please enter a valid product ID.</p>
      `;
      return;
    }
    
    try {
      // 调用后端API创建订单
      const params = new URLSearchParams();
      params.append('productId', productId);
      params.append('quantity', quantity);
      
      const res = await fetch('order', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: params
      });
      
      const data = await res.json();
      
      if (data.success) {
        msgDiv.className = 'show success';
        msgDiv.innerHTML = `
          <p style="font-weight: 600; font-size: 20px; color: #2e7d32;">Order Placed Successfully!</p>
          <div class="order-id">Order #${data.orderId}</div>
          <p style="margin-top: 15px; color: #666;">Your order has been confirmed and will be processed shortly.</p>
        `;
        
        // Reset form and preview
        form.reset();
        previewDiv.className = 'product-preview';
      } else {
        msgDiv.className = 'show error';
        msgDiv.innerHTML = `
          <p style="font-weight: 600; color: #c62828;">Order Failed!</p>
          <p>${data.message || 'Please try again.'}</p>
        `;
      }
    } catch (error) {
      console.error('Order error:', error);
      msgDiv.className = 'show error';
      msgDiv.innerHTML = `
        <p style="font-weight: 600; color: #c62828;">Order Failed!</p>
        <p>An error occurred. Please try again.</p>
      `;
    }
    
    // Scroll to message
    msgDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
}
