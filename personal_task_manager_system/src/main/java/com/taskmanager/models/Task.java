package com.taskmanager.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String id;
    private String taskName;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;

    private LocalDate creationDate;
    private LocalDate dueDate; // unique (taskName, dueDate): one due date per task

    // Relationships
    private Project project; // Nullable
    private Collaborator collaborator; // Nullable
    private Task parentTask; // Nullable
    private List<Task> subtasks;

    // Recurrence
    private RecurrencePattern recurrencePattern;
    private LocalDate recurrenceStartDate;
    private LocalDate recurrenceEndDate;

    public Task(String id, String taskName) {
        this.id = id;
        this.taskName = taskName;
        this.status = TaskStatus.OPEN;
        this.creationDate = LocalDate.now();
        this.subtasks = new ArrayList<>();
        this.recurrencePattern = RecurrencePattern.NONE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Collaborator getCollaborator() {
        return collaborator;
    }

    public void setCollaborator(Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    public List<Task> getSubtasks() {
        return subtasks;
    }

    public void addSubtasks(Task subtask) {
        this.subtasks.add(subtask);
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public LocalDate getRecurrenceStartDate() {
        return recurrenceStartDate;
    }

    public void setRecurrenceStartDate(LocalDate recurrenceStartDate) {
        this.recurrenceStartDate = recurrenceStartDate;
    }

    public LocalDate getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }
}
