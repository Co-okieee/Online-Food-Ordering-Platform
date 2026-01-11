/* ================================
   Login & Register Pages JavaScript
   (Connected to Real Backend)
   ================================ */

// ================================
// LOGIN PAGE
// ================================
if (document.getElementById('loginForm')) {
  const form = document.getElementById('loginForm');
  
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = form.username.value;
    const password = form.password.value;
    const msgDiv = document.getElementById('loginMsg');
    
    try {
      // 调用后端登录API
      const params = new URLSearchParams();
      params.append('username', username);
      params.append('password', password);
      
      const res = await fetch('login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: params
      });
      
      const data = await res.json();
      
      if (data.success) {
        msgDiv.className = 'show success';
        msgDiv.textContent = 'Login successful! Redirecting...';
        
        // 根据角色跳转
        setTimeout(() => {
          if (data.role === 'admin') {
            location.href = 'admin.html';
          } else {
            location.href = 'index.html';
          }
        }, 1000);
      } else {
        msgDiv.className = 'show error';
        msgDiv.textContent = data.message || 'Invalid username or password';
      }
    } catch (error) {
      console.error('Login error:', error);
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Login failed. Please try again.';
    }
  });
}

// ================================
// REGISTER PAGE
// ================================
if (document.getElementById('regForm')) {
  document.getElementById('regForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const username = form.username.value;
    const password = form.password.value;
    const role = form.role.value;
    const msgDiv = document.getElementById('msg');
    
    // 前端验证
    if (username.length < 3) {
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Username must be at least 3 characters long';
      return;
    }
    
    if (password.length < 6) {
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Password must be at least 6 characters long';
      return;
    }
    
    try {
      // 调用后端注册API
      const params = new URLSearchParams();
      params.append('username', username);
      params.append('password', password);
      params.append('role', role);
      
      const res = await fetch('register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: params
      });
      
      const data = await res.json();
      
      if (data.success) {
        msgDiv.className = 'show success';
        msgDiv.textContent = 'Registration successful! Redirecting to login...';
        
        setTimeout(() => {
          location.href = 'login.html';
        }, 1500);
      } else {
        msgDiv.className = 'show error';
        msgDiv.textContent = data.message || 'Registration failed. Please try again.';
      }
    } catch (error) {
      console.error('Registration error:', error);
      msgDiv.className = 'show error';
      msgDiv.textContent = 'Registration failed. Please try again.';
    }
  });
}
