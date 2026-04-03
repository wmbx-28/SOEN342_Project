package com.taskmanager.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.models.Collaborator;
import com.taskmanager.models.CollaboratorCategory;
import com.taskmanager.models.Project;
import com.taskmanager.models.RecurrencePattern;
import com.taskmanager.models.Task;
import com.taskmanager.models.TaskPriority;
import com.taskmanager.models.TaskStatus;
import com.taskmanager.persistence.StorageData;
import com.taskmanager.persistence.StoredCollaborator;
import com.taskmanager.persistence.StoredProject;
import com.taskmanager.persistence.StoredTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryRepository {
    private final Map<String, Project> projectsByName = new HashMap<>();
    private final Map<String, Collaborator> collaboratorsByName = new HashMap<>();
    private final List<Task> allTasks = new ArrayList<>();
    private final ObjectMapper objectMapper;
    private final Path storagePath;

    public InMemoryRepository() {
        this("data/task-manager-data.json");
    }

    public InMemoryRepository(String filePath) {
        this.storagePath = Paths.get(filePath);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        loadFromDisk();
    }

    // Projects
    public Project getOrCreateProject(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        Project existingProject = projectsByName.get(name);
        if (existingProject != null) {
            return existingProject;
        }

        Project project = new Project(UUID.randomUUID().toString(), name, description);
        projectsByName.put(name, project);
        saveToDisk();
        return project;
    }

    // Collaborators
    public Collaborator getOrCreateCollaborator(String name, CollaboratorCategory category) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        Collaborator existingCollaborator = collaboratorsByName.get(name);
        if (existingCollaborator != null) {
            return existingCollaborator;
        }

        Collaborator collaborator = new Collaborator(UUID.randomUUID().toString(), name, category);
        collaboratorsByName.put(name, collaborator);
        saveToDisk();
        return collaborator;
    }

    // Tasks
    public void saveTask(Task task) throws Exception {
        boolean exists = allTasks.stream().anyMatch(t ->
                t.getTaskName().equalsIgnoreCase(task.getTaskName()) &&
                        t.getDueDate() != null && task.getDueDate() != null &&
                        t.getDueDate().equals(task.getDueDate())
        );

        if (exists) {
            throw new Exception("Constraint Violation: A task named '" + task.getTaskName() + "' on " + task.getDueDate() + " already exists.");
        }

        if (task.getStatus() == TaskStatus.OPEN && task.getDueDate() == null && countOpenTasksWithoutDueDate() >= 50) {
            throw new Exception("Constraint Violation: The system cannot have more than 50 open tasks without a due date.");
        }

        allTasks.add(task);
        saveToDisk();
    }

    public List<Task> getAllTasks() {
        return allTasks;
    }

    public List<Collaborator> getAllCollaborators() {
        recomputeCollaboratorCounts();
        return Collections.unmodifiableList(new ArrayList<>(collaboratorsByName.values()));
    }

    public long countOpenTasksWithoutDueDate() {
        return allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.OPEN && task.getDueDate() == null)
                .count();
    }

    public long countOpenTasksForCollaborator(Collaborator collaborator) {
        return allTasks.stream()
                .filter(task -> task.getCollaborator() != null)
                .filter(task -> task.getCollaborator().getId().equals(collaborator.getId()))
                .filter(task -> task.getStatus() == TaskStatus.OPEN)
                .count();
    }

    public void clearAll() {
        projectsByName.clear();
        collaboratorsByName.clear();
        allTasks.clear();
        saveToDisk();
    }

    private void loadFromDisk() {
        try {
            ensureStorageFileExists();
            if (Files.size(storagePath) == 0) {
                saveToDisk();
                return;
            }

            StorageData storageData = objectMapper.readValue(storagePath.toFile(), StorageData.class);
            restoreProjects(storageData.projects);
            restoreCollaborators(storageData.collaborators);
            restoreTasks(storageData.tasks);
            recomputeCollaboratorCounts();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load persisted data from " + storagePath, e);
        }
    }

    private void restoreProjects(List<StoredProject> storedProjects) {
        if (storedProjects == null) {
            return;
        }

        for (StoredProject storedProject : storedProjects) {
            Project project = new Project(storedProject.id, storedProject.name, storedProject.description);
            projectsByName.put(project.getName(), project);
        }
    }

    private void restoreCollaborators(List<StoredCollaborator> storedCollaborators) {
        if (storedCollaborators == null) {
            return;
        }

        for (StoredCollaborator storedCollaborator : storedCollaborators) {
            Collaborator collaborator = new Collaborator(
                    storedCollaborator.id,
                    storedCollaborator.name,
                    CollaboratorCategory.valueOf(storedCollaborator.category)
            );
            collaborator.setCurrentOpenTaskCount(storedCollaborator.currentOpenTaskCount);
            collaboratorsByName.put(collaborator.getName(), collaborator);
        }
    }

    private void restoreTasks(List<StoredTask> storedTasks) {
        if (storedTasks == null) {
            return;
        }

        Map<String, Task> tasksById = new HashMap<>();
        Map<String, String> projectIdsByTaskId = new HashMap<>();
        Map<String, String> collaboratorIdsByTaskId = new HashMap<>();
        Map<String, String> parentIdsByTaskId = new HashMap<>();

        for (StoredTask storedTask : storedTasks) {
            Task task = new Task(storedTask.id, storedTask.taskName);
            task.setDescription(storedTask.description);
            if (storedTask.status != null) {
                task.setStatus(TaskStatus.valueOf(storedTask.status));
            }
            if (storedTask.priority != null) {
                task.setPriority(TaskPriority.valueOf(storedTask.priority));
            }
            task.setCreationDate(storedTask.creationDate);
            task.setDueDate(storedTask.dueDate);
            if (storedTask.recurrencePattern != null) {
                task.setRecurrencePattern(RecurrencePattern.valueOf(storedTask.recurrencePattern));
            }
            task.setRecurrenceStartDate(storedTask.recurrenceStartDate);
            task.setRecurrenceEndDate(storedTask.recurrenceEndDate);

            allTasks.add(task);
            tasksById.put(task.getId(), task);
            projectIdsByTaskId.put(task.getId(), storedTask.projectId);
            collaboratorIdsByTaskId.put(task.getId(), storedTask.collaboratorId);
            parentIdsByTaskId.put(task.getId(), storedTask.parentTaskId);
        }

        Map<String, Project> projectsById = new HashMap<>();
        for (Project project : projectsByName.values()) {
            projectsById.put(project.getId(), project);
        }

        Map<String, Collaborator> collaboratorsById = new HashMap<>();
        for (Collaborator collaborator : collaboratorsByName.values()) {
            collaboratorsById.put(collaborator.getId(), collaborator);
        }

        for (Task task : allTasks) {
            String taskId = task.getId();
            String projectId = projectIdsByTaskId.get(taskId);
            String collaboratorId = collaboratorIdsByTaskId.get(taskId);
            String parentTaskId = parentIdsByTaskId.get(taskId);

            if (projectId != null) {
                task.setProject(projectsById.get(projectId));
            }
            if (collaboratorId != null) {
                task.setCollaborator(collaboratorsById.get(collaboratorId));
            }
            if (parentTaskId != null) {
                Task parentTask = tasksById.get(parentTaskId);
                task.setParentTask(parentTask);
                if (parentTask != null) {
                    parentTask.addSubtask(task);
                }
            }
        }
    }

    private void ensureStorageFileExists() throws IOException {
        Path parentDirectory = storagePath.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }
        if (!Files.exists(storagePath)) {
            Files.createFile(storagePath);
        }
    }

    private void saveToDisk() {
        try {
            ensureStorageFileExists();
            recomputeCollaboratorCounts();
            objectMapper.writeValue(storagePath.toFile(), toStorageData());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save persisted data to " + storagePath, e);
        }
    }

    private void recomputeCollaboratorCounts() {
        for (Collaborator collaborator : collaboratorsByName.values()) {
            collaborator.setCurrentOpenTaskCount(0);
        }

        for (Task task : allTasks) {
            if (task.getCollaborator() != null && task.getStatus() == TaskStatus.OPEN) {
                Collaborator collaborator = task.getCollaborator();
                collaborator.setCurrentOpenTaskCount(collaborator.getCurrentOpenTaskCount() + 1);
            }
        }
    }

    private StorageData toStorageData() {
        StorageData storageData = new StorageData();

        projectsByName.values().stream()
                .sorted(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(project -> {
                    StoredProject storedProject = new StoredProject();
                    storedProject.id = project.getId();
                    storedProject.name = project.getName();
                    storedProject.description = project.getDescription();
                    storageData.projects.add(storedProject);
                });

        collaboratorsByName.values().stream()
                .sorted(Comparator.comparing(Collaborator::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(collaborator -> {
                    StoredCollaborator storedCollaborator = new StoredCollaborator();
                    storedCollaborator.id = collaborator.getId();
                    storedCollaborator.name = collaborator.getName();
                    storedCollaborator.category = collaborator.getCategory().name();
                    storedCollaborator.currentOpenTaskCount = collaborator.getCurrentOpenTaskCount();
                    storageData.collaborators.add(storedCollaborator);
                });

        allTasks.stream()
                .sorted(Comparator.comparing(Task::getId))
                .forEach(task -> {
                    StoredTask storedTask = new StoredTask();
                    storedTask.id = task.getId();
                    storedTask.taskName = task.getTaskName();
                    storedTask.description = task.getDescription();
                    storedTask.status = task.getStatus() != null ? task.getStatus().name() : null;
                    storedTask.priority = task.getPriority() != null ? task.getPriority().name() : null;
                    storedTask.creationDate = task.getCreationDate();
                    storedTask.dueDate = task.getDueDate();
                    storedTask.projectId = task.getProject() != null ? task.getProject().getId() : null;
                    storedTask.collaboratorId = task.getCollaborator() != null ? task.getCollaborator().getId() : null;
                    storedTask.parentTaskId = task.getParentTask() != null ? task.getParentTask().getId() : null;
                    storedTask.recurrencePattern = task.getRecurrencePattern() != null ? task.getRecurrencePattern().name() : null;
                    storedTask.recurrenceStartDate = task.getRecurrenceStartDate();
                    storedTask.recurrenceEndDate = task.getRecurrenceEndDate();
                    storageData.tasks.add(storedTask);
                });

        return storageData;
    }
}
