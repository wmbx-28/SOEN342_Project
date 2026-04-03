package com.taskmanager.gateway;

import com.taskmanager.models.Task;

import java.util.List;

public interface CalendarGateway {
    void exportTasks(List<Task> tasks, String filePath) throws Exception;
}
