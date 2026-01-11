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
 * LoginServlet - 处理来自前端的登录请求，并调用认证服务
 * 
 * @author Your Name
 * @version 1.0
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private AuthService authService;
    
    @Override
    public void init() throws ServletException {
        // 初始化 Service
        authService = new AuthService();
    }
    
    /**
     * Handle GET requests
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
                // ③ 至少 sendRedirect
                response.sendRedirect("login.jsp");
                break;
        }
    }
    
    /**
     * Handle POST requests
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        // ③ 确认失败分支: 输出error
        if (action == null || action.trim().isEmpty()) {
            sendJsonResponse(response, false, "No action specified");
            return;
        }
        
        switch (action) {
            case "login":
                handleLogin(request, response);
                break;
            case "register":
                handleRegister(request, response);
                break;
            default:
                // ③ 至少输出 error
                sendJsonResponse(response, false, "Invalid action");
                break;
        }
    }
    
    /**
     * Handle user login
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // ③ 确认失败分支: 输出error
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            sendJsonResponse(response, false, "Username and password are required");
            return;
        }
        
        try {
            // 调用 AuthService 进行认证
            User user = authService.authenticateUser(username, password);
            
            if (user != null) {
                // 创建 session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
                
                // 返回成功响应
                sendJsonResponse(response, true, "Login successful", user);
            } else {
                // ③ 确认失败分支: 输出error
                sendJsonResponse(response, false, "Invalid username or password");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // ③ 确认失败分支: 输出error
            sendJsonResponse(response, false, "Login error: " + e.getMessage());
        }
    }
    
    /**
     * Handle user registration
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        
        // ③ 确认失败分支: 输出error
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {
            sendJsonResponse(response, false, "All required fields must be filled");
            return;
        }
        
        // ③ 确认失败分支: 输出error
        if (username.length() < 3 || username.length() > 20) {
            sendJsonResponse(response, false, "Username must be between 3 and 20 characters");
            return;
        }
        
        // ③ 确认失败分支: 输出error
        if (password.length() < 6) {
            sendJsonResponse(response, false, "Password must be at least 6 characters");
            return;
        }
        
        try {
            // 调用 AuthService 进行注册
            User user = authService.registerUser(username, password, email, fullName, phone);
            
            if (user != null) {
                // 创建 session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                
                // 返回成功响应
                sendJsonResponse(response, true, "Registration successful", user);
            } else {
                // ③ 确认失败分支: 输出error
                sendJsonResponse(response, false, "Registration failed: Username or email already exists");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // ③ 确认失败分支: 输出error
            sendJsonResponse(response, false, "Registration error: " + e.getMessage());
        }
    }
    
    /**
     * Handle user logout
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // ③ 至少 sendRedirect
        response.sendRedirect("login.jsp");
    }
    
    /**
     * Check if user session is active
     */
    private void checkSession(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            sendJsonResponse(response, true, "Session active", user);
        } else {
            // ③ 确认失败分支: 输出error
            sendJsonResponse(response, false, "No active session");
        }
    }
    
    /**
     * Send JSON response
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message)
            throws IOException {
        sendJsonResponse(response, success, message, null);
    }
    
    /**
     * Send JSON response with user data
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, User user)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();
        
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\"");
        
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
        
        out.print(json.toString());
        out.flush();
    }
    
    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
