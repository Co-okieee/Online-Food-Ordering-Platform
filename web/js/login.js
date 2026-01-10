/**
 * Login Page JavaScript - Form Validation
 * Description: Client-side form validation and error handling
 * 
 * Features:
 * - Real-time input validation
 * - Username format checking
 * - Password strength validation
 * - Error message display
 * - Form submission handling
 */

// ========================================
// Configuration Constants
// ========================================
const CONFIG = {
    // Validation rules
    MIN_USERNAME_LENGTH: 3,
    MAX_USERNAME_LENGTH: 20,
    MIN_PASSWORD_LENGTH: 6,
    MAX_PASSWORD_LENGTH: 50,
    
    // Timing
    VALIDATION_DELAY: 300, // ms - debounce delay for input validation
    
    // Session storage keys
    STORAGE_KEY: 'rememberedUsername'
};

// ========================================
// Error Messages
// ========================================
const MESSAGES = {
    // Username validation messages
    USERNAME_REQUIRED: 'Username is required',
    USERNAME_TOO_SHORT: `Username must be at least ${CONFIG.MIN_USERNAME_LENGTH} characters`,
    USERNAME_TOO_LONG: `Username cannot exceed ${CONFIG.MAX_USERNAME_LENGTH} characters`,
    USERNAME_INVALID: 'Username can only contain letters, numbers, and underscores',
    
    // Password validation messages
    PASSWORD_REQUIRED: 'Password is required',
    PASSWORD_TOO_SHORT: `Password must be at least ${CONFIG.MIN_PASSWORD_LENGTH} characters`,
    PASSWORD_TOO_LONG: `Password cannot exceed ${CONFIG.MAX_PASSWORD_LENGTH} characters`,
    
    // Form validation messages
    VALIDATION_FAILED: 'Please fix the errors above before submitting'
};

// ========================================
// DOM Element References
// ========================================
let elements = {};

// ========================================
// Initialize Application
// ========================================
document.addEventListener('DOMContentLoaded', () => {
    console.log('Login page initializing...');
    
    initializeElements();
    initializeEventListeners();
    loadRememberedUsername();
    
    console.log('Login page ready');
});

/**
 * Initialize all DOM element references
 * Stores references to frequently accessed elements for better performance
 */
function initializeElements() {
    elements = {
        // Form elements
        loginForm: document.getElementById('loginForm'),
        usernameInput: document.getElementById('username'),
        passwordInput: document.getElementById('password'),
        rememberMeCheckbox: document.getElementById('rememberMe'),
        userRoleInput: document.getElementById('userRole'),
        
        // Error display elements
        usernameError: document.getElementById('usernameError'),
        passwordError: document.getElementById('passwordError'),
        loginAlert: document.getElementById('loginAlert'),
        
        // Button elements
        loginBtn: document.getElementById('loginBtn'),
        roleButtons: document.querySelectorAll('.role-btn')
    };
}

/**
 * Initialize all event listeners
 * Sets up handlers for form submission, input validation, and user interactions
 */
function initializeEventListeners() {
    // Form submission
    elements.loginForm.addEventListener('submit', handleFormSubmit);
    
    // Real-time validation with debounce
    elements.usernameInput.addEventListener('input', 
        debounce(validateUsername, CONFIG.VALIDATION_DELAY)
    );
    elements.passwordInput.addEventListener('input', 
        debounce(validatePassword, CONFIG.VALIDATION_DELAY)
    );
    
    // Clear error messages on focus
    elements.usernameInput.addEventListener('focus', () => clearFieldError('username'));
    elements.passwordInput.addEventListener('focus', () => clearFieldError('password'));
    
    console.log('Event listeners initialized');
}

// ========================================
// Validation Functions
// ========================================

/**
 * Validate username input
 * Checks for: required, length, and character format
 * @returns {boolean} True if valid, false otherwise
 */
function validateUsername() {
    const username = elements.usernameInput.value.trim();
    
    // Check if empty
    if (username === '') {
        showFieldError('username', MESSAGES.USERNAME_REQUIRED);
        return false;
    }
    
    // Check minimum length
    if (username.length < CONFIG.MIN_USERNAME_LENGTH) {
        showFieldError('username', MESSAGES.USERNAME_TOO_SHORT);
        return false;
    }
    
    // Check maximum length
    if (username.length > CONFIG.MAX_USERNAME_LENGTH) {
        showFieldError('username', MESSAGES.USERNAME_TOO_LONG);
        return false;
    }
    
    // Check format: alphanumeric and underscore only
    const usernameRegex = /^[a-zA-Z0-9_]+$/;
    if (!usernameRegex.test(username)) {
        showFieldError('username', MESSAGES.USERNAME_INVALID);
        return false;
    }
    
    // Valid - clear error and mark as success
    clearFieldError('username');
    elements.usernameInput.classList.add('success');
    return true;
}

/**
 * Validate password input
 * Checks for: required and length constraints
 * @returns {boolean} True if valid, false otherwise
 */
function validatePassword() {
    const password = elements.passwordInput.value;
    
    // Check if empty
    if (password === '') {
        showFieldError('password', MESSAGES.PASSWORD_REQUIRED);
        return false;
    }
    
    // Check minimum length
    if (password.length < CONFIG.MIN_PASSWORD_LENGTH) {
        showFieldError('password', MESSAGES.PASSWORD_TOO_SHORT);
        return false;
    }
    
    // Check maximum length
    if (password.length > CONFIG.MAX_PASSWORD_LENGTH) {
        showFieldError('password', MESSAGES.PASSWORD_TOO_LONG);
        return false;
    }
    
    // Valid - clear error and mark as success
    clearFieldError('password');
    elements.passwordInput.classList.add('success');
    return true;
}

/**
 * Validate entire form
 * Runs all field validations and returns overall result
 * @returns {boolean} True if all fields are valid
 */
function validateForm() {
    const isUsernameValid = validateUsername();
    const isPasswordValid = validatePassword();
    
    return isUsernameValid && isPasswordValid;
}

// ========================================
// Error Display Functions
// ========================================

/**
 * Display error message for a specific field
 * @param {string} fieldName - Name of the field (username/password)
 * @param {string} message - Error message to display
 */
function showFieldError(fieldName, message) {
    const input = elements[`${fieldName}Input`];
    const errorElement = elements[`${fieldName}Error`];
    
    if (input && errorElement) {
        input.classList.add('error');
        input.classList.remove('success');
        errorElement.textContent = message;
    }
}

/**
 * Clear error message for a specific field
 * @param {string} fieldName - Name of the field (username/password)
 */
function clearFieldError(fieldName) {
    const input = elements[`${fieldName}Input`];
    const errorElement = elements[`${fieldName}Error`];
    
    if (input && errorElement) {
        input.classList.remove('error', 'success');
        errorElement.textContent = '';
    }
}

/**
 * Display alert message above form
 * @param {string} message - Message to display
 * @param {string} type - Alert type ('error' or 'success')
 */
function showAlert(message, type = 'error') {
    elements.loginAlert.className = `alert alert-${type}`;
    elements.loginAlert.querySelector('.alert-message').textContent = message;
    elements.loginAlert.style.display = 'block';
    
    console.log(`Alert [${type}]: ${message}`);
}

/**
 * Hide alert message
 */
function hideAlert() {
    elements.loginAlert.style.display = 'none';
}

// ========================================
// Form Submission Handler
// ========================================

/**
 * Handle form submission
 * Validates form and prepares data for submission
 * @param {Event} event - Form submit event
 */
function handleFormSubmit(event) {
    event.preventDefault();
    hideAlert();
    
    console.log('Form submission initiated');
    
    // Validate entire form
    if (!validateForm()) {
        console.log('Form validation failed');
        showAlert(MESSAGES.VALIDATION_FAILED, 'error');
        return;
    }
    
    // Get form data
    const formData = {
        username: elements.usernameInput.value.trim(),
        password: elements.passwordInput.value,
        userRole: elements.userRoleInput.value,
        rememberMe: elements.rememberMeCheckbox.checked
    };
    
    console.log('Form validation passed');
    console.log('Form data prepared:', {
        username: formData.username,
        userRole: formData.userRole,
        rememberMe: formData.rememberMe
    });
    
    // Note: Actual submission will be handled by backend
    // This prepares the data for AJAX submission (next commit)
    
    return false;
}

// ========================================
// Session Management
// ========================================

/**
 * Load remembered username from session storage
 * Automatically fills username if user previously checked "Remember me"
 */
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

/**
 * Save username to session storage if "Remember me" is checked
 * @param {string} username - Username to remember
 * @param {boolean} shouldRemember - Whether to save username
 */
function saveUsername(username, shouldRemember) {
    if (shouldRemember) {
        sessionStorage.setItem(CONFIG.STORAGE_KEY, username);
        console.log('Username saved to session storage');
    } else {
        sessionStorage.removeItem(CONFIG.STORAGE_KEY);
        console.log('Username removed from session storage');
    }
}

// ========================================
// Utility Functions
// ========================================

/**
 * Debounce function to limit rate of function calls
 * Prevents excessive validation calls during rapid typing
 * @param {Function} func - Function to debounce
 * @param {number} wait - Wait time in milliseconds
 * @returns {Function} Debounced function
 */
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

// ========================================
// Browser Auto-fill Detection
// ========================================

/**
 * Detect and validate browser auto-filled values
 * Runs validation after page load to handle auto-fill
 */
window.addEventListener('load', () => {
    setTimeout(() => {
        if (elements.usernameInput.value) {
            validateUsername();
        }
        if (elements.passwordInput.value) {
            validatePassword();
        }
    }, 100);
});

// ========================================
// Console Log Header
// ========================================
console.log('==========================================');
console.log('Login Page - Form Validation Module');
console.log('Author: Cookie');
console.log('==========================================');
console.log('Validation Rules:');
console.log(`- Username: ${CONFIG.MIN_USERNAME_LENGTH}-${CONFIG.MAX_USERNAME_LENGTH} chars, alphanumeric + underscore`);
console.log(`- Password: ${CONFIG.MIN_PASSWORD_LENGTH}-${CONFIG.MAX_PASSWORD_LENGTH} chars`);
console.log('==========================================');