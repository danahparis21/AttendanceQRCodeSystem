
package forms;

import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import java.awt.PopupMenu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import utility.BDUtility;

public class ViewAttendance extends JFrame {
    JTextField searchField;
    JTable userTable; // ✅ Declare at class level
    JLabel header, lblFrom, lblTo, lblPresent, lblPresentCount, lblAbsent, lblAbsentCount;
    JDateChooser startDateChooser,endDateChooser;
    JCheckBox chkContact, chkAddress, chkRegID;
    JButton resetButton;


    public ViewAttendance() {
        setTitle("View Attendance");
        setSize(1250, 600);
       
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        // Background Panel with Image
        JPanel backgroundPanel = new JPanel() {
            private ImageIcon bgImage = new ImageIcon(getClass().getResource("/images/longbg.png"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    System.out.println("Background image not found!");
                }
            }
        };

        backgroundPanel.setBounds(0, 0, 1250, 600);
        backgroundPanel.setLayout(null);
        add(backgroundPanel);

        // HEADER
        JLabel header = new JLabel("ATTENDANCE TRACK", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBounds(780, 10, 450, 40);
        header.setForeground(Color.WHITE);
        backgroundPanel.add(header);

        // Table setup
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{
            "ID", "Name", "Gender", "Email", "Contact", "Address", "Registration ID", "Image Name"}, 0);

        userTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(userTable);
        tableScroll.setBounds(450, 100, 780, 400);
        styleTable(userTable);
        backgroundPanel.add(tableScroll);

        // Label for Start Date
        lblFrom = new JLabel("On/From");
        lblFrom.setBounds(450, 40, 150, 20);
        lblFrom.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblFrom.setForeground(Color.WHITE);
        backgroundPanel.add(lblFrom);

        // First Date Chooser (Start Date)
        startDateChooser = new JDateChooser();
        startDateChooser.setBounds(450, 60, 150, 30);
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        backgroundPanel.add(startDateChooser);

        // Label for End Date
        lblTo = new JLabel("To");
        lblTo.setBounds(620, 40, 150, 20);
        lblTo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTo.setForeground(Color.WHITE);
        backgroundPanel.add(lblTo);

        // Second Date Chooser (End Date)
        endDateChooser = new JDateChooser();
        endDateChooser.setBounds(620, 60, 150, 30);
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        backgroundPanel.add(endDateChooser);

        startDateChooser.addPropertyChangeListener("date", e -> fetchUser());
        endDateChooser.addPropertyChangeListener("date", e -> fetchUser());

        addLabelAndField("Search:", 60, searchField = new JTextField(), backgroundPanel);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fetchUser();
            }
        });
       

        lblPresent = new JLabel("Present: ");
        lblPresent.setBounds(50, 120, 150, 20);
        lblPresent.setFont(new Font("SansSerif", Font.BOLD, 16));
        backgroundPanel.add(lblPresent);

        lblPresentCount = new JLabel("---------");
        lblPresentCount.setBounds(120, 120, 150, 20);
        lblPresentCount.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblPresentCount.setForeground(Color.GREEN);
        backgroundPanel.add(lblPresentCount);

        lblAbsent = new JLabel("Absent: ");
        lblAbsent.setBounds(50, 150, 150, 20);
        lblAbsent.setFont(new Font("SansSerif", Font.BOLD, 16));
        backgroundPanel.add(lblAbsent);

        lblAbsentCount = new JLabel("---------");
        lblAbsentCount.setBounds(120, 150, 150, 20);
        lblAbsentCount.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAbsentCount.setForeground(Color.RED);
        backgroundPanel.add(lblAbsentCount);

        chkContact = new JCheckBox("Contact");
        chkContact.setBounds(50, 200, 150, 25);
        chkContact.setFont(new Font("SansSerif", Font.BOLD, 14));
        chkContact.setBackground(Color.WHITE);
        backgroundPanel.add(chkContact);

        chkAddress = new JCheckBox("Address");
        chkAddress.setBounds(50, 230, 150, 25);
        chkAddress.setFont(new Font("SansSerif", Font.BOLD, 14));
        chkAddress.setBackground(Color.WHITE);
        backgroundPanel.add(chkAddress);

        chkRegID = new JCheckBox("Unique Reg ID");
        chkRegID.setBounds(50, 260, 150, 25);
        chkRegID.setFont(new Font("SansSerif", Font.BOLD, 14));
        chkRegID.setBackground(Color.WHITE);
        backgroundPanel.add(chkRegID);

        chkContact.addItemListener(e -> fetchUser());
        chkAddress.addItemListener(e -> fetchUser());
        chkRegID.addItemListener(e -> fetchUser());

        resetButton = createStyledButton("Reset Filters", 100, 300);
        backgroundPanel.add(resetButton);
        resetButton.addActionListener(e -> clearFields());

        fetchUser();
        setVisible(true);
    }

    private void fetchUser() {
        List<String> columns = new ArrayList<>(Arrays.asList(
                "SR-Code", "Name", "Gender", "Email", "Date", "CheckIn", "CheckOut", "Work Duration"
        ));
        
        String searchText = searchField.getText().toString();
        Date fromDateFromCal = startDateChooser.getDate();
        LocalDate fromDate = null;
        if(fromDateFromCal != null){
            fromDate = fromDateFromCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
        }
        
        Date toDateFromCal = endDateChooser.getDate();
        LocalDate toDate = null;
        if(toDateFromCal != null){
            toDate = toDateFromCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        
        Long daysBetween = null;
        if(fromDate != null && toDate != null){
            daysBetween = countWeekdays(fromDate,toDate);   
            
        }
        Boolean contactIncluded = chkContact.isSelected();
        Boolean addressIncluded = chkAddress.isSelected();
        Boolean uniqueRegIdIncluded = chkRegID.isSelected();
        
        String sqlQuery = "SELECT ud.sr_code, ud.name, ud.gender, ud.email,  ua.date, ua.checkin, ua.checkout, ua.workduration";
        if(contactIncluded){
            columns.add("Contact");
            sqlQuery+=", ud.contact";
        }
        
        if(addressIncluded){
            columns.add("Address");
            sqlQuery+=", ud.address";
        }
        
        if(uniqueRegIdIncluded){
            columns.add("Unique Reg ID");
            sqlQuery+=", ud.uniqueregid";
        }
        
        sqlQuery += " FROM userdetails AS ud INNER JOIN userattendance AS ua ON ud.id = ua.userid ";
        if(searchText != null) {
            sqlQuery += " where (ud.name like '%" +searchText+"%' or ud.email like '%" + searchText + "%') ";
            
            if(fromDate != null && toDate != null){
                sqlQuery += " AND ua.date BETWEEN '" + fromDate + "' AND '" + toDate + "'";
            } else if (fromDate != null){
                sqlQuery += " and ua.date = '" + fromDate +"'" ;
            }
            
        } else{
            if(fromDate != null && toDate != null){
                sqlQuery += " where ua.date BETWEEN '" + fromDate + "' AND '" + toDate + "'";
            } else if (fromDate != null){
                sqlQuery += " where ua.date = '" + fromDate +"'" ;
            }
        }
        
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columns.toArray());
        userTable.setModel(model);
        
        try{
            Connection con = Database.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sqlQuery);
            Long presentCount = 0l;
            Long absentCount = 0l;
            Set<String> emailList = new HashSet<>();
            while(rs.next()){
                List<Object> row = new ArrayList<>();
                row.add(rs.getString("sr_code"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("email"));
                emailList.add(rs.getString("email"));
                row.add(rs.getString("date"));
                row.add(rs.getString("checkin"));
                row.add(rs.getString("checkout"));
                row.add(rs.getString("workduration"));
                
                if(contactIncluded){
                    row.add(rs.getString("contact"));
                }
                if(addressIncluded){
                    row.add(rs.getString("address"));
                }
                if(uniqueRegIdIncluded){
                    row.add(rs.getString("uniqueregid"));
                }
                
                if(rs.getString("checkout") == null){
                    absentCount++;
                }
                else{
                    presentCount++;
                }
                
                model.addRow(row.toArray());
            }
            
            if(emailList.size () == 1){
               lblPresent.setVisible(true);
               lblAbsent.setVisible(true);
               lblPresentCount.setVisible(true);
               lblAbsentCount.setVisible(true);
               lblPresentCount.setText(presentCount.toString());
               if(daysBetween != null && daysBetween > 0){
                   absentCount = daysBetween - presentCount;
               }
               lblAbsentCount.setText(absentCount.toString());
            }else{
                lblPresent.setVisible(false);
               lblAbsent.setVisible(false);
               lblPresentCount.setVisible(false);
               lblAbsentCount.setVisible(false);
            }
            
        }catch(Exception ex){
           // JOptionPane.showMessageDialog(null, "Something went wrong.");
           ex.printStackTrace();
            
        }
        
    }
    
     private Long countWeekdays(LocalDate start, LocalDate end) {
         long count = 0;
         LocalDate date = start;
         while(date.isBefore(end)|| date.equals(end)){
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY || date.getDayOfWeek() != DayOfWeek.THURSDAY) {
                count++; // Count the valid weekdays
             }
            date = date.plusDays(1);
         }
         return count;
     }
    
     private void clearFields() {
        searchField.setText("");
        startDateChooser.setDate(null);
        endDateChooser.setDate(null);
        lblPresentCount.setText("------");
        lblAbsentCount.setText("------");
        
        chkContact.setSelected(false);
        chkAddress.setSelected(false);
        chkRegID.setSelected(false);
        
        fetchUser();
        
        
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
    
     private void addLabelAndField(String labelText, int y, JTextField field, JPanel panel) {
        JLabel label = new JLabel(labelText);
        label.setBounds(840, y, 120, 25);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        panel.add(label);

        field.setBounds(900, y, 230, 30);
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
        new ViewAttendance();
    }

   
}
