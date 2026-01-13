/* ================================
   Product Menu Page JavaScript
   Handles product listing, filtering, search, and cart
   ================================ */

// Global variables
let allProducts = [];
let filteredProducts = [];
let currentCategory = 'all';
let currentProduct = null;

// ================================
// Page Initialization
// ================================

document.addEventListener('DOMContentLoaded', () => {
  console.log('Product page loaded');
  
  // Check user session
  checkUserSession();
  
  // Load products
  loadProducts();
  
  // Setup event listeners
  setupEventListeners();
  
  // Load cart count
  updateCartCount();
  
  // Check for category in URL
  const urlParams = new URLSearchParams(window.location.search);
  const categoryParam = urlParams.get('category');
  if (categoryParam) {
    currentCategory = categoryParam;
    // Will be applied after products load
  }
});

// ================================
// Setup Event Listeners
// ================================

function setupEventListeners() {
  // Search button
  const searchBtn = document.getElementById('searchBtn');
  const searchInput = document.getElementById('searchInput');
  
  searchBtn.addEventListener('click', performSearch);
  searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      performSearch();
    }
  });
  
  // Category filters
  const filterButtons = document.querySelectorAll('.filter-btn');
  filterButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      filterButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentCategory = btn.dataset.category;
      filterProducts();
    });
  });
  
  // Sort select
  const sortSelect = document.getElementById('sortSelect');
  sortSelect.addEventListener('change', () => {
    sortProducts(sortSelect.value);
  });
  
  // Modal add to cart button
  const modalAddToCart = document.getElementById('modalAddToCart');
  modalAddToCart.addEventListener('click', addToCartFromModal);
  
  // Close modal on escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeModal();
    }
  });
}

// ================================
// Load Products from Backend
// ================================

async function loadProducts() {
  const loadingState = document.getElementById('loadingState');
  const errorState = document.getElementById('errorState');
  const productsGrid = document.getElementById('productsGrid');
  
  try {
    loadingState.style.display = 'block';
    errorState.style.display = 'none';
    productsGrid.innerHTML = '';
    
    // Fetch products from backend
    const response = await fetch('/201Project/ProductServlet?action=list', {
      method: 'GET',
      credentials: 'include'
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch products');
    }
    
    const data = await response.json();
    
    if (data.success && data.products) {
      allProducts = data.products;
      // Process products to add convenience flags
      allProducts = data.products.map(p => ({
        ...p,
        isAvailable: p.status === 'available',
        isInStock: p.stock > 0,
        isLowStock: p.stock > 0 && p.stock <= 10
      }));
      filteredProducts = [...allProducts];
      
      // Apply category filter if set from URL
      if (currentCategory !== 'all') {
        const filterBtn = document.querySelector(`[data-category="${currentCategory}"]`);
        if (filterBtn) {
          document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
          filterBtn.classList.add('active');
          filterProducts();
        }
      }
      
      loadingState.style.display = 'none';
      displayProducts(filteredProducts);
    } else {
      throw new Error(data.message || 'Failed to load products');
    }
    
  } catch (error) {
    console.error('Error loading products:', error);
    
    // Use mock data for demo if backend fails
    console.log('Loading mock data for demo...');
    loadMockData();
  }
}

// ================================
// Mock Data for Demo
// ================================

function loadMockData() {
  const loadingState = document.getElementById('loadingState');
  const errorState = document.getElementById('errorState');
  
  // Mock product data for demonstration
  allProducts = [
    // Appetizers
    {
      productId: 1,
      productName: 'Classic Margherita Pizza',
      description: 'Fresh mozzarella, tomato sauce, and basil on a crispy thin crust',
      price: 12.99,
      stock: 50,
      category: 'appetizer',
      imageUrl: 'images/products/pizza1.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 2,
      productName: 'Pepperoni Pizza',
      description: 'Classic pepperoni with extra cheese and Italian spices',
      price: 14.99,
      stock: 45,
      category: 'appetizer',
      imageUrl: 'images/products/pizza2.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 3,
      productName: 'Caesar Salad',
      description: 'Romaine lettuce, parmesan cheese, croutons, and Caesar dressing',
      price: 8.99,
      stock: 50,
      category: 'appetizer',
      imageUrl: 'images/products/caesar.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 4,
      productName: 'Greek Salad',
      description: 'Fresh tomatoes, cucumber, feta cheese, olives, and olive oil',
      price: 9.99,
      stock: 45,
      category: 'appetizer',
      imageUrl: 'images/products/greek.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 5,
      productName: 'Buffalo Wings',
      description: 'Crispy chicken wings tossed in spicy buffalo sauce',
      price: 11.99,
      stock: 35,
      category: 'appetizer',
      imageUrl: 'images/products/wings.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    // Main Courses
    {
      productId: 6,
      productName: 'Classic Cheeseburger',
      description: 'Angus beef patty, cheddar cheese, lettuce, tomato, and special sauce',
      price: 10.99,
      stock: 60,
      category: 'main_course',
      imageUrl: 'images/products/burger1.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 7,
      productName: 'Bacon Deluxe Burger',
      description: 'Double beef patty, crispy bacon, swiss cheese, and caramelized onions',
      price: 13.99,
      stock: 35,
      category: 'main_course',
      imageUrl: 'images/products/burger2.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 8,
      productName: 'Veggie Burger',
      description: 'Plant-based patty, avocado, sprouts, and chipotle mayo',
      price: 11.99,
      stock: 25,
      category: 'main_course',
      imageUrl: 'images/products/burger3.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 9,
      productName: 'Tonkotsu Ramen',
      description: 'Rich pork bone broth, chashu pork, soft-boiled egg, and noodles',
      price: 13.99,
      stock: 40,
      category: 'main_course',
      imageUrl: 'images/products/ramen1.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 10,
      productName: 'Spicy Miso Ramen',
      description: 'Miso broth with chili oil, ground pork, and fresh vegetables',
      price: 14.99,
      stock: 30,
      category: 'main_course',
      imageUrl: 'images/products/ramen2.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 11,
      productName: 'Pad Thai',
      description: 'Stir-fried rice noodles with shrimp, peanuts, and tamarind sauce',
      price: 12.99,
      stock: 35,
      category: 'main_course',
      imageUrl: 'images/products/padthai.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 12,
      productName: 'Quinoa Power Bowl',
      description: 'Quinoa, roasted vegetables, chickpeas, and tahini dressing',
      price: 11.99,
      stock: 6,
      category: 'main_course',
      imageUrl: 'images/products/quinoa.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: true
    },
    {
      productId: 13,
      productName: 'Grilled Salmon',
      description: 'Fresh Atlantic salmon with lemon butter sauce and vegetables',
      price: 18.99,
      stock: 20,
      category: 'main_course',
      imageUrl: 'images/products/salmon.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    // Desserts
    {
      productId: 14,
      productName: 'Chocolate Lava Cake',
      description: 'Warm chocolate cake with a molten center, served with vanilla ice cream',
      price: 6.99,
      stock: 20,
      category: 'dessert',
      imageUrl: 'images/products/lava.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 15,
      productName: 'Tiramisu',
      description: 'Classic Italian dessert with coffee-soaked ladyfingers and mascarpone',
      price: 7.99,
      stock: 15,
      category: 'dessert',
      imageUrl: 'images/products/tiramisu.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 16,
      productName: 'New York Cheesecake',
      description: 'Creamy cheesecake with graham cracker crust and berry compote',
      price: 7.99,
      stock: 18,
      category: 'dessert',
      imageUrl: 'images/products/cheesecake.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 17,
      productName: 'Ice Cream Sundae',
      description: 'Three scoops of ice cream with chocolate sauce, whipped cream, and cherry',
      price: 4.99,
      stock: 50,
      category: 'dessert',
      imageUrl: 'images/products/sundae.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    // Beverages
    {
      productId: 18,
      productName: 'Fresh Lemonade',
      description: 'Freshly squeezed lemon juice with a hint of mint',
      price: 3.99,
      stock: 100,
      category: 'beverage',
      imageUrl: 'images/products/lemonade.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 19,
      productName: 'Iced Coffee',
      description: 'Cold brew coffee with your choice of milk',
      price: 4.99,
      stock: 80,
      category: 'beverage',
      imageUrl: 'images/products/coffee.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    },
    {
      productId: 20,
      productName: 'Mango Smoothie',
      description: 'Fresh mango blended with yogurt and honey',
      price: 5.99,
      stock: 0,
      category: 'beverage',
      imageUrl: 'images/products/smoothie.jpg',
      status: 'unavailable',
      isAvailable: false,
      isInStock: false,
      isLowStock: false
    },
    {
      productId: 21,
      productName: 'Green Tea',
      description: 'Premium Japanese green tea, served hot or iced',
      price: 3.49,
      stock: 120,
      category: 'beverage',
      imageUrl: 'images/products/greentea.jpg',
      status: 'available',
      isAvailable: true,
      isInStock: true,
      isLowStock: false
    }
  ];
  
  filteredProducts = [...allProducts];
  
  // Apply category filter if set from URL
  if (currentCategory !== 'all') {
    const filterBtn = document.querySelector(`[data-category="${currentCategory}"]`);
    if (filterBtn) {
      document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
      filterBtn.classList.add('active');
      filterProducts();
    }
  }
  
  loadingState.style.display = 'none';
  errorState.style.display = 'none';
  displayProducts(filteredProducts);
  
  console.log('Mock data loaded successfully:', allProducts.length, 'products');
}

// ================================
// Display Products
// ================================

function displayProducts(products) {
  const productsGrid = document.getElementById('productsGrid');
  const emptyState = document.getElementById('emptyState');
  
  if (!products || products.length === 0) {
    productsGrid.innerHTML = '';
    emptyState.style.display = 'block';
    return;
  }
  
  emptyState.style.display = 'none';
  productsGrid.innerHTML = '';
  
  products.forEach(product => {
    const card = createProductCard(product);
    productsGrid.appendChild(card);
  });
}

// ================================
// Create Product Card
// ================================

function createProductCard(product) {

  const card = document.createElement('div');
  card.className = 'product-card';
  
  if (!product.isAvailable || !product.isInStock) {
    card.classList.add('out-of-stock');
  }
  
  // Determine stock badge
  let stockBadgeHTML = '';
  if (!product.isAvailable) {
    stockBadgeHTML = '<span class="stock-badge out-of-stock">Unavailable</span>';
  } else if (!product.isInStock) {
    stockBadgeHTML = '<span class="stock-badge out-of-stock">Out of Stock</span>';
  } else if (product.isLowStock) {
    stockBadgeHTML = '<span class="stock-badge low-stock">Low Stock</span>';
  } else {
    stockBadgeHTML = '<span class="stock-badge in-stock">In Stock</span>';
  }
  
  // Get emoji based on category (fallback)
  const emoji = getCategoryEmoji(product.category);
  
  // Check if product has an image URL
  let imageHTML;
  if (product.imageUrl && product.imageUrl.trim() !== '') {
    // Use actual image
    imageHTML = `<img src="/201Project/${product.imageUrl}" alt="${escapeHtml(product.productName)}"                      onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">
                 <div style="font-size: 5rem; display: none;">${emoji}</div>`;
  } else {
    // Use emoji as fallback
    imageHTML = `<div style="font-size: 5rem;">${emoji}</div>`;
  }
  
  card.innerHTML = `
  <div class="product-image">
    ${imageHTML}
    ${stockBadgeHTML}
  </div>
  <div class="product-info">
    <div class="product-category">${escapeHtml(product.category)}</div>
    <h3 class="product-name">${escapeHtml(product.productName)}</h3>
    <p class="product-description">${escapeHtml(product.description || 'Delicious and fresh')}</p>
    <div class="product-footer">
      <span class="product-price">$${parseFloat(product.price).toFixed(2)}</span>
      <div class="product-actions">
        <button class="btn-quick-add" 
                onclick="event.stopPropagation(); quickAddToCart(${product.productId})"
                ${!product.isAvailable || !product.isInStock ? 'disabled' : ''}
                title="Quick add to cart">
          <span>🛒</span>
        </button>
        <button class="btn-view-details" 
                ${!product.isAvailable || !product.isInStock ? 'disabled' : ''}>
          ${product.isAvailable && product.isInStock ? 'View Details' : 'Unavailable'}
        </button>
      </div>
    </div>
  </div>
`;
  
  // Add click event to open modal
  if (product.isAvailable && product.isInStock) {
    card.addEventListener('click', () => {
      openProductModal(product);
    });
  }
  
  return card;
}

// ================================
// Product Modal
// ================================

function openProductModal(product) {
  currentProduct = product;
  const modal = document.getElementById('productModal');
  
  // Populate modal with product data
  const emoji = getCategoryEmoji(product.category);
  
  // Check if product has an image URL
  let imageHTML;
  if (product.imageUrl && product.imageUrl.trim() !== '') {
    // Use actual image
    imageHTML = `<img src="../${product.imageUrl}" alt="${escapeHtml(product.productName)}" 
                      style="width: 100%; height: 100%; object-fit: cover; border-radius: 16px;"
                      onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                 <div style="font-size: 10rem; display: none; width: 100%; height: 100%; align-items: center; justify-content: center;">${emoji}</div>`;
  } else {
    // Use emoji as fallback
    imageHTML = `<div style="font-size: 10rem;">${emoji}</div>`;
  }
  
  document.getElementById('modalImage').innerHTML = imageHTML;
  document.getElementById('modalTitle').textContent = product.productName;
  document.getElementById('modalCategory').textContent = product.category;
  document.getElementById('modalPrice').textContent = `$${parseFloat(product.price).toFixed(2)}`;
  document.getElementById('modalDescription').textContent = product.description || 'Delicious and freshly prepared with quality ingredients.';
  
  // Stock badge
  let stockBadgeHTML = '';
  if (product.isLowStock) {
    stockBadgeHTML = '<span class="stock-badge low-stock">Low Stock</span>';
  } else {
    stockBadgeHTML = '<span class="stock-badge in-stock">In Stock</span>';
  }
  document.getElementById('modalStockBadge').innerHTML = stockBadgeHTML;
  
  // Reset quantity
  document.getElementById('modalQuantity').value = 1;
  updateModalTotal();
  
  // Show modal
  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
}

function closeModal() {
  const modal = document.getElementById('productModal');
  modal.classList.remove('active');
  document.body.style.overflow = 'auto';
  currentProduct = null;
}

function increaseQuantity() {
  const input = document.getElementById('modalQuantity');
  let value = parseInt(input.value);
  if (value < currentProduct.stock && value < 99) {
    input.value = value + 1;
    updateModalTotal();
  }
}

function decreaseQuantity() {
  const input = document.getElementById('modalQuantity');
  let value = parseInt(input.value);
  if (value > 1) {
    input.value = value - 1;
    updateModalTotal();
  }
}

function updateModalTotal() {
  if (!currentProduct) return;
  
  const quantity = parseInt(document.getElementById('modalQuantity').value);
  const price = parseFloat(currentProduct.price);
  const total = quantity * price;
  
  document.getElementById('modalTotal').textContent = `$${total.toFixed(2)}`;
}

// ================================
// Add to Cart
// ================================

function addToCartFromModal() {
  if (!currentProduct) return;
  
  const quantity = parseInt(document.getElementById('modalQuantity').value);
  
  // Get cart from localStorage
  let cart = getCart();
  
  // Check if product already in cart
  const existingItem = cart.find(item => item.productId === currentProduct.productId);
  
  if (existingItem) {
    // Update quantity
    existingItem.quantity += quantity;
  } else {
    // Add new item
    cart.push({
      productId: currentProduct.productId,
      productName: currentProduct.productName,
      price: parseFloat(currentProduct.price),
      quantity: quantity,
      category: currentProduct.category,
      imageUrl: currentProduct.imageUrl
    });
  }
  
  // Save cart
  saveCart(cart);
  
  // Update cart count
  updateCartCount();
  
  // Show toast notification
  showToast(`Added ${currentProduct.productName} to cart!`);
  
  // Close modal
  setTimeout(() => {
    closeModal();
  }, 500);
}

// ================================
// Cart Management
// ================================

function getCart() {
  const cartData = localStorage.getItem('foodhub_cart');
  return cartData ? JSON.parse(cartData) : [];
}

function saveCart(cart) {
  localStorage.setItem('foodhub_cart', JSON.stringify(cart));
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
// Filter and Search
// ================================

function filterProducts() {
  if (currentCategory === 'all') {
    filteredProducts = [...allProducts];
  } else {
    filteredProducts = allProducts.filter(p => 
      p.category.toLowerCase() === currentCategory.toLowerCase()
    );
  }
  
  displayProducts(filteredProducts);
}

function performSearch() {
  const searchInput = document.getElementById('searchInput');
  const keyword = searchInput.value.trim();
  
  if (!keyword) {
    filteredProducts = [...allProducts];
    displayProducts(filteredProducts);
    return;
  }
  
  // Search in product name, description, and category
  filteredProducts = allProducts.filter(product => {
    const searchTerm = keyword.toLowerCase();
    return product.productName.toLowerCase().includes(searchTerm) ||
           (product.description && product.description.toLowerCase().includes(searchTerm)) ||
           product.category.toLowerCase().includes(searchTerm);
  });
  
  displayProducts(filteredProducts);
}

function sortProducts(sortType) {
  switch (sortType) {
    case 'price-low':
      filteredProducts.sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
      break;
    case 'price-high':
      filteredProducts.sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
      break;
    case 'name':
      filteredProducts.sort((a, b) => a.productName.localeCompare(b.productName));
      break;
    default:
      // Keep original order or re-apply filter
      filterProducts();
      return;
  }
  
  displayProducts(filteredProducts);
}

// ================================
// User Session Management
// ================================

async function checkUserSession() {
  try {
    const response = await fetch('/201Project/LoginServlet?action=checkSession', {
      method: 'GET',
      credentials: 'include'
    });
    
    const data = await response.json();
    
    if (data.success && data.user) {
      updateNavbarForLoggedInUser(data.user);
    } else {
      updateNavbarForGuest();
    }
  } catch (error) {
    console.error('Error checking session:', error);
    updateNavbarForGuest();
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

function updateNavbarForGuest() {
  // Nothing to do, button already there
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
  dropdown.style.background = 'white';
  dropdown.style.borderRadius = '12px';
  dropdown.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.15)';
  dropdown.style.padding = '0.5rem';
  dropdown.style.minWidth = '200px';
  dropdown.style.zIndex = '1001';
  
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
// Toast Notification
// ================================

function showToast(message) {
  const toast = document.getElementById('toast');
  const toastMessage = document.getElementById('toastMessage');
  
  toastMessage.textContent = message;
  toast.classList.add('show');
  
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
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

// ================================
// Navbar Scroll Effect
// ================================

window.addEventListener('scroll', () => {
  const navbar = document.getElementById('navbar');
  if (window.pageYOffset > 50) {
    navbar.classList.add('scrolled');
  } else {
    navbar.classList.remove('scrolled');
  }
});

// ================================
// Add styles for user dropdown
// ================================

const style = document.createElement('style');
style.textContent = `
  .user-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.5rem 1rem;
    background: var(--bg-light);
    border-radius: 25px;
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .user-info:hover {
    background: var(--bg-cream);
  }
  
  .user-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: var(--primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 0.9rem;
  }
  
  .user-name {
    font-weight: 600;
    color: var(--text-dark);
    font-size: 0.95rem;
  }
  
  .user-dropdown {
    animation: slideDown 0.3s ease;
  }
  
  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .dropdown-header {
    padding: 1rem;
    border-bottom: 1px solid #E5E5E5;
  }
  
  .dropdown-header strong {
    display: block;
    color: #1A1A1A;
    margin-bottom: 0.25rem;
  }
  
  .dropdown-header small {
    display: block;
    color: #7A7A7A;
    font-size: 0.875rem;
  }
  
  .dropdown-menu {
    list-style: none;
    padding: 0.5rem 0;
    margin: 0;
  }
  
  .dropdown-menu li {
    margin: 0;
  }
  
  .dropdown-menu a {
    display: block;
    padding: 0.75rem 1rem;
    color: #4A4A4A;
    text-decoration: none;
    border-radius: 8px;
    transition: all 0.2s ease;
    font-weight: 500;
  }
  
  .dropdown-menu a:hover {
    background: #F9F9F9;
    color: #FF6B35;
  }
`;
document.head.appendChild(style);

// ================================
// Quick Add to Cart Function
// ================================

/**
 * Quick add single item to cart directly from product card
 * @param {Number} productId - Product ID to add
 */
function quickAddToCart(productId) {
  // Find product from loaded products
  const product = allProducts.find(p => p.productId === productId);
  
  if (!product) {
    showToast('Product not found');
    return;
  }
  
  if (!product.isAvailable || !product.isInStock) {
    showToast('Sorry, this item is out of stock');
    return;
  }
  
  // Get cart from localStorage
  let cart = getCart();
  
  // Check if product already in cart
  const existingItem = cart.find(item => item.productId === product.productId);
  
  if (existingItem) {
    // Update quantity
    existingItem.quantity += 1;
  } else {
    // Add new item with quantity 1
    cart.push({
      productId: product.productId,
      productName: product.productName,
      price: parseFloat(product.price),
      quantity: 1,
      category: product.category,
      imageUrl: product.imageUrl
    });
  }
  
  // Save cart
  saveCart(cart);
  
  // Update cart count with animation
  updateCartCount();
  
  // Show toast notification
  showToast(`${product.productName} added to cart!`);
}

// Add animation to cart count update
function updateCartCount() {
  const cart = getCart();
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  
  const cartCount = document.getElementById('cartCount');
  if (cartCount) {
    cartCount.textContent = totalItems;
    
    // Add pop animation
    cartCount.style.animation = 'none';
    setTimeout(() => {
      cartCount.style.animation = 'cartPop 0.3s ease';
    }, 10);
  }
}

/* ================================
   Cart Sidebar Functions
   ================================ */

/**
 * Open cart sidebar panel
 */
function openCartSidebar() {
  const cartSidebar = document.getElementById('cartSidebar');
  if (cartSidebar) {
    cartSidebar.classList.add('active');
    document.body.style.overflow = 'hidden';
    renderCartItems();
  }
}

/**
 * Close cart sidebar panel
 */
function closeCartSidebar() {
  const cartSidebar = document.getElementById('cartSidebar');
  if (cartSidebar) {
    cartSidebar.classList.remove('active');
    document.body.style.overflow = 'auto';
  }
}

/**
 * Render all cart items in the sidebar
 */
function renderCartItems() {
  const cart = getCart();
  const cartContent = document.getElementById('cartContent');
  
  if (!cartContent) return;
  
  // If cart is empty
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
  
  // Render cart items
  let cartHTML = '<div class="cart-items">';
  
  cart.forEach(item => {
    const itemTotal = item.price * item.quantity;
    const emoji = getCategoryEmoji(item.category || 'other');
    
    cartHTML += `
      <div class="cart-item" data-product-id="${item.productId}">
        <div class="cart-item-image">
          <div class="cart-item-emoji">${emoji}</div>
        </div>
        
        <div class="cart-item-details">
          <h4 class="cart-item-name">${escapeHtml(item.productName)}</h4>
          <p class="cart-item-price">$${item.price.toFixed(2)} each</p>
          
          <div class="cart-item-controls">
            <div class="quantity-controls">
              <button class="qty-btn qty-decrease" onclick="updateCartItemQuantity(${item.productId}, -1)">
                <span>−</span>
              </button>
              <input type="number" 
                     class="qty-input" 
                     value="${item.quantity}" 
                     min="1" 
                     max="99"
                     readonly>
              <button class="qty-btn qty-increase" onclick="updateCartItemQuantity(${item.productId}, 1)">
                <span>+</span>
              </button>
            </div>
            
            <button class="btn-remove-item" onclick="removeCartItem(${item.productId})" title="Remove item">
              <span>🗑️</span>
            </button>
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
  
  // Update cart summary
  const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  updateCartSummary(total);
}

/**
 * Update cart summary totals
 * @param {Number} total - Total amount
 */
function updateCartSummary(total) {
  const subtotalEl = document.getElementById('cartSubtotal');
  const totalEl = document.getElementById('cartTotal');
  
  if (subtotalEl) subtotalEl.textContent = `$${total.toFixed(2)}`;
  if (totalEl) totalEl.textContent = `$${total.toFixed(2)}`;
}

/**
 * Update quantity of item in cart
 * @param {Number} productId - Product ID
 * @param {Number} change - Change amount (+1 or -1)
 */
function updateCartItemQuantity(productId, change) {
  let cart = getCart();
  const item = cart.find(i => i.productId === productId);
  
  if (!item) return;
  
  // Update quantity
  item.quantity += change;
  
  // Remove if quantity is 0 or less
  if (item.quantity <= 0) {
    cart = cart.filter(i => i.productId !== productId);
    showToast('Item removed from cart');
  } else if (item.quantity > 99) {
    item.quantity = 99;
    showToast('Maximum quantity reached');
  }
  
  // Save and refresh
  saveCart(cart);
  updateCartCount();
  renderCartItems();
}

/**
 * Remove item from cart
 * @param {Number} productId - Product ID to remove
 */
function removeCartItem(productId) {
  let cart = getCart();
  const item = cart.find(i => i.productId === productId);
  
  if (!item) return;
  
  // Filter out the item
  cart = cart.filter(i => i.productId !== productId);
  
  // Save and refresh
  saveCart(cart);
  updateCartCount();
  renderCartItems();
  
  showToast(`${item.productName} removed from cart`);
}

/**
 * Go to checkout page with login check
 */
async function goToCheckout() {
  const cart = getCart();
  
  if (cart.length === 0) {
    showToast('Your cart is empty!');
    return;
  }
  
  // Check if user is logged in
  try {
    const response = await fetch('/201Project/LoginServlet?action=checkSession', {
      method: 'GET',
      credentials: 'include'
    });
    
    const data = await response.json();
    
    if (data.success && data.user) {
      // User is logged in, proceed to checkout
      window.location.href = 'checkout.html';
    } else {
      // User not logged in, redirect to login with return URL
      showToast('Please login to proceed to checkout');
      setTimeout(() => {
        window.location.href = 'login.html?redirect=checkout.html';
      }, 1000);
    }
  } catch (error) {
    console.error('Error checking login status:', error);
    // If server check fails, redirect to login to be safe
    showToast('Please login to continue');
    setTimeout(() => {
      window.location.href = 'login.html?redirect=checkout.html';
    }, 1000);
  }
}

/**
 * Close cart sidebar when clicking escape key
 */
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    closeCartSidebar();
  }
});