/* ================================
   Contact Page JavaScript
   ================================ */

document.addEventListener('DOMContentLoaded', () => {
  console.log('Contact page loaded');
  checkUserSession();
  setupContactForm();
});

// ================================
// Contact Form Handler
// ================================

function setupContactForm() {
  const contactForm = document.getElementById('contactForm');
  
  if (contactForm) {
    contactForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      
      const formData = {
        name: document.getElementById('contactName').value,
        email: document.getElementById('contactEmail').value,
        phone: document.getElementById('contactPhone').value,
        subject: document.getElementById('contactSubject').value,
        message: document.getElementById('contactMessage').value
      };
      
      const submitBtn = contactForm.querySelector('.btn-submit');
      const originalText = submitBtn.innerHTML;
      submitBtn.disabled = true;
      submitBtn.innerHTML = '<span>Sending...</span>';
      
      // Simulate sending (replace with actual backend call when ready)
      setTimeout(() => {
        showToast('Message sent successfully! We\'ll get back to you soon.');
        contactForm.reset();
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
        
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
      } catch (error) {
        console.error('Contact form error:', error);
        showToast('Failed to send message. Please try again.');
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
      }
      */
    });
  }
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
    
    if (data.success && data.user) {
      updateNavbarForLoggedInUser(data.user);
    }
  } catch (error) {
    console.error('Error checking session:', error);
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
  dropdown.innerHTML = `
    <div class="dropdown-header">
      <strong>${user.fullName || user.username}</strong>
      <small>${user.email}</small>
    </div>
    <ul class="dropdown-menu">
      <li><a href="product.html">Menu</a></li>
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
  
  if (toast && toastMessage) {
    toastMessage.textContent = message;
    toast.classList.add('show');
    
    setTimeout(() => {
      toast.classList.remove('show');
    }, 3000);
  }
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