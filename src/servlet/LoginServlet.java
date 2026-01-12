package servlet;

import model.User;
import service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Login Servlet
 * Handles user authentication, registration, and session management
 *
 * Supported Actions:
 * - login: User login
 * - register: User registration
 * - logout: User logout
 * - checkSession: Check if user is logged in
 * - listUsers: Get all users (admin only)
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private UserService userService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
        gson = new Gson();
    }

    // ================================
    // POST Request Handler
    // ================================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set character encoding
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // Get action parameter
        String action = request.getParameter("action");

        if (action == null) {
            sendErrorResponse(response, "Action parameter is required");
            return;
        }

        // Route to appropriate handler
        switch (action) {
            case "login":
                handleLogin(request, response);
                break;
            case "register":
                handleRegister(request, response);
                break;
            case "logout":
                handleLogout(request, response);
                break;
            default:
                sendErrorResponse(response, "Invalid action: " + action);
        }
    }

    // ================================
    // GET Request Handler
    // ================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            sendErrorResponse(response, "Action parameter is required");
            return;
        }

        switch (action) {
            case "checkSession":
                handleCheckSession(request, response);
                break;
            case "logout":
                handleLogout(request, response);
                break;
            case "listUsers":
                handleListUsers(request, response);
                break;
            default:
                sendErrorResponse(response, "Invalid action: " + action);
        }
    }

    // ================================
    // Login Handler
    // ================================

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        // Validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            sendErrorResponse(response, "Username and password are required");
            return;
        }

        // Authenticate user
        User user = userService.loginUser(username, password, role);

        if (user != null) {
            // Login successful - create session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setMaxInactiveInterval(3600); // 1 hour

            // Send success response with user data (without password)
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Login successful");
            jsonResponse.add("user", gson.toJsonTree(user.toDTO()));

            sendJsonResponse(response, jsonResponse);

        } else {
            // Login failed
            sendErrorResponse(response, "Invalid username or password");
        }
    }

    // ================================
    // Register Handler
    // ================================

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role");

        // Default to 'user' role if not specified
        if (role == null || role.trim().isEmpty()) {
            role = "user";
        }

        // Validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            sendErrorResponse(response, "Username, password, and email are required");
            return;
        }

        // Use username as fullName if not provided
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = username;
        }

        // Register user
        User user = userService.registerUser(username, password, email, fullName, phone, role);

        if (user != null) {
            // Registration successful
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Registration successful");
            jsonResponse.addProperty("userId", user.getUserId());

            sendJsonResponse(response, jsonResponse);

        } else {
            // Registration failed (username or email already exists)
            sendErrorResponse(response, "Username or email already exists");
        }
    }

    // ================================
    // Logout Handler
    // ================================

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", "Logout successful");

        sendJsonResponse(response, jsonResponse);
    }

    // ================================
    // Check Session Handler
    // ================================

    private void handleCheckSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("user") != null) {
            // User is logged in
            User user = (User) session.getAttribute("user");

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("loggedIn", true);
            jsonResponse.add("user", gson.toJsonTree(user.toDTO()));

            sendJsonResponse(response, jsonResponse);

        } else {
            // User is not logged in
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("loggedIn", false);
            jsonResponse.addProperty("message", "No active session");

            sendJsonResponse(response, jsonResponse);
        }
    }

    // ================================
    // List Users Handler (Admin only)
    // ================================

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Check if user is admin
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            sendErrorResponse(response, "Unauthorized - Please login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        if (!currentUser.isAdmin()) {
            sendErrorResponse(response, "Unauthorized - Admin access required");
            return;
        }

        // Get all users
        List<User> users = userService.getAllUsers();

        if (users != null) {
            // Convert to DTOs (without passwords)
            List<User.UserDTO> userDTOs = new java.util.ArrayList<>();
            for (User user : users) {
                userDTOs.add(user.toDTO());
            }

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.add("users", gson.toJsonTree(userDTOs));

            sendJsonResponse(response, jsonResponse);

        } else {
            sendErrorResponse(response, "Failed to retrieve users");
        }
    }

    // ================================
    // Helper Methods
    // ================================

    /**
     * Send JSON response
     */
    private void sendJsonResponse(HttpServletResponse response, JsonObject jsonObject)
            throws IOException {
        PrintWriter out = response.getWriter();
        out.print(jsonObject.toString());
        out.flush();
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(HttpServletResponse response, String message)
            throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("message", message);

        sendJsonResponse(response, jsonResponse);
    }
}