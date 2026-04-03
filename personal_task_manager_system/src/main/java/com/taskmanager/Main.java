package com.taskmanager;

import com.taskmanager.db.InMemoryRepository;
import com.taskmanager.models.Collaborator;
import com.taskmanager.models.Project;
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
                        handleICalExport(scanner, taskService);
                        break;
                    case "4":
                        handleSearchAndView(scanner, taskService);
                        break;
                    case "5":
                        handleListOverloadedCollaborators(taskService);
                        break;
                    case "6":
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
        System.out.println("3. Export to iCal");
        System.out.println("4. Search and View Tasks");
        System.out.println("5. List Overloaded Collaborators");
        System.out.println("6. Exit");
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

    private static void handleICalExport(Scanner scanner, TaskService taskService) throws Exception {
        System.out.println("\n-== Export to iCal ==-");
        System.out.println("1. Export single task");
        System.out.println("2. Export project tasks");
        System.out.println("3. Export filtered task list");
        System.out.println("Choose export mode: ");
        String choice = scanner.nextLine();

        System.out.println("Enter the destination path for the .ics file (e.g. data/tasks.ics): ");
        String filePath = scanner.nextLine();

        switch (choice) {
            case "1":
                System.out.println("Enter the exact task name: ");
                String taskName = scanner.nextLine();
                Task selectedTask = taskService.findTopLevelTaskByName(taskName);
                taskService.exportSingleTask(selectedTask, filePath);
                System.out.println("Successfully exported matching task(s) to " + filePath);
                break;
            case "2":
                System.out.println("Enter the exact project name: ");
                String projectName = scanner.nextLine();
                Project selectedProject = taskService.findProjectByName(projectName);
                taskService.exportProjectTasks(selectedProject, filePath);
                System.out.println("Successfully exported project task(s) to " + filePath);
                break;
            case "3":
                SearchCriteria criteria = promptForSearchCriteria(scanner);
                List<Task> filteredTasks = taskService.searchTasks(criteria.nameMatch(), criteria.status(), criteria.fromDate(), criteria.toDate());
                taskService.exportFilteredList(filteredTasks, filePath);
                System.out.println("Successfully exported filtered task(s) to " + filePath);
                break;
            default:
                System.out.println("Invalid iCal export choice.");
        }
    }

    private static void handleSearchAndView(Scanner scanner, TaskService taskService) {
        System.out.println("\n-== Search Task ==-");
        System.out.println("Press enter to skip a criteria");
        SearchCriteria criteria = promptForSearchCriteria(scanner);

        // Search
        List<Task> results = taskService.searchTasks(criteria.nameMatch(), criteria.status(), criteria.fromDate(), criteria.toDate());

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

    private static SearchCriteria promptForSearchCriteria(Scanner scanner) {
        System.out.println("Name match: ");
        String nameMatch = scanner.nextLine();
        if (nameMatch.trim().isEmpty()) {
            nameMatch = null;
        }

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

        return new SearchCriteria(nameMatch, status, fromDate, toDate);
    }

    private static void handleListOverloadedCollaborators(TaskService taskService) {
        List<Collaborator> overloadedCollaborators = taskService.listOverloadedCollaborators();

        System.out.println("\n-== Overloaded Collaborators ==-");
        if (overloadedCollaborators.isEmpty()) {
            System.out.println("No overloaded collaborators found.");
            return;
        }

        System.out.printf("%-20s | %-15s | %-10s | %-10s | %-12s\n", "Name", "Category", "Open Tasks", "Limit", "Overload By");
        System.out.println("-".repeat(78));

        for (Collaborator collaborator : overloadedCollaborators) {
            int openTasks = collaborator.getCurrentOpenTaskCount();
            int limit = collaborator.getCategory().getMaxOpenTasks();
            int overloadBy = openTasks - limit;

            System.out.printf("%-20s | %-15s | %-10d | %-10d | %-12d\n",
                    collaborator.getName(),
                    collaborator.getCategory(),
                    openTasks,
                    limit,
                    overloadBy);
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

    private record SearchCriteria(String nameMatch, TaskStatus status, LocalDate fromDate, LocalDate toDate) {
    }
}