/* ================================
   Admin Product Management Page JavaScript
   ================================ */

// Remove edit/delete product sections (cleanup)
(function () {
  const hs = Array.from(document.querySelectorAll('h2'));
  hs.forEach(h => {
    const t = h.textContent.trim();
    if (t.indexOf('Edit product') !== -1 || t.indexOf('Delete Product') !== -1) {
      const container = h.parentElement;
      if (container) container.remove();
    }
  });
})();

// ================================
// HELPER FUNCTIONS
// ================================

function showMessage(text) {
  const msg = document.getElementById('msg');
  msg.textContent = text;
  msg.classList.add('show');
  setTimeout(() => {
    msg.classList.remove('show');
  }, 3000);
}

// ================================
// USER AUTHENTICATION
// ================================

async function loadUser() {
  const res = await fetch('me');
  const data = await res.json();
  if (!data.loggedIn) {
    location.href = 'login.html';
    return;
  }
  document.getElementById('userinfo').textContent = `Welcome, ${data.username} (${data.role})`;
  if (data.role !== 'admin') {
    location.href = 'index.html';
    return;
  }
}

// ================================
// LOGOUT
// ================================

document.getElementById('logoutBtn').addEventListener('click', async () => {
  const res = await fetch('logout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
    body: new URLSearchParams()
  });
  const data = await res.json();
  if (data.success) location.href = 'login.html';
});

// ================================
// LOAD PRODUCTS
// ================================

async function loadProducts() {
  const res = await fetch('admin/products');
  const list = await res.json();
  const div = document.getElementById('products');
  div.innerHTML = '';
  
  if (list.length === 0) {
    div.innerHTML = '<p style="color: #999; text-align: center; padding: 40px;">No products yet. Add your first product below!</p>';
    return;
  }
  
  list.forEach(p => {
    const item = document.createElement('div');
    item.className = 'card';
    item.innerHTML = `
      <h3>${p.name}</h3>
      <p><strong>ID:</strong> ${p.id}</p>
      <p><strong>Price:</strong> ¥${parseFloat(p.price).toFixed(2)}</p>
      <p><strong>Stock:</strong> ${p.stock} units</p>
      <div class="actions">
        <button class="btn-update" data-id="${p.id}" data-name="${p.name}" data-price="${p.price}" data-stock="${p.stock}">Edit</button>
        <button class="btn-delete" data-id="${p.id}">Delete</button>
      </div>
    `;
    div.appendChild(item);
  });
}

// ================================
// ADD NEW PRODUCT
// ================================

document.getElementById('addForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const form = e.target;
  const params = new URLSearchParams();
  params.append('name', form.name.value);
  params.append('price', form.price.value);
  params.append('stock', form.stock.value);
  
  const res = await fetch('admin/products', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
    body: params
  });
  const data = await res.json();
  
  if (data.success) {
    showMessage('Product added successfully!');
    form.reset();
    loadProducts();
  } else {
    showMessage('Failed to add product');
  }
});

// ================================
// UPDATE & DELETE PRODUCTS
// ================================

document.getElementById('products').addEventListener('click', async (e) => {
  const t = e.target;
  
  // UPDATE PRODUCT
  if (t.classList.contains('btn-update')) {
    const id = t.dataset.id;
    const name = prompt('Product Name:', t.dataset.name);
    if (name === null) return;
    const price = prompt('Price (¥):', t.dataset.price);
    if (price === null) return;
    const stock = prompt('Stock Quantity:', t.dataset.stock);
    if (stock === null) return;
    
    const params = new URLSearchParams();
    params.append('productId', id);
    params.append('name', name);
    params.append('price', price);
    params.append('stock', stock);
    
    const res = await fetch('admin/product/update', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
      body: params
    });
    const data = await res.json();
    showMessage(data.success ? 'Product updated successfully!' : 'Update failed');
    if (data.success) loadProducts();
  }
  
  // DELETE PRODUCT
  if (t.classList.contains('btn-delete')) {
    const id = t.dataset.id;
    if (!confirm('Are you sure you want to delete this product (ID: ' + id + ')?')) return;
    
    const params = new URLSearchParams();
    params.append('productId', id);
    
    const res = await fetch('admin/product/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
      body: params
    });
    const data = await res.json();
    showMessage(data.success ? 'Product deleted successfully!' : 'Delete failed');
    if (data.success) loadProducts();
  }
});

// ================================
// INITIALIZE
// ================================

loadUser().then(loadProducts);
