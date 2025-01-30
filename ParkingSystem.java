import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class ParkingSystem extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private int totalSlots, availableSlots;
    private Connection connection;

    private JButton parkButton, removeButton, viewButton, searchButton, slotSearchButton;
    private JTextField licensePlateField, slotNumberField, searchPlateField, searchSlotField, hoursField, minutesField;
    private JRadioButton amButton, pmButton;
    private JLabel slotsLabel, licensePlateLabel, slotNumberLabel, searchPlateLabel, searchSlotLabel, timeLabel, clockLabel;
    private DefaultTableModel tableModel;
    private JTextArea displayArea;

    private static final String DB_URL = "jdbc:oracle:thin:@//LAPTOP-1I5GIM6R:1521/xepdb1";
    private static final String USER = "system";
    private static final String PASSWORD = "a";
    private static final String TABLE_NAME = "parked_cars";

    private static final int PARKING_RATE_PER_HOUR = 20;

    private JFrame loginFrame;

    public ParkingSystem() {
        loginFrame = new JFrame("Admin Login");
        loginFrame.setSize(300, 200);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new GridLayout(5, 2));

        JLabel usernameLabel = new JLabel("Username:");
        loginFrame.add(usernameLabel);

        JTextField usernameField = new JTextField(10);
        loginFrame.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        loginFrame.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(10);
        loginFrame.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (checkLogin(username, password)) {
                loginFrame.setVisible(false);
                initializeParkingSystem();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid username or password.");
            }
        });
        loginFrame.add(loginButton);

        loginFrame.setVisible(true);
    }

    private boolean checkLogin(String username, String password) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM ParkingUsers WHERE Username=? AND Password=?");
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initializeParkingSystem() {
        totalSlots = 100;
        availableSlots = totalSlots;

        setTitle("Parking System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel bottomPanel = new JPanel(new GridLayout(3, 3, 10, 0));

        licensePlateLabel = new JLabel("License Plate:");
        topPanel.add(licensePlateLabel);

        licensePlateField = new JTextField(10);
        topPanel.add(licensePlateField);

        slotNumberLabel = new JLabel("Slot Number:");
        topPanel.add(slotNumberLabel);

        slotNumberField = new JTextField(10);
        topPanel.add(slotNumberField);

        timeLabel = new JLabel("Time:");
        topPanel.add(timeLabel);

        hoursField = new JTextField(2);
        topPanel.add(hoursField);

        JLabel colonLabel = new JLabel(":");
        topPanel.add(colonLabel);

        minutesField = new JTextField(2);
        topPanel.add(minutesField);

        ButtonGroup amPmGroup = new ButtonGroup();
        amButton = new JRadioButton("AM");
        pmButton = new JRadioButton("PM");
        amPmGroup.add(amButton);
        amPmGroup.add(pmButton);
        JPanel amPmPanel = new JPanel();
        amPmPanel.add(amButton);
        amPmPanel.add(pmButton);
        topPanel.add(amPmPanel);

        clockLabel = new JLabel("Clock:");
        topPanel.add(clockLabel);

        JLabel clockTimeLabel = new JLabel();
        topPanel.add(clockTimeLabel);

        Thread clockThread = new Thread(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            while (true) {
                clockTimeLabel.setText(sdf.format(new Date()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        clockThread.start();

        add(topPanel, BorderLayout.NORTH);

        JPanel slotsPanel = new JPanel(new GridLayout(10, 10));
        for (int i = 1; i <= totalSlots; i++) {
            JButton slotButton = new JButton(Integer.toString(i));
            slotButton.setBackground(Color.GREEN);
            slotButton.setOpaque(true);
            slotButton.setBorderPainted(false);
            slotsPanel.add(slotButton);
        }
        add(slotsPanel, BorderLayout.WEST);

        slotsLabel = new JLabel("Available Slots: " + availableSlots);
        add(slotsLabel, BorderLayout.CENTER);

        parkButton = new JButton("Park Car");
        parkButton.addActionListener(this);
        bottomPanel.add(parkButton);

        removeButton = new JButton("Remove Car");
        removeButton.addActionListener(this);
        bottomPanel.add(removeButton);

        viewButton = new JButton("View Parked Cars");
        viewButton.addActionListener(this);
        bottomPanel.add(viewButton);

        searchButton = new JButton("Search by License Plate");
        searchButton.addActionListener(this);
        bottomPanel.add(searchButton);

        searchPlateLabel = new JLabel("Enter License Plate:");
        bottomPanel.add(searchPlateLabel);

        searchPlateField = new JTextField(10);
        bottomPanel.add(searchPlateField);

        slotSearchButton = new JButton("Search by Slot Number");
        slotSearchButton.addActionListener(this);
        bottomPanel.add(slotSearchButton);

        searchSlotLabel = new JLabel("Enter Slot Number:");
        bottomPanel.add(searchSlotLabel);

        searchSlotField = new JTextField(10);
        bottomPanel.add(searchSlotField);

        add(bottomPanel, BorderLayout.SOUTH);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        add(scrollPane, BorderLayout.CENTER);

        String[] columnNames = {"Slot Number", "License Plate", "Time", "Fare"};
        tableModel = new DefaultTableModel(columnNames, 0);

        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("Connection successful");
        } catch (SQLException ee) {
            System.out.println("Connection failed: " + ee);
            JOptionPane.showMessageDialog(this, "Connection failed: " + ee.getMessage());
            System.exit(1);
        }

        createTableIfNotExists();
        loadParkedCars();

        setVisible(true);
    }

    public static void main(String[] args) {
        new ParkingSystem();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == parkButton) {
            parkCar();
        } else if (e.getSource() == removeButton) {
            removeCar();
        } else if (e.getSource() == viewButton) {
            viewParkedCars();
        } else if (e.getSource() == searchButton) {
            searchByLicensePlate();
        } else if (e.getSource() == slotSearchButton) {
            searchBySlotNumber();
        }
    }

    private void parkCar() {
        String licensePlate = licensePlateField.getText();
        String slotNumberStr = slotNumberField.getText();
        String hoursStr = hoursField.getText();
        String minutesStr = minutesField.getText();
        String amPmStr = amButton.isSelected() ? "AM" : "PM";

        if (licensePlate.isEmpty() || slotNumberStr.isEmpty() || hoursStr.isEmpty() || minutesStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        int slotNumber;
        int hours;
        int minutes;
        try {
            slotNumber = Integer.parseInt(slotNumberStr);
            hours = Integer.parseInt(hoursStr);
            minutes = Integer.parseInt(minutesStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Slot number, hours, and minutes should be valid numbers.");
            return;
        }

        if (slotNumber < 1 || slotNumber > totalSlots) {
            JOptionPane.showMessageDialog(this, "Slot number should be between 1 and " + totalSlots + ".");
            return;
        }

        if (hours < 1 || hours > 12 || minutes < 0 || minutes > 59) {
            JOptionPane.showMessageDialog(this, "Invalid time format.");
            return;
        }

        String timeStr = String.format("%02d:%02d %s", hours, minutes, amPmStr);

        try {
            String insertQuery = "INSERT INTO " + TABLE_NAME + " (slot_number, license_plate, time) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setInt(1, slotNumber);
            preparedStatement.setString(2, licensePlate);
            preparedStatement.setString(3, timeStr);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error parking the car. Please try again.");
            return;
        }

        int hoursParked = hours % 12;
        int fare = hoursParked * PARKING_RATE_PER_HOUR;
        Object[] rowData = {slotNumber, licensePlate, timeStr, "Rs. " + fare};
        tableModel.addRow(rowData);

        availableSlots--;
        updateSlotsLabel();
        displayArea.append("Car with license plate " + licensePlate + " parked at slot " + slotNumber + ". Available slots: " + availableSlots + "\n");

        // Change color of the slot button to red since it's now occupied
        Component[] components = ((JPanel) getContentPane().getComponent(1)).getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton slotButton = (JButton) component;
                if (Integer.parseInt(slotButton.getText()) == slotNumber) {
                    slotButton.setBackground(Color.RED);
                    break;
                }
            }
        }
    }

    private void removeCar() {
        String slotNumberStr = slotNumberField.getText();
        if (slotNumberStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a slot number.");
            return;
        }

        int slotNumber;
        try {
            slotNumber = Integer.parseInt(slotNumberStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Slot number should be a valid number.");
            return;
        }

        int hoursParked = -1;
        String timeParked = "";

        try {
            String selectQuery = "SELECT time FROM " + TABLE_NAME + " WHERE slot_number=?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setInt(1, slotNumber);
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                timeParked = resultSet.getString("time");
                String[] timeSplit = timeParked.split(":");
                hoursParked = Integer.parseInt(timeSplit[0]);
            }
            selectStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving parked time. Please try again.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(2, 3));
        JTextField hoursField = new JTextField();
        JTextField minutesField = new JTextField();
        JComboBox<String> amPmBox = new JComboBox<>(new String[]{"AM", "PM"});
        panel.add(new JLabel("Hours:"));
        panel.add(hoursField);
        panel.add(new JPanel());
        panel.add(new JLabel("Minutes:"));
        panel.add(minutesField);
        panel.add(amPmBox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Remove Car", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String hoursStr = hoursField.getText();
            String minutesStr = minutesField.getText();
            String amPmStr = (String) amPmBox.getSelectedItem();

            int removedHours;
            @SuppressWarnings("unused")
            int removedMinutes;
            try {
                removedHours = Integer.parseInt(hoursStr);
                removedMinutes = Integer.parseInt(minutesStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input for hours or minutes.");
                return;
            }

            int totalHours = calculateTotalHours(hoursParked, removedHours, amPmStr, timeParked);

            int fare = totalHours * PARKING_RATE_PER_HOUR;

            try {
                String deleteQuery = "DELETE FROM " + TABLE_NAME + " WHERE slot_number=?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setInt(1, slotNumber);
                int deletedRows = preparedStatement.executeUpdate();
                preparedStatement.close();
                if (deletedRows == 0) {
                    JOptionPane.showMessageDialog(this, "No car parked at slot number " + slotNumber + ".");
                    return;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error removing the car. Please try again.");
                return;
            }

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if ((int) tableModel.getValueAt(i, 0) == slotNumber) {
                    tableModel.removeRow(i);
                    break;
                }
            }

            availableSlots++;
            updateSlotsLabel();
            displayArea.append("Car parked at slot number " + slotNumber + " removed. Fare: Rs. " + fare + ". Available slots: " + availableSlots + "\n");

            // Change color of the slot button to green since it's now available
            Component[] components = ((JPanel) getContentPane().getComponent(1)).getComponents();
            for (Component component : components) {
                if (component instanceof JButton) {
                    JButton slotButton = (JButton) component;
                    if (Integer.parseInt(slotButton.getText()) == slotNumber) {
                        slotButton.setBackground(Color.GREEN);
                        break;
                    }
                }
            }
        }
    }

    private int calculateTotalHours(int parkedHours, int removedHours, String amPmStr, String timeParked) {
        int totalHours = 0;
        if (amPmStr.equalsIgnoreCase("AM") && timeParked.endsWith("PM")) {
            totalHours = (12 - parkedHours) + removedHours;
        } else if (amPmStr.equalsIgnoreCase("PM") && timeParked.endsWith("AM")) {
            totalHours = (12 - parkedHours) + removedHours + 12;
        } else if (amPmStr.equalsIgnoreCase("AM") && timeParked.endsWith("AM")) {
            if (removedHours < parkedHours) {
                totalHours = parkedHours - removedHours;
            } else {
                totalHours = (12 - parkedHours) + removedHours;
            }
        } else if (amPmStr.equalsIgnoreCase("PM") && timeParked.endsWith("PM")) {
            if (removedHours < parkedHours) {
                totalHours = (12 - parkedHours) + removedHours + 12;
            } else {
                totalHours = removedHours - parkedHours;
            }
        }
        return totalHours;
    }

    private void viewParkedCars() {
        JFrame tableFrame = new JFrame("Parked Cars");
        tableFrame.setSize(400, 300);
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        tableFrame.add(scrollPane, BorderLayout.CENTER);

        tableFrame.setVisible(true);
    }

    private void searchByLicensePlate() {
        String searchPlate = searchPlateField.getText();
        if (searchPlate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a license plate to search.");
            return;
        }

        try {
            String searchQuery = "SELECT * FROM " + TABLE_NAME + " WHERE license_plate=?";
            PreparedStatement preparedStatement = connection.prepareStatement(searchQuery);
            preparedStatement.setString(1, searchPlate);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No car found with license plate " + searchPlate + ".");
            } else {
                StringBuilder result = new StringBuilder();
                while (resultSet.next()) {
                    result.append("Slot Number: ").append(resultSet.getInt("slot_number")).append(", Time: ").append(resultSet.getString("time")).append("\n");
                }
                JOptionPane.showMessageDialog(this, result.toString());
            }
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching for license plate. Please try again.");
        }
    }

    private void searchBySlotNumber() {
        String searchSlotStr = searchSlotField.getText();
        if (searchSlotStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a slot number to search.");
            return;
        }

        int searchSlot;
        try {
            searchSlot = Integer.parseInt(searchSlotStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Slot number should be a valid number.");
            return;
        }

        try {
            String searchQuery = "SELECT * FROM " + TABLE_NAME + " WHERE slot_number=?";
            PreparedStatement preparedStatement = connection.prepareStatement(searchQuery);
            preparedStatement.setInt(1, searchSlot);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No car found at slot number " + searchSlot + ".");
            } else {
                StringBuilder result = new StringBuilder();
                while (resultSet.next()) {
                    result.append("License Plate: ").append(resultSet.getString("license_plate")).append(", Time: ").append(resultSet.getString("time")).append("\n");
                }
                JOptionPane.showMessageDialog(this, result.toString());
            }
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching for slot number. Please try again.");
        }
    }

    private void createTableIfNotExists() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, TABLE_NAME.toUpperCase(), null);
            if (!resultSet.next()) {
                String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (slot_number INT, license_plate VARCHAR(50), time VARCHAR(20))";
                Statement statement = connection.createStatement();
                statement.executeUpdate(createTableQuery);
                statement.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating table. Please try again.");
            System.exit(1);
        }
    }

    private void loadParkedCars() {
        try {
            String selectQuery = "SELECT * FROM " + TABLE_NAME;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectQuery);
            while (resultSet.next()) {
                int slotNumber = resultSet.getInt("slot_number");
                String licensePlate = resultSet.getString("license_plate");
                String time = resultSet.getString("time");
                Object[] rowData = {slotNumber, licensePlate, time, ""};
                tableModel.addRow(rowData);

                // Change color of the slot button to red since it's occupied
                Component[] components = ((JPanel) getContentPane().getComponent(1)).getComponents();
                for (Component component : components) {
                    if (component instanceof JButton) {
                        JButton slotButton = (JButton) component;
                        if (Integer.parseInt(slotButton.getText()) == slotNumber) {
                            slotButton.setBackground(Color.RED);
                            break;
                        }
                    }
                }
            }
            statement.close();
            availableSlots -= tableModel.getRowCount();
            updateSlotsLabel();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading parked cars. Please try again.");
            System.exit(1);
        }
    }

    private void updateSlotsLabel() {
        slotsLabel.setText("Available Slots: " + availableSlots);
    }
}
