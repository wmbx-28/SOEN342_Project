package com.taskmanager.services;

import com.taskmanager.db.InMemoryRepository;
import com.taskmanager.models.*;

import com.taskmanager.models.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CsvService {
    private final InMemoryRepository repository;
    private final TaskService taskService;
    private static final String HEADER = "TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory";

    public CsvService(InMemoryRepository repository, TaskService taskService) {
        this.repository = repository;
        this.taskService = taskService;
    }

    public void exportToCvs(String filePath) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(HEADER);
            List<Task> tasks = repository.getAllTasks();

            for (Task t: tasks) {
                String isSubTask = t.getParentTask() != null ? "Yes" : "No";
                String projName = t.getProject() != null ? t.getProject().getName() : "";
                String projDesc = t.getProject() != null ? t.getProject().getDescription() : "";
                String collabName = t.getCollaborator() != null ? t.getCollaborator().getName() : "";
                String collabCat = t.getCollaborator() != null ? t.getCollaborator().getCategory().name() : "";
                String dueDate = t.getDueDate() != null ? t.getDueDate().toString() : "";

                // Join with commas
                String line = String.join(",",
                        t.getTaskName(),
                        t.getDescription() == null ? "" : t.getDescription(),
                        isSubTask,
                        t.getStatus().name(),
                        t.getPriority() == null ? "" : t.getPriority().name(),
                        dueDate,
                        projName,
                        projDesc,
                        collabName,
                        collabCat
                );
                writer.println(line);
            }
        }
    }

    public void importFromCsv(String filePath) throws Exception {
        repository.clearAll(); // System starts fresh for requirements.

        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip the header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                if (values.length < 10) continue;

                Task task = new Task(UUID.randomUUID().toString(), values[0]);
                task.setDescription(values[1]);
                task.setStatus(TaskStatus.valueOf(values[3].toUpperCase()));
                task.setPriority(TaskPriority.valueOf(values[4].toUpperCase()));
                if (!values[5].isEmpty()) task.setDueDate(LocalDate.parse(values[5]));

                // Project creation
                if (!values[6].isEmpty()) {
                    Project p = repository.getOrCreateProject(values[6], values[7]);
                    task.setProject(p);
                }

                // Collaborator creation
                if(!values[8].isEmpty() && !values[9].isEmpty()) {
                    CollaboratorCategory cat = CollaboratorCategory.valueOf(values[9].toUpperCase());
                    Collaborator c = repository.getOrCreateCollaborator(values[8], cat);

                    // Link collaborator if there's a subtask
                    if (values[2].equalsIgnoreCase("No")) {
                        repository.saveTask(task);
                        taskService.assignCollaborator(task, c);
                        continue;
                    } else {
                        task.setCollaborator(c);
                    }

                }

                repository.saveTask(task);
            }
        }
    }
}
