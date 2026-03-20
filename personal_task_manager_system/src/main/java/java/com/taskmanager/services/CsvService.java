package java.com.taskmanager.services;

import java.com.taskmanager.db.InMemoryRepository;
import java.com.taskmanager.models.Task;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

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
}
