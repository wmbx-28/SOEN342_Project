package com.taskmanager;

import com.taskmanager.db.InMemoryRepository;
import com.taskmanager.models.Task;
import com.taskmanager.models.TaskStatus;
import com.taskmanager.services.CsvService;
import com.taskmanager.services.TaskService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize system
        InMemoryRepository repository = new InMemoryRepository("data/task-manager-data.json");
        TaskService taskService = new TaskService(repository);
        CsvService csvService = new CsvService(repository, taskService);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("|| Personal Task Management System PoC ||");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.println("\n Enter choice: ");
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        handleImport(scanner, csvService);
                        break;
                    case "2":
                        handleExport(scanner, csvService);
                        break;
                    case "3":
                        handleSearchAndView(scanner, taskService);
                        break;
                    case "4":
                        System.out.println("Exiting system. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n-== Main Menu ==-");
        System.out.println("1. Import Task from CSV");
        System.out.println("2. Export Task to CSV");
        System.out.println("3. Search and View Tasks");
        System.out.println("4. Exit");
    }

    private static void handleImport(Scanner scanner, CsvService csvService) throws Exception {
        System.out.println("Enter the path to the CSV file (e.g. tasks.csv): ");
        String path = scanner.nextLine();
        csvService.importFromCsv(path);
        System.out.println("Successfully imported tasks. System data has been refreshed.");
    }

    private static void handleExport(Scanner scanner, CsvService csvService) throws Exception {
        System.out.println("Enter the destination path for the CSV file (e.g. export.csv): ");
        String path  = scanner.nextLine();
        csvService.exportToCvs(path);
        System.out.println("Successfully exported database to " + path);
    }

    private static void handleSearchAndView(Scanner scanner, TaskService taskService) {
        System.out.println("\n-== Search Task ==-");
        System.out.println("Press enter to skip a criteria");

        System.out.println("Name match: ");
        String nameMatch = scanner.nextLine();
        if (nameMatch.trim().isEmpty()) nameMatch = null;

        System.out.println("Status (OPEN, COMPLETED, CANCELLED): ");
        String statusStr = scanner.nextLine().toUpperCase();
        TaskStatus status = null;
        if (!statusStr.trim().isEmpty()) {
            try {
                status = TaskStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid Status. Ignoring status filter");
            }
        }

        System.out.println("From Date (YYYY-MM-DD): ");
        LocalDate fromDate = parseDateSilently(scanner.nextLine());

        System.out.println("To Date (YYYY-MM-DD): ");
        LocalDate toDate = parseDateSilently(scanner.nextLine());

        // Search
        List<Task> results = taskService.searchTasks(nameMatch, status, fromDate, toDate);

        // Show results
        System.out.println("\n-== Results Found: " + results.size() + " ==-");
        if (results.isEmpty()) {
            System.out.println("No task match your criteria.");
        } else {
            // Print in table form
            System.out.printf("%-30s | %-10s | %-10s | %-10s | %-15s\n", "Task Name", "Status", "Due Date", "Is Subtask", "Collaborator");
            System.out.println("-".repeat(85));
            for (Task t: results) {
                String isSub = t.getParentTask() != null ? "Yes" : "No";
                String collab = t.getCollaborator() != null ? t.getCollaborator().getName() : "None";
                String dueDate = t.getDueDate() != null ? t.getDueDate().toString() : "None";

                System.out.printf("%-30s | %-10s | %-10s | %-10s | %-15s\n", truncate(t.getTaskName(), 30), t.getStatus(), dueDate, isSub, collab);
            }
        }
    }

    // Helper for bad or empty date inputs
    private static LocalDate parseDateSilently(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(input.trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Ignoring date filter");
            return null;
        }
    }

    // Helper function to truncate strings that are too long
    private static String truncate(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }
}