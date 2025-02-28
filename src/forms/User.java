package forms;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.JOptionPane;

public class User {
    
    private int userID;
    private String role, email;

    // Constructor
    public User(int userID, String email, String role) {
        this.userID = userID;
        this.role = role;
        this.email = email;
    }

    // Getters
    public int getUserID() {
        return userID;
    }

    public String getRole() {
        return role;
    }

    


//    // Hash password using SHA-256
//    private static String hashPassword(String password) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] hashedBytes = md.digest(password.getBytes());
//            StringBuilder sb = new StringBuilder();
//            for (byte b : hashedBytes) {
//                sb.append(String.format("%02x", b));
//            }
//            return sb.toString();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
    
            
            

    // Login method (Fixed: Hash password before checking)
    public static User login(String email, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Database.connect();
            String sql = "SELECT id, role FROM userdetails WHERE email = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, password); // Fix: Hash password before checking
            
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(rs.getInt("id"), email, rs.getString("role"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null; // If no user is found
    }
}
