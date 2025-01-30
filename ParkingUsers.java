import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ParkingUsers extends JFrame implements ActionListener {
    @SuppressWarnings("unused")
    private JTextField usernameField, deleteUsernameField;
    @SuppressWarnings("unused")
    private JPasswordField passwordField, deletePasswordField;
    private JButton createUserButton, viewUserButton, deleteUserButton;
    private Connection connection;

    public ParkingUsers() {
        setTitle("Parking Users");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(passwordField, gbc);

        createUserButton = new JButton("Create User");
        createUserButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(createUserButton, gbc);

        viewUserButton = new JButton("View Users");
        viewUserButton.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(viewUserButton, gbc);

        deleteUserButton = new JButton("Delete User");
        deleteUserButton.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(deleteUserButton, gbc);

        setVisible(true);

        // Connect to the database
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@//LAPTOP-1I5GIM6R:1521/xepdb1", "system", "a");
            System.out.println("Connection successful");
            createTable(); // Create table if it doesn't exist
        } catch (Exception ee) {
            System.out.println("Connection failed: " + ee);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createUserButton) {
            createUser();
        } else if (e.getSource() == viewUserButton) {
            viewUsers();
        } else if (e.getSource() == deleteUserButton) {
            deleteUser();
        }
    }

    private void createUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO ParkingUsers (Username, Password) VALUES (?, ?)");
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this, "User created successfully");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error creating user: " + ex.getMessage());
        }
    }

    private void viewUsers() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM ParkingUsers");

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columns = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columns[i - 1] = metaData.getColumnName(i);
            }

            Object[][] data = new Object[100][columnCount]; // assuming a maximum of 100 users
            int rowCount = 0;
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    data[rowCount][i - 1] = resultSet.getObject(i);
                }
                rowCount++;
            }

            Object[][] dataArray = new Object[rowCount][columnCount];
            System.arraycopy(data, 0, dataArray, 0, rowCount);

            JTable table = new JTable(dataArray, columns);
            JScrollPane scrollPane = new JScrollPane(table);
            JFrame frame = new JFrame("View Users");
            frame.add(scrollPane);
            frame.setSize(800, 400);
            frame.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error viewing users: " + ex.getMessage());
        }
    }

    private void deleteUser() {
        String usernameToDelete = JOptionPane.showInputDialog(this, "Enter Username to delete:");
        String passwordToDelete = JOptionPane.showInputDialog(this, "Enter password to delete:");

        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM ParkingUsers WHERE Username = ? AND Password = ?");
            statement.setString(1, usernameToDelete);
            statement.setString(2, passwordToDelete);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "User with Username " + usernameToDelete + " deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage());
        }
    }

    private void createTable() throws SQLException {
        Statement statement = connection.createStatement();
        // Check if the table exists
        ResultSet resultSet = connection.getMetaData().getTables(null, null, "ParkingUsers", null);
        if (!resultSet.next()) {
            // Create table if it doesn't exist
            String createTableQuery = "CREATE TABLE ParkingUsers (Username VARCHAR(255) PRIMARY KEY, Password VARCHAR(255))";
            statement.executeUpdate(createTableQuery);
        }
    }

    public static void main(String[] args) {
        new ParkingUsers();
    }
}