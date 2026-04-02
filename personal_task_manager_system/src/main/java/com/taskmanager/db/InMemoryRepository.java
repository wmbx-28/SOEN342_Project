package com.taskmanager.db;

import com.taskmanager.models.Collaborator;
import com.taskmanager.models.CollaboratorCategory;
import com.taskmanager.models.Project;
import com.taskmanager.models.Task;
import java.util.*;

public class InMemoryRepository {
    private final Map<String, Project> projectsByName = new HashMap<>();
    private final Map<String, Collaborator> collaboratorsByName = new HashMap<>();
    private final List<Task> allTasks = new ArrayList<>();

    // Projects
    public Project getOrCreateProject(String name, String description) {
        if (name == null || name.trim().isEmpty()) return null;
        // Enforce unique project names
        return projectsByName.computeIfAbsent(name, k -> new Project(UUID.randomUUID().toString(), name, description));
    }

    // Collaborators
    public Collaborator getOrCreateCollaborator(String name, CollaboratorCategory category) {
        if (name == null || name.trim().isEmpty()) return null;
        return collaboratorsByName.computeIfAbsent(name, k -> new Collaborator(UUID.randomUUID().toString(), name, category));
    }

    // Tasks
    // Tasks
    public void saveTask(Task task) throws Exception {
        // RULE ENFORCEMENT: Unique combination of Task Name + Due Date
        boolean exists = allTasks.stream().anyMatch(t ->
                t.getTaskName().equalsIgnoreCase(task.getTaskName()) &&
                        t.getDueDate() != null && task.getDueDate() != null &&
                        t.getDueDate().equals(task.getDueDate())
        );

        if (exists) {
            throw new Exception("Constraint Violation: A task named '" + task.getTaskName() + "' on " + task.getDueDate() + " already exists.");
        }

        allTasks.add(task);
    }

    public List<Task> getAllTasks() {
        return allTasks;
    }

    // Clear data for import
    public void clearAll() {
        projectsByName.clear();
        collaboratorsByName.clear();
        allTasks.clear();
    }
}
