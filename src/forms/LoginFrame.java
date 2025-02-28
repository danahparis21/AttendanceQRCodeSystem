package forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel background;
    private ImageIcon gifBackground;
    
    private final Color normalRed = new Color(0xd2232a);
    private final Color hoverRed = new Color(0x890418);
    private final Color pressedRed= new Color(0x42020e);
    

    public LoginFrame() {
        setTitle("Library System - Login");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);

        // Load GIF as Background (Do not resize directly)
        gifBackground = new ImageIcon(getClass().getResource("/images/bsubg.gif"));
        background = new JLabel(gifBackground);
        background.setBounds(0, 0, getWidth(), getHeight()); 
        background.setLayout(null); // Allow components inside it
        getContentPane().add(background);

        // Resize GIF when window resizes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeBackground();
            }
        });


        // 🏢 Login Panel
        JPanel loginPanel = new JPanel();
        loginPanel.setBounds(100, 150, 500, 550);
        loginPanel.setBackground(new Color(255, 255, 255, 200));
        loginPanel.setLayout(null);

        // 🏷️ Log In Title
        JLabel loginTitle = new JLabel("Log In");
        loginTitle.setFont(new Font("Arial", Font.BOLD, 50));
        loginTitle.setForeground(new Color(50, 50, 50));
        loginTitle.setBounds(190, 30, 200, 100);
        loginPanel.add(loginTitle);

        background.add(loginPanel);

        // 📌 Labels & Input Fields
        JLabel userLabel = new JLabel("Email:");
        userLabel.setBounds(60, 250, 150, 40);
        userLabel.setFont(new Font("Arial", Font.BOLD, 20));
        loginPanel.add(userLabel);

        emailField = new JTextField();
        emailField.setBounds(60, 300, 350, 40);
        emailField.setFont(new Font("Arial", Font.PLAIN, 18));
        loginPanel.add(emailField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(60, 350, 150, 40);
        passLabel.setFont(new Font("Arial", Font.BOLD, 20));
        loginPanel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(60, 400, 350, 40);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        loginPanel.add(passwordField);

        // ✅ Login Button
        loginButton = new JButton("Login");
        loginButton.setBounds(160, 500, 150, 50);
        styleButton(loginButton, normalRed, hoverRed, pressedRed);
        loginPanel.add(loginButton);

        Connection connection = Database.connect(); // Adjust based on your setup

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            User user = User.login(email, password);

            if (user == null) {
                JOptionPane.showMessageDialog(null, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int ID = user.getUserID();
                String role = user.getRole();

                if ("Admin".equals(role)) {
                    new AdminDashboard().setVisible(true);
                    dispose();
                } else if ("Student".equals(role)) {
                    new StudentDashboard(ID, connection).setVisible(true); // Pass the connection
           
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Unexpected role: " + role, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    // 🎨 Button Styling
    private void styleButton(JButton button, Color normalColor, Color hoverColor, Color pressedColor) {
        button.setBackground(normalColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(110, 80, 34), 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // 🔄 Hover & Click Effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(pressedColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(hoverColor);
            }
        });
    }

    // 🔄 Resize Background GIF
    // 🔄 Resize Background GIF Without Losing Animation
    private void resizeBackground() {
        background.setBounds(0, 0, getWidth(), getHeight());
        background.revalidate();
        background.repaint();
    }


    public static void main(String[] args) {
        new LoginFrame();
    }
}
