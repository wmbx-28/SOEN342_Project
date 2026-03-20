package com.taskmanager.models;

public class Collaborator {
    private String id; // UUID
    private String name;
    private CollaboratorCategory category;

    private int currentOpenTaskCount; // Initially 0

    public Collaborator(String name, String id, CollaboratorCategory category) {
        this.name = name;
        this.id = id;
        this.category = category;
        this.currentOpenTaskCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CollaboratorCategory getCategory() {
        return category;
    }

    public void setCategory(CollaboratorCategory category) {
        this.category = category;
    }

    public int getCurrentOpenTaskCount() {
        return currentOpenTaskCount;
    }

    public void setCurrentOpenTaskCount(int currentOpenTaskCount) {
        this.currentOpenTaskCount = currentOpenTaskCount;
    }

    // Check for max amount of tasks taken
    public boolean canTakeNewTask() {
        return currentOpenTaskCount < category.getMaxOpenTasks();
    }
}
