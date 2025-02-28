package forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.sql.*;
import java.util.Objects;
import javax.swing.table.TableModel;
import utility.BDUtility;

public class ViewStudents extends JFrame {
    JTextField searchField;
    JTable userTable; // ✅ Declare at class level

  public ViewStudents() {
    setTitle("Register Students");
    setSize(1250, 600);
    setLayout(null);
    
    setLocationRelativeTo(null);
    
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
    JLabel header = new JLabel("VIEW STUDENTS", SwingConstants.CENTER);
    header.setFont(new Font("SansSerif", Font.BOLD, 18));
    header.setBounds(400, 40, 300, 30);
    header.setForeground(Color.WHITE);
    backgroundPanel.add(header); // ✅ Add to background panel

    // IMAGE UPLOAD (JInternalFrame)
    JInternalFrame imageFrame = new JInternalFrame("Student Image", false, false, false, false);
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
        "SR-Code", "Name", "Gender", "Email", "Contact", "Address", "Registration ID", "Image Name"}, 0);

    userTable = new JTable(tableModel);
    JScrollPane tableScroll = new JScrollPane(userTable);
    tableScroll.setBounds(20, 100, 780, 400);
    styleTable(userTable);
    backgroundPanel.add(tableScroll); // ✅ Add to background panel

    // MouseListener for selecting a row
    userTable.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                TableModel model = userTable.getModel();
                Object value = model.getValueAt(selectedRow, 7);
                String imageName = (value == null) ? null : value.toString();

                if (imageName != null && !imageName.trim().isEmpty()) {
                    String imagePath = BDUtility.getPath("/images" + File.separator + imageName);
                    File imageFile = new File(imagePath);

                    if (imageFile.exists()) {
                        ImageIcon icon = new ImageIcon(imagePath);
                        Image image = icon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
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
            }
        }
    });

    // SEARCH FIELD
    addLabelAndField("Search:", 60, searchField = new JTextField(), backgroundPanel);
    searchField.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent evt) {
            lblImage.setIcon(null);
            try {
                fetchUser(searchField.getText().trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    

    try {
        fetchUser(null);
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    setVisible(true);
}

    

    private void fetchUser(String searchText) {
        DefaultTableModel tableModel = (DefaultTableModel) userTable.getModel();
        tableModel.setRowCount(0);

        try (Connection connection = Database.connect()) {
            PreparedStatement pst;
            if (searchText == null || searchText.trim().isEmpty()) {
                pst = connection.prepareStatement("SELECT * FROM userdetails WHERE role = 'Student' ");
            } else {
                pst = connection.prepareStatement("SELECT * FROM userdetails WHERE role = 'Student' AND (name LIKE ? OR email LIKE ?)");
                pst.setString(1, "%" + searchText + "%");
                pst.setString(2, "%" + searchText + "%");
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("sr_code"),
                    rs.getString("name"),
                    rs.getString("gender"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    rs.getString("address"),
                    rs.getString("uniqueregid"),
                    rs.getString("imagename")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Something went wrong");
        }
    }

    private void addLabelAndField(String labelText, int y, JTextField field, JPanel panel) {
        JLabel label = new JLabel(labelText);
        label.setBounds(700, y, 120, 25);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        panel.add(label);

        field.setBounds(800, y, 230, 30);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(field);
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

    public static void main(String[] args) {
        new ViewStudents();
    }
}
