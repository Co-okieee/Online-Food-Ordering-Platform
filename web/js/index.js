/* ================================
   FoodHub Homepage JavaScript
   Handles session management, cart, and interactions
   ================================ */

// ================================
// Session Management
// ================================

/**
 * Check if user is logged in
 * Called when page loads
 */
async function checkUserSession() {
  try {
    const response = await fetch('/201Project/LoginServlet?action=checkSession', {
      method: 'GET',
      credentials: 'include'
    });
    
    const data = await response.json();
    
    if (data.success && data.user) {
      // User is logged in
      updateNavbarForLoggedInUser(data.user);
      loadCartCount();
    } else {
      // User is not logged in
      updateNavbarForGuest();
    }
  } catch (error) {
    console.error('Error checking session:', error);
    updateNavbarForGuest();
  }
}

// Update cart count on index page load
document.addEventListener('DOMContentLoaded', () => {
  updateCartCountFromStorage();
});

function updateCartCountFromStorage() {
  const cart = getCartFromStorage();
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  
  const cartCount = document.getElementById('cartCount');
  if (cartCount) {
    cartCount.textContent = totalItems;
  }
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
 * Update navbar for logged-in user
 */
function updateNavbarForLoggedInUser(user) {
  const userSection = document.getElementById('userSection');
  if (!userSection) return;
  
  const cartBtn = userSection.querySelector('.cart-btn');
  const loginBtn = userSection.querySelector('.btn-login');
  
  // Remove login button
  if (loginBtn) {
    loginBtn.remove();
  }
  
  // Add user info
  const userInfo = document.createElement('div');
  userInfo.className = 'user-info';
  userInfo.innerHTML = `
    <div class="user-avatar">${user.username.charAt(0).toUpperCase()}</div>
    <span class="user-name">${user.username}</span>
  `;
  
  // Add dropdown menu on click
  userInfo.addEventListener('click', () => {
    showUserMenu(user);
  });
  
  userSection.appendChild(userInfo);
}

/**
 * Update navbar for guest user
 */
function updateNavbarForGuest() {
  const userSection = document.getElementById('userSection');
  if (!userSection) return;
  
  // Keep cart button but reset count
  const cartCount = document.getElementById('cartCount');
  if (cartCount) {
    cartCount.textContent = '0';
  }
}

/**
 * Show user dropdown menu
 */
function showUserMenu(user) {
  // Remove existing menu if any
  const existingMenu = document.querySelector('.user-dropdown');
  if (existingMenu) {
    existingMenu.remove();
    return;
  }
  
  // Create dropdown menu
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
  
  // Position dropdown
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
  
  // Handle logout
  const logoutLink = dropdown.querySelector('#logoutLink');
  logoutLink.addEventListener('click', (e) => {
    e.preventDefault();
    handleLogout();
  });
  
  // Close on outside click
  setTimeout(() => {
    document.addEventListener('click', function closeDropdown(e) {
      if (!dropdown.contains(e.target) && !userInfo.contains(e.target)) {
        dropdown.remove();
        document.removeEventListener('click', closeDropdown);
      }
    });
  }, 100);
}

/**
 * Handle user logout
 */
async function handleLogout() {
  try {
    await fetch('/201Project/LoginServlet?action=logout', {
      method: 'GET',
      credentials: 'include'
    });
    
    // Redirect to login page
    window.location.href = 'login.html';
  } catch (error) {
    console.error('Logout error:', error);
    // Redirect anyway
    window.location.href = 'login.html';
  }
}

// ================================
// Cart Management
// ================================

/**
 * Load cart item count
 */
function loadCartCount() {
  // Get cart from localStorage (or you can fetch from server)
  const cart = getCartFromStorage();
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  
  const cartCount = document.getElementById('cartCount');
  if (cartCount) {
    cartCount.textContent = totalItems;
    
    // Add animation when count changes
    cartCount.style.animation = 'none';
    setTimeout(() => {
      cartCount.style.animation = 'pop 0.3s ease';
    }, 10);
  }
}

/**
 * Get cart from localStorage
 */
function getCartFromStorage() {
  const cartData = localStorage.getItem('foodhub_cart');
  return cartData ? JSON.parse(cartData) : [];
}

// ================================
// Navbar Scroll Effect
// ================================

let lastScrollTop = 0;
const navbar = document.getElementById('navbar');

window.addEventListener('scroll', () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  
  // Add shadow on scroll
  if (scrollTop > 50) {
    navbar.classList.add('scrolled');
  } else {
    navbar.classList.remove('scrolled');
  }
  
  lastScrollTop = scrollTop;
});

// ================================
// Mobile Menu Toggle
// ================================

const mobileMenuToggle = document.getElementById('mobileMenuToggle');
const navCenter = document.querySelector('.nav-center');

if (mobileMenuToggle && navCenter) {
  mobileMenuToggle.addEventListener('click', () => {
    navCenter.classList.toggle('mobile-active');
    mobileMenuToggle.classList.toggle('active');
  });
}

// ================================
// Smooth Scroll for Anchor Links
// ================================

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
  anchor.addEventListener('click', function(e) {
    const href = this.getAttribute('href');
    
    // Skip if href is just "#"
    if (href === '#') {
      e.preventDefault();
      return;
    }
    
    const target = document.querySelector(href);
    if (target) {
      e.preventDefault();
      const navHeight = document.querySelector('.navbar').offsetHeight;
      const targetPosition = target.offsetTop - navHeight;
      
      window.scrollTo({
        top: targetPosition,
        behavior: 'smooth'
      });
    }
  });
});

// ================================
// Animation on Scroll
// ================================

const observerOptions = {
  threshold: 0.2,
  rootMargin: '0px 0px -100px 0px'
};

const observer = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.style.opacity = '1';
      entry.target.style.transform = 'translateY(0)';
    }
  });
}, observerOptions);

// Observe elements for animation
document.addEventListener('DOMContentLoaded', () => {
  const animateElements = document.querySelectorAll('.feature-card, .category-card, .benefit-item');
  
  animateElements.forEach(el => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(30px)';
    el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    observer.observe(el);
  });
});

// ================================
// Category Click Tracking
// ================================

document.querySelectorAll('.category-card').forEach(card => {
  card.addEventListener('click', (e) => {
    const categoryName = card.querySelector('.category-name').textContent;
    console.log(`Category clicked: ${categoryName}`);
    // You can add analytics tracking here
  });
});

// ================================
// Initialize on Page Load
// ================================

document.addEventListener('DOMContentLoaded', () => {
  console.log('FoodHub homepage loaded');
  checkUserSession();
  
  // Add animation classes
  const heroText = document.querySelector('.hero-text');
  if (heroText) {
    heroText.style.opacity = '0';
    setTimeout(() => {
      heroText.style.transition = 'opacity 0.8s ease';
      heroText.style.opacity = '1';
    }, 100);
  }
});

// ================================
// Add pop animation for cart count
// ================================

const style = document.createElement('style');
style.textContent = `
  @keyframes pop {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.3);
    }
    100% {
      transform: scale(1);
    }
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
  
  .nav-center.mobile-active {
    display: flex;
    position: fixed;
    top: var(--nav-height);
    left: 0;
    right: 0;
    background: white;
    padding: 2rem;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    z-index: 999;
  }
  
  .nav-center.mobile-active .nav-menu {
    flex-direction: column;
    width: 100%;
  }
  
  .mobile-menu-toggle.active span:nth-child(1) {
    transform: rotate(45deg) translate(6px, 6px);
  }
  
  .mobile-menu-toggle.active span:nth-child(2) {
    opacity: 0;
  }
  
  .mobile-menu-toggle.active span:nth-child(3) {
    transform: rotate(-45deg) translate(6px, -6px);
  }
`;
document.head.appendChild(style);

/* ================================
   Contact Form Handler
   ================================ */

const contactForm = document.getElementById('contactForm');

if (contactForm) {
  contactForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = {
      name: document.getElementById('contactName').value,
      email: document.getElementById('contactEmail').value,
      subject: document.getElementById('contactSubject').value,
      message: document.getElementById('contactMessage').value
    };
    
    // Show loading state
    const submitBtn = contactForm.querySelector('.btn-contact-submit');
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span>Sending...</span>';
    
    // Simulate sending (replace with actual API call)
    setTimeout(() => {
      // Show success message
      showToast('Message sent successfully! We\'ll get back to you soon.');
      
      // Reset form
      contactForm.reset();
      
      // Restore button
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
    }, 1500);
    
    // TODO: Implement actual backend call
    /*
    try {
      const response = await fetch('/201Project/ContactServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      
      const data = await response.json();
      
      if (data.success) {
        showToast('Message sent successfully!');
        contactForm.reset();
      } else {
        showToast('Failed to send message. Please try again.');
      }
    } catch (error) {
      console.error('Contact form error:', error);
      showToast('Failed to send message. Please try again.');
    }
    */
  });
}

/* ================================
   Toast Helper Function (if not already present)
   ================================ */

function showToast(message) {
  // Create toast if it doesn't exist
  let toast = document.getElementById('toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'toast';
    toast.className = 'toast';
    toast.innerHTML = `
      <span class="toast-icon">✓</span>
      <span class="toast-message" id="toastMessage"></span>
    `;
    document.body.appendChild(toast);
  }
  
  const toastMessage = document.getElementById('toastMessage');
  toastMessage.textContent = message;
  toast.classList.add('show');
  
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}