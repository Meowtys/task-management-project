import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class TaskManager extends JFrame {
    private TaskManagerLogic logic;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, searchField;
    private JButton deadlineButton;
    private LocalDate selectedDate;
    private JComboBox<String> priorityCombo;
    private int selectedTaskId = -1;

    public TaskManager() {
        logic = new TaskManagerLogic();
        selectedDate = LocalDate.now();
        setTitle("Task Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Task Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel);

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Task"));
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JPanel titlePriorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        titlePriorityPanel.add(new JLabel("Title:"));
        titleField = new JTextField(25);
        titlePriorityPanel.add(titleField);
        titlePriorityPanel.add(new JLabel("Priority:"));
        priorityCombo = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        priorityCombo.setSelectedItem("Low");
        titlePriorityPanel.add(priorityCombo);

        JButton addBtn = new JButton("Add Task");
        addBtn.addActionListener(e -> addTask());
        titlePriorityPanel.add(addBtn);
        inputPanel.add(titlePriorityPanel);

        JPanel deadlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        deadlinePanel.add(new JLabel("Deadline:"));
        deadlineButton = new JButton(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        deadlineButton.addActionListener(e -> showDatePicker());
        deadlinePanel.add(deadlineButton);
        inputPanel.add(deadlinePanel);

        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.add(inputPanel, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(25);
        searchPanel.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchTasks());
        searchPanel.add(searchBtn);

        JButton showAllBtn = new JButton("Show All");
        showAllBtn.addActionListener(e -> refreshTasks());
        searchPanel.add(showAllBtn);

        // Table Panel
        String[] columnNames = {"ID", "Title", "Priority", "Deadline", "Status", "Created"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getSelectionModel().addListSelectionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row != -1) {
                selectedTaskId = (int) tableModel.getValueAt(row, 0);
            }
        });

        taskTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(taskTable);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Tasks"));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton markCompleteBtn = new JButton("Mark Complete");
        markCompleteBtn.addActionListener(e -> markComplete());
        buttonPanel.add(markCompleteBtn);

        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editTask());
        buttonPanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteTask());
        buttonPanel.add(deleteBtn);

        // Assemble main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(inputContainer, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
        refreshTasks();
    }

    private void addTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task title.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String priority = (String) priorityCombo.getSelectedItem();
        String deadline = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        logic.addTask(title, priority, deadline);
        titleField.setText("");
        refreshTasks();
        JOptionPane.showMessageDialog(this, "Task '" + title + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTasks() {
        tableModel.setRowCount(0);
        for (Task task : logic.getTasks()) {
            String status = task.completed ? "✓ Complete" : "○ Pending";
            tableModel.addRow(new Object[]{
                task.id,
                task.title,
                task.priority,
                task.deadline.isEmpty() ? "N/A" : task.deadline,
                status,
                task.created_at
            });
        }
    }

    private void markComplete() {
        if (selectedTaskId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        logic.markComplete(selectedTaskId);
        refreshTasks();
        JOptionPane.showMessageDialog(this, "Task status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editTask() {
        if (selectedTaskId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = logic.getTask(selectedTaskId);
        if (task == null) return;

        String newTitle = JOptionPane.showInputDialog(this, "New title:", task.title);
        if (newTitle == null) return;

        String newPriority = JOptionPane.showInputDialog(this, "New priority:", task.priority);
        String newDeadline = JOptionPane.showInputDialog(this, "New deadline:", task.deadline);

        logic.editTask(selectedTaskId, newTitle, newPriority, newDeadline);
        refreshTasks();
        JOptionPane.showMessageDialog(this, "Task updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteTask() {
        if (selectedTaskId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            logic.deleteTask(selectedTaskId);
            refreshTasks();
            JOptionPane.showMessageDialog(this, "Task deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchTasks() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search keyword.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        java.util.List<Task> results = logic.searchTasks(keyword);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tasks found matching '" + keyword + "'", "Search", JOptionPane.INFORMATION_MESSAGE);
            refreshTasks();
            return;
        }

        for (Task task : results) {
            String status = task.completed ? "✓ Complete" : "○ Pending";
            tableModel.addRow(new Object[]{
                task.id,
                task.title,
                task.priority,
                task.deadline.isEmpty() ? "N/A" : task.deadline,
                status,
                task.created_at
            });
        }
    }

    private void showDatePicker() {
        JDialog dateDialog = new JDialog(this, "Select Deadline Date", true);
        dateDialog.setSize(400, 300);
        dateDialog.setLocationRelativeTo(this);
        dateDialog.setLayout(new BorderLayout(10, 10));

        JPanel datePanel = new JPanel(new GridLayout(7, 7, 5, 5));
        datePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel monthYearLabel = new JLabel();
        updateMonthYearLabel(monthYearLabel, selectedDate);

        JButton prevBtn = new JButton("< Previous");
        JButton nextBtn = new JButton("Next >");

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        navPanel.add(prevBtn);
        navPanel.add(monthYearLabel);
        navPanel.add(nextBtn);

        prevBtn.addActionListener(e -> {
            selectedDate = selectedDate.minusMonths(1);
            updateMonthYearLabel(monthYearLabel, selectedDate);
            updateDatePanel(datePanel, dateDialog);
        });

        nextBtn.addActionListener(e -> {
            selectedDate = selectedDate.plusMonths(1);
            updateMonthYearLabel(monthYearLabel, selectedDate);
            updateDatePanel(datePanel, dateDialog);
        });

        updateDatePanel(datePanel, dateDialog);

        dateDialog.add(navPanel, BorderLayout.NORTH);
        dateDialog.add(new JScrollPane(datePanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dateDialog.dispose());
        buttonPanel.add(cancelBtn);
        dateDialog.add(buttonPanel, BorderLayout.SOUTH);

        dateDialog.setVisible(true);
    }

    private void updateMonthYearLabel(JLabel label, LocalDate date) {
        label.setText(date.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        label.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void updateDatePanel(JPanel datePanel, JDialog dateDialog) {
        datePanel.removeAll();

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            JLabel dayLabel = new JLabel(day);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            datePanel.add(dayLabel);
        }

        LocalDate firstDay = selectedDate.withDayOfMonth(1);
        int daysInMonth = selectedDate.lengthOfMonth();
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < firstDayOfWeek; i++) {
            datePanel.add(new JLabel());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            LocalDate currentDate = selectedDate.withDayOfMonth(day);
            
            if (currentDate.equals(LocalDate.now())) {
                dayButton.setBackground(new Color(100, 150, 255));
                dayButton.setForeground(Color.WHITE);
            }
            
            final int finalDay = day;
            dayButton.addActionListener(e -> {
                selectedDate = selectedDate.withDayOfMonth(finalDay);
                deadlineButton.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                dateDialog.dispose();
            });
            
            datePanel.add(dayButton);
        }

        datePanel.revalidate();
        datePanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaskManager());
    }
}

class Task {
    int id;
    String title;
    String priority;
    String deadline;
    boolean completed;
    String created_at;

    public Task(int id, String title, String priority, String deadline) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.deadline = deadline;
        this.completed = false;
        this.created_at = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

class TaskManagerLogic {
    private final java.util.List<Task> tasks;
    private int taskIdCounter;
    private final String filename = "tasks.json";

    public TaskManagerLogic() {
        tasks = loadTasks();
        taskIdCounter = tasks.size() + 1;
    }

    private java.util.List<Task> loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            java.util.List<Task> loadedTasks = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("\"id\"")) {
                    Task task = parseTask(line);
                    if (task != null) loadedTasks.add(task);
                }
            }
            return loadedTasks;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private Task parseTask(String json) {
        // Simple JSON parsing for demonstration
        try {
            int id = Integer.parseInt(extractField(json, "id"));
            String title = extractField(json, "title");
            String priority = extractField(json, "priority");
            String deadline = extractField(json, "deadline");
            
            Task task = new Task(id, title, priority, deadline);
            String completedStr = extractField(json, "completed");
            task.completed = Boolean.parseBoolean(completedStr);
            task.created_at = extractField(json, "created_at");
            return task;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\":\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    public void addTask(String title, String priority, String deadline) {
        Task task = new Task(taskIdCounter, title, priority, deadline);
        tasks.add(task);
        taskIdCounter++;
        saveTasks();
    }

    public java.util.List<Task> getTasks() {
        return tasks;
    }

    public Task getTask(int id) {
        return tasks.stream().filter(t -> t.id == id).findFirst().orElse(null);
    }

    public void editTask(int id, String newTitle, String newPriority, String newDeadline) {
        Task task = getTask(id);
        if (task != null) {
            if (!newTitle.isEmpty()) task.title = newTitle;
            if (!newPriority.isEmpty()) task.priority = newPriority;
            if (!newDeadline.isEmpty()) task.deadline = newDeadline;
            saveTasks();
        }
    }

    public void deleteTask(int id) {
        tasks.removeIf(t -> t.id == id);
        saveTasks();
    }

    public void markComplete(int id) {
        Task task = getTask(id);
        if (task != null) {
            task.completed = !task.completed;
            saveTasks();
        }
    }

    public java.util.List<Task> searchTasks(String keyword) {
        return tasks.stream()
                .filter(t -> t.title.toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    private void saveTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("[");
            for (int i = 0; i < tasks.size(); i++) {
                Task t = tasks.get(i);
                writer.println("{");
                writer.println("  \"id\": " + t.id + ",");
                writer.println("  \"title\": \"" + escapeJson(t.title) + "\",");
                writer.println("  \"priority\": \"" + t.priority + "\",");
                writer.println("  \"deadline\": \"" + t.deadline + "\",");
                writer.println("  \"completed\": " + t.completed + ",");
                writer.println("  \"created_at\": \"" + t.created_at + "\"");
                writer.println("}" + (i < tasks.size() - 1 ? "," : ""));
            }
            writer.println("]");
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }

    private String escapeJson(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}