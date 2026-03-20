package java.com.taskmanager.services;

import java.com.taskmanager.db.InMemoryRepository;
import java.com.taskmanager.models.Collaborator;
import java.com.taskmanager.models.Task;
import java.com.taskmanager.models.TaskStatus;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaskService {
    private final InMemoryRepository repository;

    public TaskService(InMemoryRepository repository) {
        this.repository = repository;
    }

    // RULE: Linking a task to a collaborator creates a subtask
    // RULE: Enforce limits on open tasks based on category
    public void assignCollaborator(Task parentsTask, Collaborator collaborator) throws Exception {
        if (!collaborator.canTakeNewTask()) {
            throw new Exception("Error: Collaborator " + collaborator.getName() + " has reached their limit of " + collaborator.getCategory().getMaxOpenTasks() + " open tasks.");
        }

        // Create subtask
        Task subtask = new Task(UUID.randomUUID().toString(), parentsTask.getTaskName() + " - " + collaborator.getName());
        subtask.setCollaborator(collaborator);
        subtask.setProject(parentsTask.getProject());
        subtask.setDueDate(parentsTask.getDueDate());

        // Link and save it
        parentsTask.addSubtasks(subtask);
        repository.saveTask(subtask);

        // Update collaborator active load
        collaborator.setCurrentOpenTaskCount(collaborator.getCurrentOpenTaskCount() + 1);
    }

    // RULE: Search tasks by criteria. If none, return all OPEN tasks sorted by due date
    public List<Task> searchTasks(String nameMatch, TaskStatus status, LocalDate fromDate, LocalDate toDate) {
        return repository.getAllTasks().stream()
                .filter(t -> nameMatch == null || t.getTaskName().toLowerCase().contains(nameMatch.toLowerCase()))
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> fromDate == null || (t.getDueDate() != null && !t.getDueDate().isBefore(fromDate)))
                .filter(t -> toDate == null || (t.getDueDate() != null && !t.getDueDate().isAfter(fromDate)))
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
}
