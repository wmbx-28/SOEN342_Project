package com.taskmanager.services;

import com.taskmanager.db.InMemoryRepository;
import com.taskmanager.models.Collaborator;
import com.taskmanager.models.Task;
import com.taskmanager.models.TaskStatus;
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
    public void assignCollaborator(Task parentTask, Collaborator c) throws Exception {
        if (!c.canTakeNewTask()) {
            throw new Exception("Error: Collaborator " + c.getName() + " has reached their limit of " + c.getCategory().getMaxOpenTasks() + " open tasks.");
        }

        // Create subtask
        Task subtask = new Task(UUID.randomUUID().toString(), parentTask.getTaskName() + " - " + c.getName());

        subtask.setParentTask(parentTask);

        subtask.setCollaborator(c);
        subtask.setProject(parentTask.getProject());
        subtask.setDueDate(parentTask.getDueDate());

        // Update the in-memory graph before saving so persistence captures all changes.
        parentTask.addSubtask(subtask);
        c.setCurrentOpenTaskCount(c.getCurrentOpenTaskCount() + 1);
        repository.saveTask(subtask);
    }

    // RULE: Search tasks by criteria, if none return all OPEN tasks sorted by due date
    public List<Task> searchTasks(String nameMatch, TaskStatus status, LocalDate fromDate, LocalDate toDate) {
        boolean noCriteriaSpecified = (nameMatch == null && status == null && fromDate == null && toDate == null);

        // If no criteria, force the filter to look for OPEN tasks. Otherwise, use what the user provided.
        TaskStatus effectiveStatus = noCriteriaSpecified ? TaskStatus.OPEN : status;

        return repository.getAllTasks().stream()
                .filter(t -> nameMatch == null || t.getTaskName().toLowerCase().contains(nameMatch.toLowerCase()))
                .filter(t -> effectiveStatus == null || t.getStatus() == effectiveStatus)
                .filter(t -> fromDate == null || (t.getDueDate() != null && !t.getDueDate().isBefore(fromDate)))
                .filter(t -> toDate == null || (t.getDueDate() != null && !t.getDueDate().isAfter(toDate)))
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
}
