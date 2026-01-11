package dao.impl;

import dao.UserDAO;
import model.User;
import util.DBConnection;
import java.sql.*;

/**
 * User DAO Implementation
 * Responsibility: Implement data access operations for User entity using JDBC
 */
public class UserDAOImpl implements UserDAO {
    
    private DBConnection dbConnection;
    
    /**
     * Constructor
     */
    public UserDAOImpl() {
        this.dbConnection = DBConnection.getInstance();
    }
    
    @Override
    public User findByUserName(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return null;
    }
    
    @Override
    public User findByUserNameAndPassword(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return null;
    }
    
    @Override
    public int create(User user, String hashedPassword) {
        String sql = "INSERT INTO users (username, email, password, full_name, phone, role, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getRole());
            stmt.setString(7, user.getStatus());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return -1;
    }
    
    @Override
    public int update(User user) {
        String sql = "UPDATE users SET email = ?, full_name = ?, phone = ?, " +
                     "role = ?, status = ?, updated_at = NOW() WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getStatus());
            stmt.setInt(6, user.getUserId());
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return 0;
    }
    
    @Override
    public int deleteById(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return 0;
    }
    
    @Override
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return null;
    }
    
    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return null;
    }
    
    @Override
    public int updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = NOW() WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, null);
        }
        
        return 0;
    }
    
    /**
     * Extract User object from ResultSet
     * @param rs ResultSet
     * @return User object
     * @throws SQLException if SQL error occurs
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
    
    /**
     * Close database resources
     * @param conn Connection
     * @param stmt Statement
     * @param rs ResultSet
     */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
