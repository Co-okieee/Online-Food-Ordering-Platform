cat > /home/claude/commit_10_login_ajax.js << 'JSEOF'
/**
 * Login Page JavaScript - Complete with AJAX Submission
 * Description: Full login functionality with backend integration
 * 
 * Complete Features:
 * - Form validation
 * - Password toggle
 * - Role switching
 * - AJAX form submission
 * - Loading states
 * - Error handling
 * - Success handling and redirection
 */

// ========================================
// Configuration
// ========================================
const CONFIG = {
    API_ENDPOINT: '/login',
    MIN_USERNAME_LENGTH: 3,
    MAX_USERNAME_LENGTH: 20,
    MIN_PASSWORD_LENGTH: 6,
    MAX_PASSWORD_LENGTH: 50,
    VALIDATION_DELAY: 300,
    REDIRECT_DELAY: 1500,
    STORAGE_KEY: 'rememberedUsername'
};

const MESSAGES = {
    USERNAME_REQUIRED: 'Username is required',
    USERNAME_TOO_SHORT: `Username must be at least ${CONFIG.MIN_USERNAME_LENGTH} characters`,
    USERNAME_TOO_LONG: `Username cannot exceed ${CONFIG.MAX_USERNAME_LENGTH} characters`,
    USERNAME_INVALID: 'Username can only contain letters, numbers, and underscores',
    PASSWORD_REQUIRED: 'Password is required',
    PASSWORD_TOO_SHORT: `Password must be at least ${CONFIG.MIN_PASSWORD_LENGTH} characters`,
    PASSWORD_TOO_LONG: `Password cannot exceed ${CONFIG.MAX_PASSWORD_LENGTH} characters`,
    VALIDATION_FAILED: 'Please fix the errors above before submitting',
    LOGIN_FAILED: 'Invalid username or password',
    NETWORK_ERROR: 'Network error. Please check your connection',
    SERVER_ERROR: 'Server error. Please try again later',
    LOGIN_SUCCESS: 'Login successful! Redirecting...'
};

let elements = {};

// ========================================
// Initialize
// ========================================
document.addEventListener('DOMContentLoaded', () => {
    console.log('Login page initializing...');
    initializeElements();
    initializeEventListeners();
    loadRememberedUsername();
    console.log('Login page ready');
});

function initializeElements() {
    elements = {
        loginForm: document.getElementById('loginForm'),
        usernameInput: document.getElementById('username'),
        passwordInput: document.getElementById('password'),
        rememberMeCheckbox: document.getElementById('rememberMe'),
        userRoleInput: document.getElementById('userRole'),
        usernameError: document.getElementById('usernameError'),
        passwordError: document.getElementById('passwordError'),
        loginAlert: document.getElementById('loginAlert'),
        loginBtn: document.getElementById('loginBtn'),
        togglePasswordBtn: document.getElementById('togglePassword'),
        roleButtons: document.querySelectorAll('.role-btn'),
        loadingOverlay: document.getElementById('loadingOverlay')
    };
}

function initializeEventListeners() {
    elements.loginForm.addEventListener('submit', handleFormSubmit);
    elements.usernameInput.addEventListener('input', debounce(validateUsername, CONFIG.VALIDATION_DELAY));
    elements.passwordInput.addEventListener('input', debounce(validatePassword, CONFIG.VALIDATION_DELAY));
    elements.usernameInput.addEventListener('focus', () => clearFieldError('username'));
    elements.passwordInput.addEventListener('focus', () => clearFieldError('password'));
    elements.togglePasswordBtn.addEventListener('click', togglePasswordVisibility);
    elements.roleButtons.forEach(btn => btn.addEventListener('click', handleRoleToggle));
    console.log('Event listeners initialized');
}

// ========================================
// Validation
// ========================================
function validateUsername() {
    const username = elements.usernameInput.value.trim();
    if (username === '') {
        showFieldError('username', MESSAGES.USERNAME_REQUIRED);
        return false;
    }
    if (username.length < CONFIG.MIN_USERNAME_LENGTH) {
        showFieldError('username', MESSAGES.USERNAME_TOO_SHORT);
        return false;
    }
    if (username.length > CONFIG.MAX_USERNAME_LENGTH) {
        showFieldError('username', MESSAGES.USERNAME_TOO_LONG);
        return false;
    }
    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
        showFieldError('username', MESSAGES.USERNAME_INVALID);
        return false;
    }
    clearFieldError('username');
    elements.usernameInput.classList.add('success');
    return true;
}

function validatePassword() {
    const password = elements.passwordInput.value;
    if (password === '') {
        showFieldError('password', MESSAGES.PASSWORD_REQUIRED);
        return false;
    }
    if (password.length < CONFIG.MIN_PASSWORD_LENGTH) {
        showFieldError('password', MESSAGES.PASSWORD_TOO_SHORT);
        return false;
    }
    if (password.length > CONFIG.MAX_PASSWORD_LENGTH) {
        showFieldError('password', MESSAGES.PASSWORD_TOO_LONG);
        return false;
    }
    clearFieldError('password');
    elements.passwordInput.classList.add('success');
    return true;
}

function validateForm() {
    return validateUsername() && validatePassword();
}

function showFieldError(fieldName, message) {
    const input = elements[`${fieldName}Input`];
    const errorElement = elements[`${fieldName}Error`];
    if (input && errorElement) {
        input.classList.add('error');
        input.classList.remove('success');
        errorElement.textContent = message;
    }
}

function clearFieldError(fieldName) {
    const input = elements[`${fieldName}Input`];
    const errorElement = elements[`${fieldName}Error`];
    if (input && errorElement) {
        input.classList.remove('error', 'success');
        errorElement.textContent = '';
    }
}

function showAlert(message, type = 'error') {
    elements.loginAlert.className = `alert alert-${type}`;
    elements.loginAlert.querySelector('.alert-message').textContent = message;
    elements.loginAlert.style.display = 'block';
    console.log(`Alert [${type}]: ${message}`);
}

function hideAlert() {
    elements.loginAlert.style.display = 'none';
}

// ========================================
// Form Submission with AJAX
// ========================================
async function handleFormSubmit(event) {
    event.preventDefault();
    hideAlert();
    console.log('Form submission initiated');
    
    if (!validateForm()) {
        console.log('Form validation failed');
        showAlert(MESSAGES.VALIDATION_FAILED, 'error');
        return;
    }
    
    const formData = {
        username: elements.usernameInput.value.trim(),
        password: elements.passwordInput.value,
        userRole: elements.userRoleInput.value,
        rememberMe: elements.rememberMeCheckbox.checked
    };
    
    console.log('Form validation passed, submitting to backend');
    setLoadingState(true);
    
    try {
        const response = await submitLogin(formData);
        if (response.success) {
            handleLoginSuccess(formData);
        } else {
            handleLoginFailure(response.message || MESSAGES.LOGIN_FAILED);
        }
    } catch (error) {
        console.error('Login error:', error);
        handleLoginError(error);
    } finally {
        setLoadingState(false);
    }
}

async function submitLogin(formData) {
    console.log('Sending AJAX request to:', CONFIG.API_ENDPOINT);
    
    const response = await fetch(CONFIG.API_ENDPOINT, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams(formData)
    });
    
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return await response.json();
    }
    
    if (response.redirected || response.status === 200) {
        return { success: true, redirectUrl: response.url };
    }
    
    return { success: false };
}

function handleLoginSuccess(formData) {
    console.log('Login successful!');
    
    if (formData.rememberMe) {
        sessionStorage.setItem(CONFIG.STORAGE_KEY, formData.username);
    } else {
        sessionStorage.removeItem(CONFIG.STORAGE_KEY);
    }
    
    showAlert(MESSAGES.LOGIN_SUCCESS, 'success');
    
    setTimeout(() => {
        const redirectUrl = formData.userRole === 'admin' 
            ? '/pages/admin.html' 
            : '/pages/index.html';
        console.log('Redirecting to:', redirectUrl);
        window.location.href = redirectUrl;
    }, CONFIG.REDIRECT_DELAY);
}

function handleLoginFailure(message) {
    console.log('Login failed:', message);
    showAlert(message, 'error');
    shakeForm();
}

function handleLoginError(error) {
    console.error('Network/Server error:', error);
    let message = MESSAGES.NETWORK_ERROR;
    if (error.message.includes('HTTP error')) {
        message = MESSAGES.SERVER_ERROR;
    }
    showAlert(message, 'error');
    shakeForm();
}

function setLoadingState(isLoading) {
    if (isLoading) {
        elements.loginBtn.classList.add('loading');
        elements.loginBtn.disabled = true;
        if (elements.loadingOverlay) {
            elements.loadingOverlay.style.display = 'flex';
        }
    } else {
        elements.loginBtn.classList.remove('loading');
        elements.loginBtn.disabled = false;
        if (elements.loadingOverlay) {
            elements.loadingOverlay.style.display = 'none';
        }
    }
}

function shakeForm() {
    elements.loginForm.style.animation = 'none';
    setTimeout(() => {
        elements.loginForm.style.animation = 'shake 0.5s';
    }, 10);
}

// ========================================
// Interactive Features
// ========================================
function togglePasswordVisibility() {
    const isPassword = elements.passwordInput.type === 'password';
    elements.passwordInput.type = isPassword ? 'text' : 'password';
    elements.togglePasswordBtn.textContent = isPassword ? 'Hide' : 'Show';
    console.log(`Password ${isPassword ? 'visible' : 'hidden'}`);
}

function handleRoleToggle(event) {
    const clickedBtn = event.currentTarget;
    const role = clickedBtn.dataset.role;
    
    elements.roleButtons.forEach(btn => btn.classList.remove('active'));
    clickedBtn.classList.add('active');
    elements.userRoleInput.value = role;
    
    const formTitle = document.querySelector('h2');
    if (formTitle) {
        formTitle.textContent = role === 'admin' ? 'Admin Portal' : 'Welcome Back';
    }
    
    console.log(`Role changed to: ${role}`);
}

// ========================================
// Session Management
// ========================================
function loadRememberedUsername() {
    const rememberedUsername = sessionStorage.getItem(CONFIG.STORAGE_KEY);
    if (rememberedUsername) {
        elements.usernameInput.value = rememberedUsername;
        elements.rememberMeCheckbox.checked = true;
        elements.passwordInput.focus();
        console.log('Loaded remembered username');
    } else {
        elements.usernameInput.focus();
    }
}

// ========================================
// Utility
// ========================================
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

window.addEventListener('load', () => {
    setTimeout(() => {
        if (elements.usernameInput.value) validateUsername();
        if (elements.passwordInput.value) validatePassword();
    }, 100);
});

// ========================================
// Keyboard Shortcuts
// ========================================
document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') hideAlert();
    if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
        elements.loginForm.dispatchEvent(new Event('submit'));
    }
});

console.log('==========================================');
console.log('Login Page - Complete with AJAX');
console.log('Author: Cookie');
console.log('Version: Final');
console.log('==========================================');
JSEOF