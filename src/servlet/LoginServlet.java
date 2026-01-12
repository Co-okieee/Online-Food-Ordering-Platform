package servlet;

import model.User;
import service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * LoginServlet - Handle login and registration requests from frontend
 * 
 * Features:
 * - User authentication (login)
 * - User registration
 * - Session management
 * - Logout functionality
 * - JSON response format
 * 
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Service layer for authentication logic
    private AuthService authService;
    
    /**
     * Initialize servlet
     * Create AuthService instance
     */
    @Override
    public void init() throws ServletException {
        authService = new AuthService();
        System.out.println("[LoginServlet] Servlet initialized successfully");
    }
    
    // ========================================
    // GET Request Handler
    // ========================================
    
    /**
     * Handle GET requests
     * Supports: logout, checkSession
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "default";
        }
        
        switch (action) {
            case "logout":
                handleLogout(request, response);
                break;
            case "checkSession":
                checkSession(request, response);
                break;
            default:
                // Redirect to login page for unknown actions
                response.sendRedirect("pages/login.html");
                break;
        }
    }
    
    // ========================================
    // POST Request Handler
    // ========================================
    
    /**
     * Handle POST requests
     * Supports: login, register
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        // Validate action parameter
        if (action == null || action.trim().isEmpty()) {
            sendJsonResponse(response, false, "No action specified", null);
            return;
        }
        
        System.out.println("[LoginServlet] POST request - action: " + action);
        
        switch (action) {
            case "login":
                handleLogin(request, response);
                break;
            case "register":
                handleRegister(request, response);
                break;
            default:
                sendJsonResponse(response, false, "Invalid action", null);
                break;
        }
    }
    
    // ========================================
    // Login Handler
    // ========================================
    
    /**
     * Handle user login request
     * 
     * @param request HTTP request containing username and password
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get parameters from request
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        System.out.println("[LoginServlet] Login attempt: " + username);
        
        // Validate required parameters
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            sendJsonResponse(response, false, "Username and password are required", null);
            return;
        }
        
        try {
            // Authenticate user using AuthService
            User user = authService.authenticateUser(username, password);
            
            if (user != null) {
                // Authentication successful
                // Create session and store user information
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes timeout
                
                System.out.println("[LoginServlet] Login successful: " + username);
                
                // Send success response with user data
                sendJsonResponse(response, true, "Login successful", user);
            } else {
                // Authentication failed
                System.out.println("[LoginServlet] Login failed: Invalid credentials");
                sendJsonResponse(response, false, "Invalid username or password", null);
            }
            
        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("[LoginServlet] Login error: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(response, false, "Login error: " + e.getMessage(), null);
        }
    }
    
    // ========================================
    // Registration Handler
    // ========================================
    
    /**
     * Handle user registration request
     * 
     * @param request HTTP request containing user information
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get parameters from request
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        
        System.out.println("[LoginServlet] Registration attempt: " + username);
        
        // Validate required parameters
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {
            sendJsonResponse(response, false, "All required fields must be filled", null);
            return;
        }
        
        // Validate username length
        if (username.length() < 3 || username.length() > 20) {
            sendJsonResponse(response, false, "Username must be between 3 and 20 characters", null);
            return;
        }
        
        // Validate password length
        if (password.length() < 6) {
            sendJsonResponse(response, false, "Password must be at least 6 characters", null);
            return;
        }
        
        try {
            // Register user using AuthService
            User newUser = authService.registerUser(username, password, email, fullName, phone);
            
            if (newUser != null) {
                // Registration successful
                // Create session and log user in automatically
                HttpSession session = request.getSession(true);
                session.setAttribute("user", newUser);
                session.setAttribute("userId", newUser.getUserId());
                session.setAttribute("username", newUser.getUsername());
                session.setAttribute("role", newUser.getRole());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes timeout
                
                System.out.println("[LoginServlet] Registration successful: " + username);
                
                // Send success response with user data
                sendJsonResponse(response, true, "Registration successful", newUser);
            } else {
                // Registration failed
                System.out.println("[LoginServlet] Registration failed: Username or email already exists");
                sendJsonResponse(response, false, "Registration failed: Username or email already exists", null);
            }
            
        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("[LoginServlet] Registration error: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(response, false, "Registration error: " + e.getMessage(), null);
        }
    }
    
    // ========================================
    // Logout Handler
    // ========================================
    
    /**
     * Handle user logout request
     * Invalidate session and redirect to login page
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get existing session (don't create new one)
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate();
            System.out.println("[LoginServlet] User logged out: " + username);
        }
        
        // Redirect to login page
        response.sendRedirect("pages/login.html");
    }
    
    // ========================================
    // Session Check Handler
    // ========================================
    
    /**
     * Check if user session is active
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    private void checkSession(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get existing session (don't create new one)
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("user") != null) {
            // Session is active
            User user = (User) session.getAttribute("user");
            sendJsonResponse(response, true, "Session active", user);
        } else {
            // No active session
            sendJsonResponse(response, false, "No active session", null);
        }
    }
    
    // ========================================
    // JSON Response Helper Methods
    // ========================================
    
    /**
     * Send JSON response without user data
     * 
     * @param response HTTP response
     * @param success Success status (true/false)
     * @param message Response message
     * @throws IOException if I/O error occurs
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message)
            throws IOException {
        sendJsonResponse(response, success, message, null);
    }
    
    /**
     * Send JSON response with user data
     * 
     * Format: {success: true/false, message: "...", user: {...}}
     * 
     * @param response HTTP response
     * @param success Success status (true/false)
     * @param message Response message
     * @param user User object (can be null)
     * @throws IOException if I/O error occurs
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, 
                                 String message, User user) throws IOException {
        
        // Set response content type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Build JSON manually (no external libraries needed)
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\"");
        
        // Add user data if available
        if (user != null) {
            json.append(",\"user\":{");
            json.append("\"userId\":").append(user.getUserId()).append(",");
            json.append("\"username\":\"").append(escapeJson(user.getUsername())).append("\",");
            json.append("\"email\":\"").append(escapeJson(user.getEmail())).append("\",");
            json.append("\"fullName\":\"").append(escapeJson(user.getFullName())).append("\",");
            json.append("\"phone\":\"").append(escapeJson(user.getPhone() != null ? user.getPhone() : "")).append("\",");
            json.append("\"role\":\"").append(escapeJson(user.getRole())).append("\"");
            json.append("}");
        }
        
        json.append("}");
        
        // Send response
        out.print(json.toString());
        out.flush();
        
        System.out.println("[LoginServlet] JSON response sent: " + json.toString());
    }
    
    /**
     * Escape special characters for JSON
     * Prevents JSON injection and formatting errors
     * 
     * @param str String to escape
     * @return Escaped string safe for JSON
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        
        return str.replace("\\", "\\\\")   // Backslash
                  .replace("\"", "\\\"")   // Double quote
                  .replace("\n", "\\n")    // Newline
                  .replace("\r", "\\r")    // Carriage return
                  .replace("\t", "\\t");   // Tab
    }
}