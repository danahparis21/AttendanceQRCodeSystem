
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
import utility.BDUtility;

public class ViewQr extends JFrame {
    JTextField searchField;
    JTable userTable; // ✅ Declare at class level
    JTable tblQrList;
    
    public ViewQr() {
        setTitle("View QR");
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
        JLabel header = new JLabel("VIEW QR", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBounds(400, 30, 450, 40);
        header.setForeground(Color.WHITE);
        backgroundPanel.add(header); // ✅ Add to background panel

        // IMAGE UPLOAD (JInternalFrame)
        JInternalFrame imageFrame = new JInternalFrame("QR Image", false, false, false, false);
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
            "Email"}, 0);

        tblQrList = new JTable(tableModel); // ✅ Initialize properly
        JScrollPane tableScroll = new JScrollPane(tblQrList);
        tableScroll.setBounds(20, 100, 780, 400);
        styleTable(tblQrList);
        backgroundPanel.add(tableScroll); // ✅ Add to background panel

        // Add MouseListener to handle row clicks
        tblQrList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int index = tblQrList.getSelectedRow();
                TableModel model = tblQrList.getModel();
                String name = model.getValueAt(index, 0).toString();
                ImageIcon icon = new ImageIcon(BDUtility.getPath("qrCodes" + File.separator + name));
                Image image = icon.getImage().getScaledInstance(400, 380, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(image);
                lblImage.setIcon(resizedIcon);
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
        DefaultTableModel tableModel = (DefaultTableModel) tblQrList.getModel();
        File directory = new File(BDUtility.getPath("/qrCodes"));
        File[] files = directory.listFiles();
        
        if(files != null){
            for(File file : files){
                tableModel.addRow(new Object[] {file.getName(), file.length()});
                
                
            }
        }
       
    }

    private void addLabelAndField(String labelText, int y, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setBounds(700, y, 120, 25);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(label);

        field.setBounds(800, y, 230, 30);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(field);
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
        new ViewQr();
    }
}
