
package forms;

import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import static java.lang.System.out;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import utility.BDUtility;

public class StudentDashboard extends JFrame {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, refreshButton, searchButton;
    private JTextField searchField;
    private JComboBox<String> searchFilter;
    private Connection connection;
    JLabel studentCountLabel, clockLabel;
    private ImageIcon gifBackground;
    private JLabel background; 
    JPanel backgroundPanel;
    private JLabel nameLabel, srCodeLabel, lblImage, lblQrImage;
    private Connection connect;
    private int studentId;
    private String studentEmail; // Used for QR code file naming
   


      // Constructor
    public StudentDashboard(int studentId, Connection connection) {
         this.studentId = studentId;
        this.connect = connection;

        
       
        setTitle("Student Dashboard");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);
        setLayout(null);

       // Load GIF as Background (Do not resize directly)
        gifBackground = new ImageIcon(getClass().getResource("/images/studentbg.gif"));
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
        
        nameLabel = new JLabel("Name");
        nameLabel.setBounds(370, 530,200,20);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);
         background.add(nameLabel);
         
        srCodeLabel= new JLabel("SR-Code");
        srCodeLabel.setBounds(480, 640,100,20);
        srCodeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        srCodeLabel.setForeground(Color.WHITE);
         background.add(srCodeLabel);
        
        // IMAGE UPLOAD (JInternalFrame)
       JInternalFrame imageFrame = new JInternalFrame("Student Image", false, false, false, false);
       imageFrame.setBounds(390, 365, 140, 140);
       imageFrame.setLayout(null);
       background.add(imageFrame); // ✅ Add to background panel

       lblImage = new JLabel("No Image", SwingConstants.CENTER);
       lblImage.setBounds(0, 0, 140, 140);
       lblImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
       imageFrame.add(lblImage);
       imageFrame.setVisible(true);
       
       //QR
       // IMAGE UPLOAD (JInternalFrame)
        JInternalFrame QrFrame = new JInternalFrame("QR Image", false, false, false, false);
        QrFrame.setBounds(700, 200, 300, 300);
        QrFrame.setLayout(null);
        background.add(QrFrame); // ✅ Add to background panel

        lblQrImage = new JLabel("No Image", SwingConstants.CENTER);
        lblQrImage.setBounds(0, 0, 300, 300);
        lblQrImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        QrFrame.add(lblQrImage);
        QrFrame.setVisible(true);
        
        JButton saveQrAtButton = createStyledButton("Download QR", 760, 550);
        background.add(saveQrAtButton); // ✅ Add to background panel
        saveQrAtButton.addActionListener(e -> saveQrAt(studentEmail));
        
        JButton updateDetails = createStyledButton("Edit Details", 760, 700);
        background.add(updateDetails); // ✅ Add to background panel
        updateDetails.addActionListener(e -> new EditDetails(studentId).setVisible(true));
        
        // Report Panel
        JPanel reportPanel = new JPanel();
        reportPanel.setBounds(1200, 150, 300, 520); // Increased height
        reportPanel.setBackground(new Color(0x343a40));
        reportPanel.setLayout(null);
        background.add(reportPanel);

        // Label for Start Date
        JLabel lblFrom = new JLabel("On/From:");
        lblFrom.setBounds(20, 10, 100, 20);
        lblFrom.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblFrom.setForeground(Color.WHITE);
        reportPanel.add(lblFrom);

        // Start Date Chooser
        JDateChooser startDateChooser = new JDateChooser();
        startDateChooser.setBounds(20, 50, 150, 25);
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        reportPanel.add(startDateChooser);

        // Label for End Date
        JLabel lblTo = new JLabel("To:");
        lblTo.setBounds(20, 100, 100, 20);
        lblTo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTo.setForeground(Color.WHITE);
        reportPanel.add(lblTo);

        // End Date Chooser
        JDateChooser endDateChooser = new JDateChooser();
        endDateChooser.setBounds(20, 150, 150, 25);
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        reportPanel.add(endDateChooser);

        // Button to calculate attendance
        JButton btnCalculate = new JButton("Calculate");
        btnCalculate.setBounds(30, 200, 120, 25);
        reportPanel.add(btnCalculate);

        // Labels for Present and Absent Counts
        JLabel lblPresent = new JLabel("Present:");
        lblPresent.setBounds(20, 300, 100, 20);
        lblPresent.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblPresent.setForeground(Color.WHITE);
        reportPanel.add(lblPresent);

        JLabel lblPresentCount = new JLabel("0");
        lblPresentCount.setBounds(120, 300, 50, 20);
        lblPresentCount.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblPresentCount.setForeground(Color.GREEN);
        reportPanel.add(lblPresentCount);

        JLabel lblAbsent = new JLabel("Absent:");
        lblAbsent.setBounds(20, 400, 100, 20);
        lblAbsent.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAbsent.setForeground(Color.WHITE);
        reportPanel.add(lblAbsent);

        JLabel lblAbsentCount = new JLabel("0");
        lblAbsentCount.setBounds(120, 400, 50, 20);
        lblAbsentCount.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAbsentCount.setForeground(Color.RED);
        reportPanel.add(lblAbsentCount);
            btnCalculate.addActionListener(e -> {
        Date fromDateFromCal = startDateChooser.getDate();
        LocalDate fromDate = null;
        if (fromDateFromCal != null) {
            fromDate = fromDateFromCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        Date toDateFromCal = endDateChooser.getDate();
        LocalDate toDate = null;
        if (toDateFromCal != null) {
            toDate = toDateFromCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        if (fromDate != null && toDate != null) {
            long totalDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1; // Include both start and end date

            int presentCount = getPresentCount(fromDate, toDate, studentId); // Fetch from DB
            int absentCount = (int) (totalDays - presentCount);

            lblPresentCount.setText(String.valueOf(presentCount));
            lblAbsentCount.setText(String.valueOf(absentCount));
        }
    });



        
        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBounds(650, 90, 900, 50);
        bottomPanel.setBackground(new Color(0x343a40)); // Slight transparency
        bottomPanel.setLayout(null);
        //bottomPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        background.add(bottomPanel);

        // Students Enrolled Label
        studentCountLabel = new JLabel("Welcome, Student!");
        studentCountLabel.setFont(new Font("Arial", Font.BOLD, 18));
        studentCountLabel.setBounds(30, 10, 300, 40);
        studentCountLabel.setForeground(Color.WHITE);
        bottomPanel.add(studentCountLabel);

        // Real-Time Clock Label
        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Arial", Font.BOLD, 18));
         clockLabel.setBackground(new Color(255, 255, 255, 180));
         clockLabel.setForeground(Color.WHITE);
        clockLabel.setBounds(300, 10, 500, 40);
        bottomPanel.add(clockLabel);
       


        // LOGOUT BUTTON
        JButton logout = createButton("Logout", 680, 5, bottomPanel);
        bottomPanel.add(logout);
        logout.addActionListener(e -> logOut());


        // Start real-time clock
        startClock();
        fetchStudentDetails();
        setVisible(true);
        
    }
    private int getPresentCount(LocalDate fromDate, LocalDate toDate, int userId) {
        int count = 0;
        try {
            Connection connection = Database.connect();
            String query = "SELECT COUNT(DISTINCT date) FROM userattendance WHERE userid = ? AND date BETWEEN ? AND ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setDate(2, new java.sql.Date(convertToDate(fromDate).getTime()));  // Convert LocalDate to java.util.Date then to java.sql.Date
            stmt.setDate(3, new java.sql.Date(convertToDate(toDate).getTime()));    // Convert LocalDate to java.util.Date then to java.sql.Date

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return count;
    }

    // Helper function to convert LocalDate to java.util.Date
    private Date convertToDate(LocalDate localDate) {
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    
  
   
    
     // Fetch student details from the database
    private void fetchStudentDetails() {
        try {
            String query = "SELECT name, sr_code, email, imagename FROM userdetails WHERE id = ?";
            PreparedStatement stmt = connect.prepareStatement(query);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String srCode = rs.getString("sr_code");
                studentEmail = rs.getString("email");
                String imageName = rs.getString("imagename");

                if (nameLabel != null) {
                    nameLabel.setText(name);
                }
                if (srCodeLabel != null) {
                    srCodeLabel.setText(srCode);
                }

                if (imageName != null && !imageName.trim().isEmpty()) {
                    String imagePath = BDUtility.getPath("/images" + File.separator + imageName);
                    File imageFile = new File(imagePath);

                    if (imageFile.exists()) {
                        ImageIcon icon = new ImageIcon(imagePath);
                        Image image = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                        lblImage.setIcon(new ImageIcon(image));
                        lblImage.setText("");
                    } else {
                        lblImage.setIcon(null);
                        lblImage.setText("Image Not Found");
                        JOptionPane.showMessageDialog(null, "Image has been deleted or not found.",
                                "Image Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    lblImage.setIcon(null);
                    lblImage.setText("No Image");
                }

            
                // Convert email to correct format
                String formattedEmail = studentEmail.replace("@", "_");

                // Construct correct file path
                String qrPath = BDUtility.getPath("qrCodes" + File.separator + formattedEmail + "_QR.jpg");
                System.out.println("Looking for QR code at: " + qrPath);


                File qrFile = new File(qrPath);
                if (qrFile.exists()) {
                    ImageIcon qrIcon = new ImageIcon(qrPath);
                    Image qrImage = qrIcon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                    lblQrImage.setIcon(new ImageIcon(qrImage)); // ✅ Assign to correct JLabel
                    lblQrImage.setText(""); 
                } else {
                    lblQrImage.setIcon(null);
                    lblQrImage.setText("QR Not Found");
                }
}
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    


private void saveQrAt(String email) {
    if (email == null || email.isEmpty()) {
        JOptionPane.showMessageDialog(null, "No Email Found");
        return;
    }

    try {
        // Convert email to correct format
        String formattedEmail = email.replace("@", "_");

        // Construct QR code file path
        String qrPath = BDUtility.getPath("qrCodes" + File.separator + formattedEmail + "_QR.jpg");
        File qrFile = new File(qrPath);

        if (!qrFile.exists()) {
            JOptionPane.showMessageDialog(null, "QR code file not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Let user choose where to save the QR code
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save QR Code at");
        fileChooser.setSelectedFile(new File(formattedEmail + "_QR.jpg"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Copy QR file to chosen location
            Files.copy(qrFile.toPath(), fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);

            JOptionPane.showMessageDialog(null, "QR code saved successfully!");
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Something went wrong while saving the QR code.", "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

    private JButton createStyledButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 180, 40);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(0x0284c8));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(0x0284c8), 2));
        button.setFocusPainted(false);
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(0x0284c8));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x0284c8));
                button.setForeground(Color.WHITE);
            }
        });

        return button;
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
        button.setBounds(x, y, 190, 40);

        
   
        // Default colors
        Color defaultBg = new Color(0xd2232a);
        Color defaultFg = Color.WHITE;
        Color hoverBg = Color.WHITE;
        Color hoverFg = new Color(0xd2232a);

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
    
     private void resizeBackground() {
        background.setBounds(0, 0, getWidth(), getHeight());
        background.revalidate();
        background.repaint();
    }


    public static void main(String[] args) {
        // Example usage (replace with actual connection and student ID after login)
        Connection dbConnection = Database.connect(); // Implement this method
        int loggedInStudentId = 2; // Get this from the login system
        new StudentDashboard(loggedInStudentId, dbConnection);
    }
}