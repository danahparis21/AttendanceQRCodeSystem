package forms;

import java.awt.GridLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;

import java.time.LocalDate;
import java.util.*;

public class DataAnalytics extends JFrame {

    private DefaultCategoryDataset absentDataset;
    private DefaultCategoryDataset presentDataset;
    private ChartPanel absentPanel;
    private ChartPanel presentPanel;
    private JFreeChart absentChart;
    private JFreeChart presentChart;
    private Map<String, Integer> absentData;
    private Map<String, Integer> presentData;

    public DataAnalytics() {
        setTitle("Attendance Report");
        setSize(1050, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(2, 1));

        // Initialize datasets
        absentDataset = new DefaultCategoryDataset();
        presentDataset = new DefaultCategoryDataset();

        // Initialize attendance data
        absentData = new HashMap<>();
        presentData = new HashMap<>();

        // Create charts
        absentChart = createChart(absentDataset, "Absent Report", "Date", "Number of Absences");
        presentChart = createChart(presentDataset, "Present Report", "Date", "Number of Presents");

        // Add charts to panels
        absentPanel = new ChartPanel(absentChart);
        presentPanel = new ChartPanel(presentChart);

        add(absentPanel);
        add(presentPanel);

        // Load initial data
        loadInitialData();
        setVisible(true);

        // Simulate real-time updates (For testing)
        startRealTimeUpdates();
    }

    /**
     * Method to create a line chart
     */
    private JFreeChart createChart(DefaultCategoryDataset dataset, String title, String xAxisLabel, String yAxisLabel) {
        return ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataset,
                PlotOrientation.VERTICAL, false, true, false);
    }

    /**
     * Load initial attendance data
     */
    private void loadInitialData() {
        updateAttendance("2025-03-05", 2, 5);
        updateAttendance("2025-03-06", 0, 6);
        updateAttendance("2025-03-07", 1, 4);
    }

    /**
     * Updates attendance data dynamically, only adding records if they exist
     */
    public void updateAttendance(String date, int absentCount, int presentCount) {
        if (absentCount == 0 && presentCount == 0) {
            return; // Do not add empty records
        }

        absentData.put(date, absentData.getOrDefault(date, 0) + absentCount);
        presentData.put(date, presentData.getOrDefault(date, 0) + presentCount);

        // Update datasets
        absentDataset.setValue(absentData.get(date), "Attendance", date);
        presentDataset.setValue(presentData.get(date), "Attendance", date);

        // Refresh charts
        absentPanel.repaint();
        presentPanel.repaint();
    }

    /**
     * Simulates real-time updates (for testing)
     */
    private void startRealTimeUpdates() {
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> {
            String newDate = getNextDate();
            int newAbsent = new Random().nextInt(3);
            int newPresent = new Random().nextInt(10);

            updateAttendance(newDate, newAbsent, newPresent);
            System.out.println("Updated for: " + newDate + " (Absent: " + newAbsent + ", Present: " + newPresent + ")");
        });
        timer.start();
    }

    /**
     * Generates the next date based on the last recorded date
     */
    private String getNextDate() {
        if (presentData.isEmpty()) return "2025-03-08";

        // Sort dates correctly (as actual dates, not lexicographically)
        List<String> dates = new ArrayList<>(presentData.keySet());
        dates.sort(Comparator.comparing(s -> LocalDate.parse(s)));

        // Get last recorded date
        LocalDate lastDate = LocalDate.parse(dates.get(dates.size() - 1));

        return lastDate.plusDays(1).toString(); // Returns next date in "YYYY-MM-DD" format
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        new DataAnalytics();
    }
}
