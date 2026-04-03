package com.taskmanager.gateway;

import com.taskmanager.models.Task;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ICal4jGateway implements CalendarGateway {
    @Override
    public void exportTasks(List<Task> tasks, String filePath) throws Exception {
        Calendar calendar = new Calendar();
        Version version = new Version();
        version.setValue(Version.VALUE_2_0);
        calendar.add(new ProdId("-//SOEN342 Personal Task Manager//iCal Export//EN"));
        calendar.add(version);
        calendar.add(new CalScale(CalScale.VALUE_GREGORIAN));

        for (Task task : tasks) {
            if (task.getDueDate() == null || task.getParentTask() != null) {
                continue;
            }

            VEvent event = new VEvent(task.getDueDate(), task.getTaskName());
            event.add(new Uid(UUID.randomUUID().toString()));
            event.add(new Summary(task.getTaskName()));
            event.add(new Description(buildDescription(task)));
            calendar.add(event);
        }

        Path outputPath = Path.of(filePath);
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            new CalendarOutputter().output(calendar, outputStream);
        }
    }

    private String buildDescription(Task task) {
        StringBuilder description = new StringBuilder();

        if (task.getDescription() != null && !task.getDescription().isBlank()) {
            description.append(task.getDescription()).append("\n");
        }

        description.append("Status: ").append(task.getStatus()).append("\n");
        description.append("Priority: ").append(task.getPriority() != null ? task.getPriority() : "N/A").append("\n");

        if (task.getProject() != null) {
            description.append("Project: ").append(task.getProject().getName()).append("\n");
        }

        if (!task.getSubtasks().isEmpty()) {
            description.append("Subtasks: ");
            for (int i = 0; i < task.getSubtasks().size(); i++) {
                Task subtask = task.getSubtasks().get(i);
                description.append(subtask.getTaskName()).append(" [").append(subtask.getStatus()).append("]");
                if (i < task.getSubtasks().size() - 1) {
                    description.append(", ");
                }
            }
        }

        return description.toString().trim();
    }
}
