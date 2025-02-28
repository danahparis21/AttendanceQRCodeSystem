
package forms;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import static java.lang.String.format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import utility.BDUtility;
import java.sql.Connection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class MarkAttendance extends JFrame implements Runnable, ThreadFactory {
    private JTable bookTable;
    JLabel lbltimer;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, refreshButton, searchButton;
    private JTextField searchField;
    private JComboBox<String> searchFilter;
    private Connection connection;
    JPanel webCamPanel;
    JLabel lblImage, lblName, lblCheckInCheckOut, lblSrCode;
    
    
    private WebcamPanel panel = null;
    private Webcam webcam = null;
    private ExecutorService executor = Executors.newSingleThreadExecutor(this);
    private volatile boolean running = true;
    
    Map<String,String> resultMap = new HashMap<String,String>();
 
    // Constructor
    public MarkAttendance() {
        setTitle("Mark Attendance");
        setSize(1920, 1080);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        // Background Panel with Image
        JPanel backgroundPanel = new JPanel() {
            private ImageIcon bgImage = new ImageIcon(getClass().getResource("/images/scannerbg2.png"));

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

        backgroundPanel.setBounds(0, 0, 1920, 1080);
        backgroundPanel.setLayout(null);
        add(backgroundPanel);

        // HEADER
        JLabel header = new JLabel("MARK ATTENDANCE", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBounds(660, 40, 300, 30);
        header.setForeground(Color.WHITE);
        backgroundPanel.add(header);

        lblImage = new JLabel("No Image", SwingConstants.CENTER);
        lblImage.setBounds(780, 100, 285, 285);
        lblImage.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        backgroundPanel.add(lblImage);

        webCamPanel = new JPanel();
        webCamPanel.setBounds(80, 100, 689, 518);
        webCamPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        backgroundPanel.add(webCamPanel);

        JLabel lbldate = new JLabel("Date");
        lbldate.setBounds(1100, 100, 120, 25);
        lbldate.setFont(new Font("SansSerif", Font.BOLD, 16));
        backgroundPanel.add(lbldate);

        JLabel lbltime = new JLabel("Time");
        lbltime.setBounds(1200, 100, 120, 25);
        lbltime.setFont(new Font("SansSerif", Font.BOLD, 16));
        backgroundPanel.add(lbltime);

        lbltimer = new JLabel("");
        lbltimer.setBounds(1100, 150, 300, 30);
        lbltimer.setFont(new Font("SansSerif", Font.BOLD, 20));
        backgroundPanel.add(lbltimer);

        lblName = new JLabel("Name");
        lblName.setBounds(800, 450, 242, 37);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 20));
        backgroundPanel.add(lblName);

        lblSrCode = new JLabel("SR Code");
        lblSrCode.setBounds(800, 490, 242, 37);
        lblSrCode.setFont(new Font("SansSerif", Font.BOLD, 20));
        backgroundPanel.add(lblSrCode);

        lblCheckInCheckOut = new JLabel("Check-In/Check-Out Time");
        lblCheckInCheckOut.setBounds(800, 550, 242, 37);
        lblCheckInCheckOut.setFont(new Font("SansSerif", Font.BOLD, 20));
        backgroundPanel.add(lblCheckInCheckOut);

        Timer timer = new Timer(1, e -> updateTimer());
        timer.start();
        initWebcam();

        addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            stopWebcam();
            System.out.println("Window is closing, webcam stopped.");
        }
    });


        setVisible(true);
    }
    
    private void stopWebcam() {
        System.out.println("Stopping webcam...");
        running = false;  // Stop the loop

        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            System.out.println("Webcam closed.");
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            System.out.println("Executor shutdown.");
        }
    }

     

    private void updateTimer(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        lbltimer.setText(simpleDateFormat.format(new Date()));
        
    }
    
    
    
@Override
public void run() {
    while (running) {  // Check "running" condition
        try {
            Thread.sleep(1000);
            if (!running) break;  // Exit immediately if running is false
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        try {
            if (!running) {  
                System.out.println("Stopping QR code scanner...");
                break;
            }

            if (webcam == null || !webcam.isOpen()) {
                System.out.println("Webcam is not open. Stopping loop.");
                break;  // Exit loop if the webcam is closed
            }

            BufferedImage image = webcam.getImage();
            if (image == null) {
                System.out.println("No image detected. Waiting for a QR code...");
                continue;
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = null;
            try {
                result = new MultiFormatReader().decode(bitmap);
            } catch (NotFoundException ex) {
                continue;  // No QR code found, continue scanning
            }

            if (result != null) {
                String jsonString = result.getText();
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {}.getType();
                resultMap = gson.fromJson(jsonString, type);

                if (resultMap.get("email") != null) {
                    String finalPath = BDUtility.getPath("images\\" + resultMap.get("email") + ".jpg");
                    CircularImageFrame(finalPath);

                    SwingUtilities.invokeLater(() -> {
                        MarkAttendance.this.setState(JFrame.NORMAL);
                        MarkAttendance.this.setVisible(true);
                        MarkAttendance.this.toFront();
                    });
                } else {
                    System.out.println("Warning: sr_code is null or empty!");
                }
            }

        } catch (Exception ex) {
            System.err.println("Error processing QR code: " + ex.getMessage());
        }
    }

    System.out.println("QR code scanning loop stopped.");
}




    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "My Thread");
        t.setDaemon(true);
        return t;
        
    }

        private void initWebcam() {
    webcam = Webcam.getDefault();
    if (webcam != null) {
        Dimension[] resolutions = webcam.getViewSizes();
        Dimension maxResolution = resolutions[resolutions.length - 1];

        webcam.setViewSize(maxResolution);
        webcam.open();

        panel = new WebcamPanel(webcam);
        panel.setPreferredSize(maxResolution);
        panel.setFPSDisplayed(true);

        if (webCamPanel != null) {
            webCamPanel.add(panel);
            webCamPanel.revalidate();
            webCamPanel.repaint();
        } else {
            System.out.println("webCamPanel is null!");
        }
        executor.execute(this);
    } else {
        System.out.println("Webcam not found.");
    }
}
        
       
        
        private BufferedImage imagee = null;
        private void CircularImageFrame(String imagePath) {
        try{
            Connection connection = Database.connect();
            java.sql.Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("Select * from userdetails where email = '"+resultMap.get("email")+"';");
            if(!rs.next()){
                showPopUpForCertainDuration("User is not Registered or Deleted", "Invalid QR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            imagee = null;
            File imageFile  = new File(imagePath);
            if(imageFile.exists()){
                try{
                    imagee = ImageIO.read(new File(imagePath));
                    imagee = createCircularImage(imagee);
                    ImageIcon icon = new ImageIcon(imagee);
                    lblImage.setIcon(icon);

                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }else{
                BufferedImage imageeee = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = imageeee.createGraphics();
                
                g2d.setColor(Color.GRAY);
                g2d.fillOval(25, 25, 250, 250);
                g2d.setFont(new Font("Serif",Font.BOLD, 250));
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(resultMap.get("name").charAt(0)), 75, 225 );
                
                g2d.dispose();
                
                ImageIcon imageIconn = new ImageIcon(imageeee);
                lblImage.setIcon(imageIconn);
//                this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                this.pack();
//                this.setLocationRelativeTo(null);
//                this.setVisible(true);
            }
            
            lblName.setHorizontalAlignment(JLabel.CENTER);
            lblName.setText(resultMap.get("name"));
            
            String srCodeStr = resultMap.get("sr_code");
            if (srCodeStr != null && !srCodeStr.isEmpty()) {
                lblSrCode.setText(srCodeStr);
            } else {
                lblSrCode.setText("N/A");  // Display "N/A" instead of null
                System.out.println("Warning: sr_code is null or empty!");
            }

            if(!checkInCheckOut()){
                return;
                
            }
            
        }catch(Exception ex){
            ex.printStackTrace();
            
        }
    }
        
        private void showPopUpForCertainDuration(String popUpMessage, String popUpHeader, Integer iconId) throws HeadlessException {
            final JOptionPane optionPane = new JOptionPane(popUpMessage, iconId);
            final JDialog dialog = optionPane.createDialog(popUpHeader);
            dialog.setModalityType(Dialog.ModalityType.MODELESS); // Non-blocking
            dialog.setAlwaysOnTop(true); // Keeps it on top
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Ensures cleanup

            // Ensure main window remains visible
            SwingUtilities.invokeLater(() -> {
                //MarkAttendance.this.setState(JFrame.NORMAL);
                MarkAttendance.this.setVisible(true);
                MarkAttendance.this.toFront();
                MarkAttendance.this.requestFocus();
            });

            Timer timer = new Timer(5000, e -> {
                dialog.dispose();
                clearUserDetails();
            });

            timer.setRepeats(false);
            timer.start();
            dialog.setVisible(true);
        }

        
        private void clearUserDetails() {
           lblCheckInCheckOut .setText("");
           lblCheckInCheckOut.setBackground(null);
           lblCheckInCheckOut.setForeground(null);
           lblCheckInCheckOut.setOpaque(false);
           lblName.setText("");
          

           lblSrCode.setText("");
           
           lblImage.setIcon(null);
           
           
        }
        
        private BufferedImage createCircularImage(BufferedImage image) {
            int diameter = 285;
            BufferedImage resizedImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image, 0,0, diameter, diameter ,null);
            g2.dispose();
            
            BufferedImage circularImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            g2 = circularImage.createGraphics();
            Ellipse2D.Double circle = new Ellipse2D.Double (0, 0, diameter, diameter);
            g2.setClip(circle);
            g2.drawImage(resizedImage, 0,0, null);
            g2.dispose();
            return circularImage;
            
          }

     

    private boolean checkInCheckOut() throws HeadlessException, SQLException {
        String popUpHeader = null;
        String popUpMessage = null;
        Color color = null;
        
        Connection connection =Database.connect();
        java.sql.Statement st = connection.createStatement();
        
        LocalDate currentDate  = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String userIdStr = resultMap.get("id");

        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            System.out.println("Error: User ID is null or empty!");
            showPopUpForCertainDuration("Error: Invalid User ID", "Check-In Error", JOptionPane.ERROR_MESSAGE);
            return false; // Stop execution to avoid an error
        }

        int userId = 0; // Default value
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Error: Cannot parse User ID: " + userIdStr);
            showPopUpForCertainDuration("Error: Invalid User ID format", "Check-In Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Use the safe userId in SQL query
        ResultSet rs = st.executeQuery("Select * from userattendance where date = '" + currentDate.format(dateFormatter) + "' and userid=" + userId + ";");

        if(rs.next()){
            String checkOutDateTime = rs.getString(5);
            if(checkOutDateTime != null){
                popUpMessage = "Already Checked Out for the Day.";
                popUpHeader =  "Invalid Check Out";
                showPopUpForCertainDuration(popUpMessage, popUpHeader, JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            String checkInDateTime = rs.getString(4);
            LocalDateTime  checkInLocalDateTime = LocalDateTime.parse(checkInDateTime, dateTimeFormatter);
            Duration duration = Duration.between(checkInLocalDateTime, currentDateTime);
            
            long hours = duration.toHours();
            long minutes = duration.minusHours(hours).toMinutes();
            long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
            
            if(!(hours > 0 || (hours == 0 && minutes >= 3))){
                long remainingMinutes = 3-minutes;
                long remainingSeconds = 60-seconds;
                
                popUpMessage = String.format("You attended the class less than 3 minutes ago. \n You can check out after : %d minutes and %d seconds", remainingMinutes, remainingSeconds);
                popUpHeader = "Duration Warning";
                showPopUpForCertainDuration(popUpMessage, popUpHeader, JOptionPane.WARNING_MESSAGE);
                return false;
    
            }
            
            String updateQuery = "update userattendance set checkout = ?, workduration=? where date = ? and userid=?";
           
           PreparedStatement stmt = connection.prepareStatement(updateQuery);
           stmt.setString(1,currentDateTime.format(dateTimeFormatter));
           stmt.setString(2, ""+hours+" Hours and "+minutes + " Minutes");
           stmt.setString(3,currentDate.format(dateFormatter));
           stmt.setString(4,resultMap.get("id"));
           
           stmt.executeUpdate();
           popUpHeader  = "CheckOut";
           popUpMessage = "Checked Out at "+currentDateTime.format(dateTimeFormatter) + "\nWork Duration "+ hours + " Hours and " + minutes + " Minutes";
           color = Color.RED;

            
        }else{
            String insertQuery = "INSERT INTO userattendance (userid, date, checkin) VALUES (?,?,?)";
            PreparedStatement stmt = connection.prepareStatement(insertQuery);
            stmt.setString(1, resultMap.get("id"));
            stmt.setString(2, currentDate.format(dateFormatter));
            stmt.setString(3, currentDateTime.format(dateTimeFormatter));
            stmt.executeUpdate();
            popUpHeader  = "Check In";
           popUpMessage = "Checked In at "+currentDateTime.format(dateTimeFormatter);
           color = Color.GREEN;
        }
        
        lblCheckInCheckOut.setHorizontalAlignment(JLabel.CENTER);
        lblCheckInCheckOut.setText(popUpHeader);
        lblCheckInCheckOut.setForeground(color);
        lblCheckInCheckOut.setBackground(Color.DARK_GRAY);
        lblCheckInCheckOut.setOpaque(true);
        showPopUpForCertainDuration(popUpMessage, popUpHeader, JOptionPane.INFORMATION_MESSAGE);
        return true;

   }
    
    
    @Override
    public void paint(Graphics g){
        super.paint(g);
        if(imagee != null){
            g.drawImage(imagee, 0,0, null);
            
        }
    }

    
public static void main(String[] args) {
        new MarkAttendance();
    }
    

    
}
