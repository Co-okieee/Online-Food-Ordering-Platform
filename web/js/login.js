/* ================================
   Login & Register Pages JavaScript
   Connected to Backend LoginServlet
   ================================ */

// ================================
// LOGIN PAGE HANDLER
// ================================
if (document.getElementById('loginForm')) {
  const form = document.getElementById('loginForm');
  
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // Get form input values
    const username = form.username.value;
    const password = form.password.value;
    const role = form.querySelector('input[name="role"]:checked').value;
    const msgDiv = document.getElementById('loginMsg');
    
    try {
      // Prepare request parameters for backend
      const params = new URLSearchParams();
      params.append('action', 'login');
      params.append('username', username);
      params.append('password', password);
      params.append('role', role);
      
      // Send POST request to LoginServlet
      const res = await fetch('/201Project/LoginServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: params
      });
      
      // Parse JSON response from backend
      const data = await res.json();
      
      // Handle successful login
      if (data.success) {
        msgDiv.className = 'show success';
        msgDiv.textContent = 'Login successful! Redirecting...';
        
        // Redirect based on user role
        setTimeout(() => {
          if (data.user && data.user.role === 'admin') {
            location.href = 'admin.html';
          } else {
            location.href = 'index.html';
          }
        }, 1000);
      } else {
        // Display error message from backend
        msgDiv.className = 'show error';
        msgDiv.textContent = data.message || 'Invalid username or password';
      }
    } catch (error) {
      // Handle network or parsing errors
      console.error('Login error:', error);
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Login failed. Please try again.';
    }
  });
}

// ================================
// REGISTER PAGE HANDLER
// ================================
if (document.getElementById('regForm')) {
  document.getElementById('regForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    
    // Get form input values
    const username = form.username.value;
    const password = form.password.value;
    const email = form.email.value;
    const fullName = form.fullName.value.trim() || username; // Use username if fullName is empty
    const phone = form.phone ? form.phone.value : '';
    const role = form.querySelector('input[name="role"]:checked').value;
    const msgDiv = document.getElementById('msg');
    
    // Frontend validation - username length
    if (username.length < 3) {
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Username must be at least 3 characters long';
      return;
    }
    
    // Frontend validation - password length
    if (password.length < 6) {
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Password must be at least 6 characters long';
      return;
    }
    
    // Frontend validation - email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Please enter a valid email address';
      return;
    }
    
    // Full name validation removed - now optional
    // If empty, we use username as fullName
    
    try {
      // Prepare request parameters for backend
      const params = new URLSearchParams();
      params.append('action', 'register');
      params.append('username', username);
      params.append('password', password);
      params.append('email', email);
      params.append('fullName', fullName); // Will be username if not provided
      params.append('phone', phone);
      params.append('role', role);
      
      // Send POST request to LoginServlet
      const res = await fetch('/201Project/LoginServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: params
      });
      
      // Check if HTTP request was successful
      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }
      
      // Parse JSON response from backend
      const data = await res.json();
      
      // Handle successful registration
      if (data.success) {
        msgDiv.className = 'show success';
        msgDiv.textContent = 'Registration successful! Redirecting to login...';
        
        // Redirect to login page after 1.5 seconds
        setTimeout(() => {
          location.href = 'login.html';
        }, 1500);
      } else {
        // Display error message from backend
        msgDiv.className = 'show error';
        msgDiv.textContent = data.message || 'Registration failed. Please try again.';
      }
    } catch (error) {
      // Handle network or parsing errors
      console.error('Registration error:', error);
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Registration failed. Error: ' + error.message;
    }
  });
}