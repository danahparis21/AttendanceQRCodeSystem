package forms;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.constant.ConstantDescs.NULL;
import java.sql.*;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import utility.BDUtility;


/**
 *
 * @author 63945
 */
public class EditDetails extends JFrame {
    private JTextField nameField, emailField, contactField, addressField, srCodeField;
    private JLabel lblImage;
    private JRadioButton maleButton, femaleButton;
    private JButton addButton, clearButton, registerButton;
    String uniqueReg = null;
    String existingImageName = null;
    BufferedImage originalImage = null;
    File selectedFile = null;
      private int studentId;
    

     public EditDetails(int studentID) {
        this.studentId = studentID;

        setTitle("Edit Details");
        setSize(1050, 600);
        setLayout(null);
        setLocationRelativeTo(null);
       

        // Background Panel with Image
        JPanel backgroundPanel = new JPanel() {
            private ImageIcon bgImage = new ImageIcon(getClass().getResource("/images/bg3.png")); // Ensure correct path

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
        add(backgroundPanel); // Add background panel first

        // HEADER
        JLabel header = new JLabel("UPDATE STUDENT DETAILS", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBounds(660, 40, 300, 30);
        header.setForeground(Color.WHITE);
        backgroundPanel.add(header); // Add to backgroundPanel

        // SEARCH BY SR CODE
        srCodeField = new JTextField();
        addLabelAndField("SR Code:", 120, srCodeField, backgroundPanel);
        srCodeField.setEditable(false);

        // FORM FIELDS
        nameField = new JTextField();
        addLabelAndField("Name:", 160, nameField, backgroundPanel);

        emailField = new JTextField();
        addLabelAndField("Email:", 200, emailField, backgroundPanel);

        // GENDER SELECTION
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setBounds(100, 240, 120, 25);
        genderLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        backgroundPanel.add(genderLabel);

        maleButton = new JRadioButton("Male");
        maleButton.setBounds(200, 240, 80, 25);
        femaleButton = new JRadioButton("Female");
        femaleButton.setBounds(300, 240, 80, 25);

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);

        backgroundPanel.add(maleButton);
        backgroundPanel.add(femaleButton);

        
        addLabelAndField("Contact:", 280, contactField = new JTextField(), backgroundPanel);
        addLabelAndField("Address:", 320, addressField = new JTextField(), backgroundPanel);


        // IMAGE UPLOAD (JInternalFrame)
        JInternalFrame imageFrame = new JInternalFrame("Student Image", false, false, false, false);
        imageFrame.setBounds(600, 100, 300, 300);
        imageFrame.setLayout(null);
        backgroundPanel.add(imageFrame);

        lblImage = new JLabel("No Image", SwingConstants.CENTER);
        lblImage.setBounds(0, 0, 300, 300);
        lblImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageFrame.add(lblImage);
        imageFrame.setVisible(true);

        JButton btnUpload = new JButton("Update Image");
        btnUpload.setBounds(600, 400, 300, 30);
        btnUpload.addActionListener(e -> chooseImage());
        backgroundPanel.add(btnUpload);

        // BUTTONS
        

        JButton updateButton = createStyledButton("Update Student", 300, 450);
        backgroundPanel.add(updateButton);
        updateButton.addActionListener(this::updateStudent);
        searchStudent(studentId);

        setVisible(true);
    }
    
    private void updateStudent(ActionEvent e) {
    try {
        String srCode = srCodeField.getText().trim();
        String name = nameField.getText().trim();
        String gender = maleButton.isSelected() ? "Male" : femaleButton.isSelected() ? "Female" : "";
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String address = addressField.getText().trim();
        String password = name.isEmpty() ? "" : name.split(" ")[0]; // First name as password
        
        // Validate email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            JOptionPane.showMessageDialog(null, "Invalid Email.", "Invalid", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate contact number (exactly 11 digits)
        String contactRegex = "^\\d{11}$";
        if (!contact.matches(contactRegex)) {
            JOptionPane.showMessageDialog(null, "Invalid Contact number", "Invalid", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if required fields are empty
        if (srCode.isEmpty() || name.isEmpty() || email.isEmpty() || contact.isEmpty() || address.isEmpty() || gender.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection connection = Database.connect();

        // ✅ Fix: Use a PreparedStatement for checking SR-Code
        String checkQuery = "SELECT * FROM userdetails WHERE sr_code = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
        checkStmt.setString(1, srCode);
        ResultSet rs = checkStmt.executeQuery();

        if (!rs.next()) {
            JOptionPane.showMessageDialog(null, "SR-Code not found", "NOT FOUND", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // ✅ Save the image and get the file name
        String imageName = saveImage(email);

        // ✅ Fix: Properly structured UPDATE query based on whether imageName exists
        String updateQuery;
        if (imageName != null) {
            updateQuery = "UPDATE userdetails SET name = ?, gender = ?, email = ?, contact = ?, address = ?, imagename = ? WHERE sr_code = ?";
        } else {
            updateQuery = "UPDATE userdetails SET name = ?, gender = ?, email = ?, contact = ?, address = ? WHERE sr_code = ?";
        }

        PreparedStatement stmt = connection.prepareStatement(updateQuery);
        stmt.setString(1, name);
        stmt.setString(2, gender);
        stmt.setString(3, email);
        stmt.setString(4, contact);
        stmt.setString(5, address);

        if (imageName != null) {
            stmt.setString(6, imageName);
            stmt.setString(7, srCode);
        } else {
            stmt.setString(6, srCode);
        }

        stmt.executeUpdate();
        JOptionPane.showMessageDialog(null, "User Updated Successfully.", "Confirmation", JOptionPane.INFORMATION_MESSAGE);

     
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}


    
   
    private void searchStudent(int studentID) {
    try {
        Connection connection = Database.connect();
        String query = "SELECT * FROM userdetails WHERE id = ?"; // Assuming studentID is stored in 'id' column
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, studentID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            srCodeField.setText(rs.getString("sr_code"));
            nameField.setText(rs.getString("name"));
            emailField.setText(rs.getString("email"));

            if (rs.getString("gender").equalsIgnoreCase("Male")) {
                maleButton.setSelected(true);
                femaleButton.setSelected(false);
            } else {
                maleButton.setSelected(false);
                femaleButton.setSelected(true);
            }

            contactField.setText(rs.getString("contact"));
            addressField.setText(rs.getString("address"));
            uniqueReg = rs.getString("uniqueregid");

            String imageNameDB = rs.getString("imagename");
            existingImageName = (imageNameDB == null || imageNameDB.isEmpty()) ? null : imageNameDB;

            if (existingImageName != null) {
                String imagePath = BDUtility.getPath("images" + File.separator + existingImageName);
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    ImageIcon icon = new ImageIcon(imagePath);
                    Image image = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                    lblImage.setIcon(new ImageIcon(image));
                } else {
                    lblImage.setIcon(null);
                }
            } else {
                lblImage.setIcon(null);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Student not found.", "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error loading student details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
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
   
    
    private void chooseImage() {
        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setSize(600, 400);
        
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (JPG, PNG)", "jpg", "png");
        fileChooser.setFileFilter(filter);
        fileChooser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)){
                    selectedFile=fileChooser.getSelectedFile();
                    lblImage.setText("");
                    try{
                        originalImage = ImageIO.read(selectedFile);
                        
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
                        
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                    
                }
                dialog.dispose();
            }
        
    });
        
        dialog.add(fileChooser);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
       
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
    
    public static void main(String[] args) {
        int studentID = 2;
        new EditDetails(studentID);
    }
}
