package com.taskmanager.services;

import com.taskmanager.db.InMemoryRepository;
import com.taskmanager.gateway.CalendarGateway;
import com.taskmanager.gateway.ICal4jGateway;
import com.taskmanager.models.Collaborator;
import com.taskmanager.models.Project;
import com.taskmanager.models.Task;
import com.taskmanager.models.TaskStatus;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaskService {
    private final InMemoryRepository repository;
    private final CalendarGateway calendarGateway;

    public TaskService(InMemoryRepository repository) {
        this(repository, new ICal4jGateway());
    }

    public TaskService(InMemoryRepository repository, CalendarGateway calendarGateway) {
        this.repository = repository;
        this.calendarGateway = calendarGateway;
    }

    // RULE: Linking a task to a collaborator creates a subtask
    // RULE: Enforce limits on open tasks based on category
    public void assignCollaborator(Task parentTask, Collaborator c) throws Exception {
        long currentOpenAssignments = repository.countOpenTasksForCollaborator(c);
        if (currentOpenAssignments >= c.getCategory().getMaxOpenTasks()) {
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
        repository.saveTask(subtask);
    }

    public List<Collaborator> listOverloadedCollaborators() {
        return repository.getAllCollaborators().stream()
                .filter(c -> repository.countOpenTasksForCollaborator(c) > c.getCategory().getMaxOpenTasks())
                .sorted(Comparator.comparing(Collaborator::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Task findTopLevelTaskByName(String taskName) throws Exception {
        List<Task> matchingTasks = repository.getAllTasks().stream()
                .filter(task -> task.getTaskName().equalsIgnoreCase(taskName))
                .filter(task -> task.getParentTask() == null)
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        if (matchingTasks.isEmpty()) {
            throw new Exception("No top-level task found with the name '" + taskName + "'.");
        }

        return matchingTasks.get(0);
    }

    public Project findProjectByName(String projectName) throws Exception {
        List<Project> matchingProjects = repository.getAllTasks().stream()
                .filter(task -> task.getProject() != null)
                .filter(task -> task.getProject().getName().equalsIgnoreCase(projectName))
                .map(Task::getProject)
                .distinct()
                .collect(Collectors.toList());

        if (matchingProjects.isEmpty()) {
            throw new Exception("No project found with the name '" + projectName + "'.");
        }

        return matchingProjects.get(0);
    }

    public void exportSingleTask(Task task, String filePath) throws Exception {
        exportEligibleTasks(List.of(task), filePath);
    }

    public void exportProjectTasks(Project project, String filePath) throws Exception {
        List<Task> matchingTasks = repository.getAllTasks().stream()
                .filter(task -> task.getProject() != null)
                .filter(task -> task.getProject().getId().equals(project.getId()))
                .filter(task -> task.getParentTask() == null)
                .collect(Collectors.toList());

        exportEligibleTasks(matchingTasks, filePath);
    }

    public void exportFilteredList(List<Task> tasks, String filePath) throws Exception {
        exportEligibleTasks(tasks, filePath);
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

    private void exportEligibleTasks(List<Task> tasks, String filePath) throws Exception {
        List<Task> eligibleTasks = tasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getParentTask() == null)
                .collect(Collectors.toList());

        if (eligibleTasks.isEmpty()) {
            throw new Exception("No eligible tasks with a due date were found for iCal export.");
        }

        calendarGateway.exportTasks(eligibleTasks, filePath);
    }
}
