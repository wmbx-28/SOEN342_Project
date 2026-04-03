package com.taskmanager.persistence;

import java.time.LocalDate;

public class StoredTask {
    public String id;
    public String taskName;
    public String description;
    public String status;
    public String priority;
    public LocalDate creationDate;
    public LocalDate dueDate;
    public String projectId;
    public String collaboratorId;
    public String parentTaskId;
    public String recurrencePattern;
    public LocalDate recurrenceStartDate;
    public LocalDate recurrenceEndDate;
}
