import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GateManagementSystem {

    JFrame frame;
    JTextField nameField, phoneField, purposeField, searchField;
    JLabel clockLabel;
    JTable table;
    DefaultTableModel model;

    Connection con;

    GateManagementSystem() {
        connectDB();

        frame = new JFrame("Gate Management System");
        frame.setSize(800, 500);
        frame.setLayout(new FlowLayout());

        nameField = new JTextField(10);
        phoneField = new JTextField(10);
        purposeField = new JTextField(10);
        searchField = new JTextField(10);

        JButton addBtn = new JButton("Add");
        JButton exitBtn = new JButton("Mark Exit");
        JButton deleteBtn = new JButton("Delete");
        JButton searchBtn = new JButton("Search");
        JButton exportBtn = new JButton("Export CSV");

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Phone", "Purpose", "Entry", "Exit"});
        table = new JTable(model);

        JScrollPane sp = new JScrollPane(table);

        clockLabel = new JLabel();
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            clockLabel.setText("Time: " + sdf.format(new Date()));
        });
        timer.start();

        frame.add(new JLabel("Name"));
        frame.add(nameField);

        frame.add(new JLabel("Phone"));
        frame.add(phoneField);

        frame.add(new JLabel("Purpose"));
        frame.add(purposeField);

        frame.add(addBtn);
        frame.add(exitBtn);
        frame.add(deleteBtn);

        frame.add(new JLabel("Search Phone"));
        frame.add(searchField);
        frame.add(searchBtn);

        frame.add(exportBtn);
        frame.add(clockLabel);
        frame.add(sp);

        addBtn.addActionListener(e -> addVisitor());
        exitBtn.addActionListener(e -> markExit());
        deleteBtn.addActionListener(e -> deleteVisitor());
        searchBtn.addActionListener(e -> searchVisitor());
        exportBtn.addActionListener(e -> exportCSV());

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loadData();
    }

    void connectDB() {
        try {
            String url = "jdbc:mysql://localhost:3306/gate_system";
            String user = "root";
            String password = "...";

            con = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Database Connected Successfully!");

        } catch (Exception e) {
            System.out.println("❌ Database Connection Failed!");
            e.printStackTrace();
        }
    }

    void addVisitor() {
        try {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String purpose = purposeField.getText();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Fill Name & Phone!");
                return;
            }

            String entryTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

            String query = "INSERT INTO visitors(name, phone, purpose, entry_time, exit_time) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, phone);
            pst.setString(3, purpose);
            pst.setString(4, entryTime);
            pst.setString(5, "Not Exited");

            pst.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Visitor Added!");
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadData() {
        try {
            model.setRowCount(0);

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM visitors");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("purpose"),
                        rs.getString("entry_time"),
                        rs.getString("exit_time")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void markExit() {
        try {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "Select row!");
                return;
            }

            int id = (int) model.getValueAt(row, 0);
            String exitTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

            String query = "UPDATE visitors SET exit_time=? WHERE id=?";
            PreparedStatement pst = con.prepareStatement(query);

            pst.setString(1, exitTime);
            pst.setInt(2, id);

            pst.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Exit Marked!");
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void searchVisitor() {
        try {
            model.setRowCount(0);

            String phone = searchField.getText();

            String query = "SELECT * FROM visitors WHERE phone=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, phone);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("purpose"),
                        rs.getString("entry_time"),
                        rs.getString("exit_time")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void exportCSV() {
        try {
            FileWriter writer = new FileWriter("visitors.csv");

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    writer.append(model.getValueAt(i, j).toString() + ",");
                }
                writer.append("\n");
            }

            writer.close();
            JOptionPane.showMessageDialog(frame, "Exported!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void deleteVisitor() {
        try {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "Select row!");
                return;
            }

            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Delete selected visitor?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String query = "DELETE FROM visitors WHERE id=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, id);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Row deleted!");
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GateManagementSystem();
    }
}
