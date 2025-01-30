import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ParkingGUI extends JFrame implements ActionListener {
    // Components
    private JButton employeeLoginBtn, employeeSignUpBtn;

    public ParkingGUI() {
        setTitle("Parking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Background Panel
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bgIcon = new ImageIcon("C:\\Programming\\Parking Management System\\Images\\bg.jpeg");
                g.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(null); // Set layout to null to position components manually
        add(bgPanel, BorderLayout.CENTER);

        // Logo
        ImageIcon logoIcon = new ImageIcon("C:\\Programming\\Parking Management System\\Images\\logo.png");
        int logoWidth = (int) (logoIcon.getIconWidth() * .90); // Adjusted logo size
        int logoHeight = (int) (logoIcon.getIconHeight() * .90); // Adjusted logo size
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setBounds(50, 50, logoWidth, logoHeight); // Adjusted logo position
        bgPanel.add(logoLabel);

        // Buttons
        employeeLoginBtn = new JButton("Employee Login");
        employeeSignUpBtn = new JButton("Employee Sign Up");

        employeeLoginBtn.addActionListener(this);
        employeeSignUpBtn.addActionListener(this);

        // Set button colors
        Color buttonColor = new Color(59, 89, 182);
        Color textColor = new Color(245, 245, 220);
        employeeLoginBtn.setBackground(buttonColor);
        employeeLoginBtn.setForeground(textColor);
        employeeSignUpBtn.setBackground(buttonColor);
        employeeSignUpBtn.setForeground(textColor);

        int buttonWidth = 200;
        int buttonHeight = 40;
        int buttonSpacing = 20;

        int startY = 50; // Starting Y position for buttons
        int startX = logoWidth + 100; // Starting X position for buttons

        employeeLoginBtn.setBounds(startX, startY, buttonWidth, buttonHeight);
        employeeSignUpBtn.setBounds(startX + buttonWidth + buttonSpacing, startY, buttonWidth, buttonHeight);

        bgPanel.add(employeeLoginBtn);
        bgPanel.add(employeeSignUpBtn);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == employeeLoginBtn) {
            // Open Employee Login Window
            ParkingSystem parkingSystem = new ParkingSystem();
            parkingSystem.setVisible(true);
        } else if (e.getSource() == employeeSignUpBtn) {
            // Open Employee Sign Up Window
            ParkingUsers parkingUsers = new ParkingUsers();
            parkingUsers.setVisible(true);
        }
    }
    
    public static void main(String[] args) {
        // Create and display the GUI
        SwingUtilities.invokeLater(() -> {
            ParkingGUI parkingGUI = new ParkingGUI();
            parkingGUI.setVisible(true);
        });
    }
}
