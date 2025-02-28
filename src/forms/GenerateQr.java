/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forms;

import com.google.gson.Gson;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode; // ✅ Correct Import
import utility.BDUtility;

public class GenerateQr extends JFrame {
    JTextField searchField;
    JTable userTable; // ✅ Declare at class level
     ByteArrayOutputStream out = null;
        String email = null;

    public GenerateQr() {
        setTitle("Generate QR");
        setSize(1250, 600);
        setLayout(null);
        
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        // Background Panel with Image
        JPanel backgroundPanel = new JPanel() {
            private ImageIcon bgImage = new ImageIcon(getClass().getResource("/images/longbg.png")); // Ensure path is correct

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

        backgroundPanel.setBounds(0, 0, 1250, 600);
        backgroundPanel.setLayout(null);
        add(backgroundPanel); // Add background first

        // HEADER
        JLabel header = new JLabel("GENERATE QR", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBounds(400, 30, 450, 40);
        header.setForeground(Color.WHITE);
        backgroundPanel.add(header); // ✅ Add to background panel

        // IMAGE UPLOAD (JInternalFrame)
        JInternalFrame imageFrame = new JInternalFrame("Student QR", false, false, false, false);
        imageFrame.setBounds(800, 100, 400, 400);
        imageFrame.setLayout(null);
        backgroundPanel.add(imageFrame); // ✅ Add to background panel

        JLabel lblImage = new JLabel("No Image", SwingConstants.CENTER);
        lblImage.setBounds(0, 0, 400, 400);
        lblImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageFrame.add(lblImage);
        imageFrame.setVisible(true);

        // Table setup
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{
            "ID", "SR-Code", "Name", "Gender", "Email", "Contact", "Address", "Registration ID"}, 0);

        userTable = new JTable(tableModel); // ✅ Initialize properly
        JScrollPane tableScroll = new JScrollPane(userTable);
        tableScroll.setBounds(20, 100, 780, 400);
        styleTable(userTable);
        backgroundPanel.add(tableScroll); // ✅ Add to background panel

        // Add MouseListener to handle row clicks
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int index = userTable.getSelectedRow();
                TableModel model = userTable.getModel();
                String id = model.getValueAt(index, 0).toString();
                String sr_code = model.getValueAt(index, 1).toString();
                String name = model.getValueAt(index, 2).toString();
                email = model.getValueAt(index, 4).toString();
                String registrationId = model.getValueAt(index, 7).toString();

                Map<String, String> data = new HashMap<>();
                data.put("id", id);
                data.put("sr_code", sr_code);
                data.put("name", name);
                data.put("email", email);
                data.put("registrationId", registrationId);

                Gson gson = new Gson();
                String jsonData = gson.toJson(data);

                try {
                    out = QRCode.from(jsonData)
                        .withSize(400, 400) 
                        .to(ImageType.PNG)
                        .stream();

                    byte[] imageData = out.toByteArray(); // ✅ Correct variable assignment
                    ImageIcon icon = new ImageIcon(imageData);
                    lblImage.setIcon(icon);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // BUTTONS
        JButton saveQrButton = createStyledButton("Save QR", 130, 510);
        backgroundPanel.add(saveQrButton); // ✅ Add to background panel
        saveQrButton.addActionListener(e -> saveQr());

        JButton saveQrAtButton = createStyledButton("Save QR At", 380, 510);
        backgroundPanel.add(saveQrAtButton); // ✅ Add to background panel
        saveQrAtButton.addActionListener(e -> saveQrAt());

        try {
            fetchUser(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setVisible(true);
    }

    
    
    
       private void saveQr() {
    try {
        if (out == null) {
            JOptionPane.showMessageDialog(null, "No QR Generated!");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No user selected!");
            return;
        }

        String safeEmail = email.replaceAll("[^a-zA-Z0-9.-]", "_"); // Sanitize filename
        String defaultDir = BDUtility.getPath("qrCodes");
        File directory = new File(defaultDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File defaultFile = new File(directory, safeEmail + "_QR.jpg");
        System.out.println("Saving QR Code to: " + defaultFile.getAbsolutePath());

        try {
            java.nio.file.Files.write(defaultFile.toPath(), out.toByteArray());
            JOptionPane.showMessageDialog(null, "QR Code saved successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving QR Code.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Something went wrong.");
    }
}

private void saveQrAt() {
    try {
        if (out == null) {
            JOptionPane.showMessageDialog(null, "No QR Generated");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save QR Code at");

        String safeEmail = (email != null) ? email.replaceAll("[^a-zA-Z0-9.-]", "_") : "QR_Code";
        fileChooser.setSelectedFile(new File(safeEmail + "_QR.png"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Warn if the file already exists
            if (fileToSave.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(null, "File already exists. Overwrite?", "Warning", JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return; // Cancel save operation
                }
            }

            try {
                java.nio.file.Files.write(fileToSave.toPath(), out.toByteArray());
                JOptionPane.showMessageDialog(null, "QR code saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error saving QR", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Something went wrong.");
    }
}


    
    private JButton createStyledButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 180, 40);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(0x890418));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(0x890418), 2));
        button.setFocusPainted(false);
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(0x890418));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0x890418));
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }
    
    private void styleTable(JTable table) {
        // Table Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setBackground(new Color(0x393939)); // Dark Gray
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        // Table Body Styling
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(0x393939));
        table.setGridColor(new Color(0xD3D3D3)); // Light Gray Grid
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Selection Styling
        table.setSelectionBackground(new Color(0x393939));
        table.setSelectionForeground(Color.WHITE);

        // Borderless Look
        table.setBorder(BorderFactory.createEmptyBorder());
    }
    
     private void fetchUser(String searchText) {
        DefaultTableModel tableModel = (DefaultTableModel) userTable.getModel();
        tableModel.setRowCount(0);

        try (Connection connection = Database.connect()) {
            PreparedStatement pst;
            if (searchText == null || searchText.trim().isEmpty()) {
                pst = connection.prepareStatement("SELECT * FROM userdetails WHERE role = 'Student'");
            } else {
                pst = connection.prepareStatement("SELECT * FROM userdetails WHERE role = 'Student' AND (name LIKE ? OR email LIKE ?)");
                pst.setString(1, "%" + searchText + "%");
                pst.setString(2, "%" + searchText + "%");
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("sr_code"),
                    rs.getString("name"),
                    rs.getString("gender"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    rs.getString("address"),
                    rs.getString("uniqueregid"),
                    
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Something went wrong");
        }
    }
        
public static void main(String[] args) {
        new GenerateQr();
    }
}
