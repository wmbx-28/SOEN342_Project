package com.taskmanager.models;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String id;
    private String name;
    private String description;

    private List<Collaborator> collaborators;

    public Project(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.collaborators = new ArrayList<>();
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Collaborator> getCollaborators() {
        return collaborators;
    }

    public void addCollaborator(Collaborator collaborator) {
        this.collaborators.add(collaborator);
    }
}
