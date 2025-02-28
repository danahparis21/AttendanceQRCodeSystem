
package forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
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
import utility.BDUtility;

/**
 *
 * @author 63945
 */
public class DeleteStudent extends JFrame {
    JTextField searchField;
    JTable userTable; // ✅ Declare at class level

   public DeleteStudent() {
    setTitle("Delete Student");
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
    JLabel header = new JLabel("DELETE STUDENT", SwingConstants.CENTER);
    header.setFont(new Font("SansSerif", Font.BOLD, 18));
    header.setBounds(400, 30, 450, 40);
    header.setForeground(Color.WHITE);
    backgroundPanel.add(header); // ✅ Add to background panel

    // Table setup
    DefaultTableModel tableModel = new DefaultTableModel(new String[]{
        "SR-Code", "Name", "Gender", "Email", "Contact", "Address", "Registration ID", "Image Name"}, 0);

    userTable = new JTable(tableModel); // ✅ Initialize properly
    JScrollPane tableScroll = new JScrollPane(userTable);
    tableScroll.setBounds(20, 100, 1200, 400);
    userTable.setAutoCreateRowSorter(true);
    styleTable(userTable);
    
    backgroundPanel.add(tableScroll); // ✅ Add to background panel

    // Add MouseListener to handle row clicks
    userTable.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            try {
                int dialogResult = JOptionPane.showConfirmDialog(null, 
                    "* User details\n* Images\n* QRCodes\n* Attendance\n\nAssociated with this user will be deleted.\n Are you sure you want to proceed?", 
                    "Confirmation", JOptionPane.YES_NO_OPTION);
                
                if (dialogResult == JOptionPane.YES_OPTION) {
                    int index = userTable.getSelectedRow();
                    
                    if (index == -1) {
                        JOptionPane.showMessageDialog(null, "Please select a student to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    TableModel model = userTable.getModel();
                    String email = model.getValueAt(index, 3).toString();

                    String imagePath = BDUtility.getPath("/images" + File.separator + email + ".jpg");
                    deleteFile(imagePath);
                    imagePath = BDUtility.getPath("/qrCodes" + File.separator + email + ".jpg");
                    deleteFile(imagePath);

                    Connection connection = Database.connect();
                    String attendanceDeleteQuery = "DELETE userattendance, userdetails FROM userdetails " +
                            "LEFT JOIN userattendance ON userattendance.userid = userdetails.userid " +
                            "WHERE userdetails.email=?";
                    
                    PreparedStatement stmt = connection.prepareStatement(attendanceDeleteQuery);
                    stmt.setString(1, email);
                    stmt.executeUpdate();
                    fetchUser(null);
                    
                    JOptionPane.showMessageDialog(null, "User deleted successfully.", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Deletion Canceled", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Something went wrong.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    });

    // Search Field
    JLabel searchLabel = new JLabel("Search:");
    searchLabel.setBounds(900, 50, 80, 30);
    searchLabel.setForeground(Color.WHITE);
    searchLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    backgroundPanel.add(searchLabel);

    searchField = new JTextField();
    searchField.setBounds(1000, 50, 200, 30);
    backgroundPanel.add(searchField);

    searchField.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                fetchUser(searchField.getText().trim()); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    // Fetch data initially
    try {
        fetchUser(null);
    } catch (Exception ex) {
        ex.printStackTrace();
    }

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
    
    private void deleteFile(String filepath) {
    File fileToDelete = new File(filepath);
    if (fileToDelete.exists()) {
        if (fileToDelete.delete()) {
            System.out.println("File Deleted Successfully: " + filepath);
        } else {
            System.err.println("Failed to delete the file: " + filepath);
        }
    } else {
        System.out.println("File does not exist: " + filepath);
    }
}

    
        public static void main(String[] args) {
        new DeleteStudent();
    }
}
