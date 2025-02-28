
package forms;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import utility.BDUtility;


/**
 *
 * @author 63945
 */
public class RegisterStudents extends JFrame {
    private JTextField nameField, emailField, contactField, addressField, srCodeField;
    private JLabel lblImage;
    private JRadioButton maleButton, femaleButton;
    private JButton addButton, clearButton, registerButton;

    public RegisterStudents() {
        setTitle("Register Students");
        setSize(1050, 600);
        setLayout(null);
        
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        // Background Panel with Image
        JPanel backgroundPanel = new JPanel() {
            private ImageIcon bgImage = new ImageIcon(getClass().getResource("/images/bg3.png")); // Ensure this path is correct

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

        backgroundPanel.setBounds(0, 0, 1050, 600);
        backgroundPanel.setLayout(null);
        add(backgroundPanel); // Add the background panel first

        // HEADER
        JLabel header = new JLabel("REGISTER STUDENT", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBounds(660, 40, 300, 30);
        header.setForeground(Color.WHITE);
        backgroundPanel.add(header); // Add to backgroundPanel

        // FORM FIELDS
        addLabelAndField("SR-Code:", 120, srCodeField = new JTextField(), backgroundPanel);
        addLabelAndField("Name:", 160, nameField = new JTextField(), backgroundPanel);

        // GENDER SELECTION
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setBounds(100, 200, 120, 25);
        genderLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        backgroundPanel.add(genderLabel);

        maleButton = new JRadioButton("Male");
        maleButton.setBounds(200, 200, 80, 25);
        maleButton.setOpaque(false); // Ensure transparency
        femaleButton = new JRadioButton("Female");
        femaleButton.setBounds(260, 200, 80, 25);
        femaleButton.setOpaque(false); // Ensure transparency

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);

        backgroundPanel.add(maleButton);
        backgroundPanel.add(femaleButton);

        addLabelAndField("Email:", 240, emailField = new JTextField(), backgroundPanel);
        addLabelAndField("Contact:", 280, contactField = new JTextField(), backgroundPanel);
        addLabelAndField("Address:", 320, addressField = new JTextField(), backgroundPanel);

        // IMAGE UPLOAD (JInternalFrame)
        JInternalFrame imageFrame = new JInternalFrame("Student Image", false, false, false, false);
        imageFrame.setBounds(600, 100, 250, 250);
        imageFrame.setLayout(null);
        backgroundPanel.add(imageFrame);

        lblImage = new JLabel("No Image", SwingConstants.CENTER);
        lblImage.setBounds(0, 0, 250, 250);
        lblImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageFrame.add(lblImage);
        imageFrame.setVisible(true);

        JButton btnUpload = new JButton("Upload Image");
        btnUpload.setBounds(600, 350, 250, 30);
        btnUpload.addActionListener(e -> chooseImage());
        backgroundPanel.add(btnUpload);

        // BUTTONS
        registerButton = createStyledButton("Register Student", 300, 400);
        backgroundPanel.add(registerButton);
        registerButton.addActionListener(this::registerStudent);

        clearButton = createStyledButton("Clear", 500, 400);
        backgroundPanel.add(clearButton);
        clearButton.addActionListener(e -> clearFields());

        setVisible(true);
    }

    private void addLabelAndField(String labelText, int y, JTextField field, JPanel panel) {
        JLabel label = new JLabel(labelText);
        label.setBounds(100, y, 120, 25);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(label);

        field.setBounds(200, y, 230, 30);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(field);
        panel.add(label);
        panel.add(field);
    }

    private JButton createStyledButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 180, 40);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(0x393939));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(0x393939), 2));
        button.setFocusPainted(false);
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(0x393939));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x393939));
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }
   
    BufferedImage originalImage = null;
    File selectedFile = null;
    private void chooseImage() {
        
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile=fileChooser.getSelectedFile();
            lblImage.setText("");
            try{
                originalImage =ImageIO.read(selectedFile);
                
                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();
                
                int labelWidth = lblImage.getWidth();
                int labelHeight = lblImage.getHeight();
                
                double scaleX = (double) labelWidth/originalWidth;
                double scaleY = (double) labelHeight/originalHeight;
                
                double scale = Math.min(scaleX, scaleY);
                
                int scaleWidth = (int)(originalWidth * scale);
                int scaleHeight = (int)(originalHeight * scale);
                
                Image scaledImage = originalImage.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                lblImage.setIcon(icon);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
   

    private void registerStudent(ActionEvent e) {
      String srCode = srCodeField.getText();
      String name = nameField.getText();
      String email = emailField.getText();
      String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

      if (!email.matches(emailRegex)) {
          JOptionPane.showMessageDialog(null, "Invalid Email.", "Invalid", JOptionPane.ERROR_MESSAGE);
          return;
      }

      String contact = contactField.getText();
      String contactRegex = "^\\d{11}$"; // Ensures exactly 11 digits
      if (!contact.matches(contactRegex)) {
          JOptionPane.showMessageDialog(null, "Invalid Contact number", "Invalid", JOptionPane.ERROR_MESSAGE);
          return; // Added return to stop execution on error
      }

      String address = addressField.getText();
      String gender = maleButton.isSelected() ? "Male" : femaleButton.isSelected() ? "Female" : "";
      String password = name.split(" ")[0]; // First name as password
      String uniqueRegId = "" + System.nanoTime()+ System.nanoTime(); // No need to repeat nanoTime multiple times

      if (srCode.isEmpty() || name.isEmpty() || email.isEmpty() || contact.isEmpty() || address.isEmpty() || gender.isEmpty()) {
          JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Error", JOptionPane.WARNING_MESSAGE);
          return;
      }

      Connection connection = Database.connect();

      try {
          // Check for duplicate email
          String checkQuery = "SELECT * FROM userdetails WHERE email = ?";
          try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
              checkStmt.setString(1, email);
              ResultSet rs = checkStmt.executeQuery();
              if (rs.next()) {
                  JOptionPane.showMessageDialog(null, "Duplicate email.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                  return;
              }
          }

          // Save the image
          String imageName = saveImage(email);

          // Insert new student record
          String insertQuery = "INSERT INTO userdetails (sr_code, name, gender, email, contact, address, uniqueregid, password, imagename, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
          try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
               stmt.setString(1, srCode);
              stmt.setString(2, name);
              stmt.setString(3, gender);
              stmt.setString(4, email);
              stmt.setString(5 , contact);
              stmt.setString(6, address);
              stmt.setString(7, uniqueRegId);
              stmt.setString(8, password);
              stmt.setString(9, imageName);
              stmt.setString(10, "Student");

              stmt.executeUpdate(); // Corrected from executeQuery()

              JOptionPane.showMessageDialog(null, "Student registered successfully!");
              clearFields();
          }

      } catch (SQLException ex) {
          ex.printStackTrace();
          JOptionPane.showMessageDialog(this, "Error registering student.", "Database Error", JOptionPane.ERROR_MESSAGE);
      }
  }

   
   
   
    private String saveImage(String email) {
    if (originalImage != null && selectedFile != null) {
        try {
            String savePath = BDUtility.getPath("images" + File.separator);
            String extension = BDUtility.getFileExtension(selectedFile.getName());
            
            if (extension.isEmpty()) {
                System.err.println("Invalid file extension.");
                return null;
            }

            String imageName = email + "." + extension;
            File saveFile = new File(savePath + imageName);

            BufferedImage scaledImage = BDUtility.scaleImage(originalImage); // Fixed method usage
            ImageIO.write(scaledImage, extension, saveFile);

            return imageName;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    return null; // Ensure method always returns a value
}

   
    
     private void clearFields() {
        srCodeField.setText("");
        nameField.setText("");
        emailField.setText("");
        contactField.setText("");
        addressField.setText("");
        maleButton.setSelected(false);
        femaleButton.setSelected(false);
        lblImage.setIcon(null);
    }
    public static void main(String[] args) {
        new RegisterStudents();
    }
}
        
