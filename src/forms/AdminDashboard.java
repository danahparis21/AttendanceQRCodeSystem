package forms;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.sql.Connection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import java.util.Date;
import javax.swing.ImageIcon;

public class AdminDashboard extends JFrame {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, refreshButton, searchButton;
    private JTextField searchField;
    private JComboBox<String> searchFilter;
    private Connection connection;
    JLabel studentCountLabel, clockLabel;

      // Constructor
    public AdminDashboard() {
        setTitle("Attendance Management System");
        setSize(1920, 1080);
        
        setLayout(null);

        // Background Panel with Image
        JPanel backgroundPanel = new JPanel() {
        private ImageIcon bgImage = new ImageIcon(getClass().getResource("/images/dashboardbg.png")); // Ensure this path is correct

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            } else {
                System.out.println("Background image not found!"); // Debugging message
            }
        }
    };

        backgroundPanel.setBounds(0, 0, 1920, 1080);
        backgroundPanel.setLayout(null);
        add(backgroundPanel);

        // BUTTON PANEL
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(10, 180, 1520, 90);
        buttonPanel.setBackground(new Color(255, 255, 255, 150)); // Semi-transparent white
        buttonPanel.setLayout(null);
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        backgroundPanel.add(buttonPanel);

        JButton registerStudents = createButton("Register Students", 0, 20, buttonPanel);
        JButton viewStudents = createButton("View Students", 190, 20, buttonPanel);
        JButton updateStudents = createButton("Update Students", 380, 20, buttonPanel);
        JButton deleteStudent = createButton("Delete Student", 570, 20, buttonPanel);
        JButton generateQR = createButton("Generate QR", 760, 20, buttonPanel);
        JButton viewQR = createButton("View QR", 950, 20, buttonPanel);
        JButton markAttendance = createButton("Mark Attendance", 1140, 20, buttonPanel);
        JButton viewAttendance = createButton("View Attendance", 1330, 20, buttonPanel);

        // Register button functionality
        registerStudents.addActionListener(e -> new RegisterStudents().setVisible(true));
        viewStudents.addActionListener(e -> new ViewStudents().setVisible(true));
        updateStudents.addActionListener(e -> new UpdateStudents().setVisible(true));
        deleteStudent.addActionListener(e -> new DeleteStudent().setVisible(true));
        generateQR.addActionListener(e -> new GenerateQr().setVisible(true));
        viewQR.addActionListener(e -> new ViewQr().setVisible(true));
        markAttendance.addActionListener(e -> new MarkAttendance().setVisible(true));
        viewAttendance.addActionListener(e -> new ViewAttendance().setVisible(true));

        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBounds(650, 75, 900, 70);
        bottomPanel.setBackground(new Color(255, 255, 255, 180)); // Slight transparency
        bottomPanel.setLayout(null);
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        backgroundPanel.add(bottomPanel);

        // Students Enrolled Label
        studentCountLabel = new JLabel("Students Enrolled: Loading...");
        studentCountLabel.setFont(new Font("Arial", Font.BOLD, 18));
        studentCountLabel.setBounds(30, 15, 300, 40);
        bottomPanel.add(studentCountLabel);

        // Real-Time Clock Label
        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Arial", Font.BOLD, 18));
         clockLabel.setBackground(new Color(255, 255, 255, 180));
        clockLabel.setBounds(300, 15, 500, 40);
        bottomPanel.add(clockLabel);
       


        // LOGOUT BUTTON
        JButton logout = createButton("Logout", 700, 10, bottomPanel);
        bottomPanel.add(logout);
        logout.addActionListener(e -> logOut());

        // Fetch student count
        updateStudentCount();

        // Start real-time clock
        startClock();

        setVisible(true);
    }
    
    // Method to fetch student count from the database
    private void updateStudentCount() {
        try {
            Connection connection = Database.connect();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM userDetails WHERE role = 'Student'");

            if (rs.next()) {
                int count = rs.getInt(1);
                studentCountLabel.setText("Students Enrolled: " + count);
            }
            connection.close();
        } catch (Exception e) {
            studentCountLabel.setText("Students Enrolled: Error");
            e.printStackTrace();
        }
    }

    // Method to start real-time clock
    private void startClock() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm:ss a");
                dateFormat.setTimeZone(TimeZone.getDefault());
                String currentTime = dateFormat.format(new Date());
                SwingUtilities.invokeLater(() -> clockLabel.setText(currentTime));
            }
        }, 0, 1000);
        
    }

    // Logout method
    private void logOut() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    // Button creation helper method
    private JButton createButton(String text, int x, int y, JPanel panel) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBounds(x, y, 190, 50);

        
   
        // Default colors
        Color defaultBg = new Color(0x890418);
        Color defaultFg = Color.WHITE;
        Color hoverBg = Color.WHITE;
        Color hoverFg = new Color(0x890418);

        // Apply default styling
        button.setBackground(defaultBg);
        button.setForeground(defaultFg);
        button.setBorder(BorderFactory.createLineBorder(defaultBg, 1));
        button.setFocusPainted(false);
        button.setOpaque(true);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverBg);
                button.setForeground(hoverFg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultBg);
                button.setForeground(defaultFg);
            }
        });

        panel.add(button);
        return button;
    }
    public static void main(String[] args) {
        new AdminDashboard();
    }
}
