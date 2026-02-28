# Identification of System Operations and Operation Contracts

---

## System Operations

### Task Lifecycle

1. makeNewTask(title, description?, priority, date?) -> taskId
2. updateTask(taskId, attribute, newValue)
3. completeTask(taskId)
4. cancelTask(taskId)

### Project Management

1. createProject(name, description?) -> projectId
2. updateProject(projectId, attribute, newValue)
3. assignTaskToProject(taskId, projectId)
4. removeTaskFromProject(taskId)
5. completeProject(projectId)
6. cancelProject(projectId)

### Tags

1. addTagToTask(taskId, tagName)
2. removeTagFromTask(taskId, tagName)

### Subtasks

1. createSubtask(parentTaskId, title) -> subtaskId
2. setSubtaskCompletion(parentTaskId, subtaskId, isCompleted)
3. updateSubtaskTitle(parentTaskId, subtaskId, title)
4. cancelSubtask(subtaskId)

### Viewing / Searching (Read Operations)

1. listTasksByStatus(status)
2. listTasksByPriority(order)
3. listTasksByDueDate(order)
4. listTasksByProject(projectId)
5. listTasksByTag(tagName)
6. listTasksDueOn(date)
7. listTasksDueWithin(rangeStart, rangeEnd)
8. searchTasks(keyword)
9. viewTaskActivity(taskId)

---

# Operation Contracts

---

## CO1: makeNewTask(title, description?, priority, date?)

### Preconditions

- title is not empty.
- priority is valid.

### Postconditions

- A Task instance t was created.
- t.title and t.priority were set.
- t.creationDate set to current time.
- t.status set to open.
- An ActivityEntry a was created ("Task created").

---

## CO2: updateTask(taskId, attribute, newValue)

### Preconditions

- Task t exists.
- changes are valid.

### Postconditions

- Specified attributes updated with newValue.
- Project association updated if applicable.
- Tag associations updated if applicable.
- ActivityEntry created ("Task updated").

---

## CO3: completeTask(taskId)

### Preconditions

- Task exists.
- Task status is open.

### Postconditions

- Task status set to completed.
- ActivityEntry created ("Task completed").

---

## CO4: cancelTask(taskId)

### Preconditions

- Task exists.
- Task status is open.

### Postconditions

- Task status set to cancelled.
- ActivityEntry created ("Task cancelled").

---

## CO5: createProject(name, description?)

### Preconditions

- name is not empty.

### Postconditions

- Project p created.
- p.name set.
- ActivityEntry created ("Project created").

---

## CO6: assignTaskToProject(taskId, projectId)

### Preconditions

- Task exists.
- Project exists.

### Postconditions

- Previous project association removed if any.
- Task associated with project.
- ActivityEntry created ("Task assigned to Project").

---

## CO7: removeTaskFromProject(taskId)

### Preconditions

- Task exists.
- Task currently associated with a project.

### Postconditions

- Association between task and project removed.
- ActivityEntry created ("Task unassigned from Project").

---

## CO8: addTagToTask(taskId, tagName)

### Preconditions

- Task exists.
- tagName not empty.

### Postconditions

- Tag created if not existing.
- Association formed between task and tag.
- ActivityEntry created ("Tag added to Task").

---

## CO9: removeTagFromTask(taskId, tagName)

### Preconditions

- Task exists.
- Tag exists and associated.

### Postconditions

- Association removed.
- ActivityEntry created ("Tag removed from Task").

---

## CO10: createSubtask(parentTaskId, title)

### Preconditions

- Parent task exists.
- title not empty.

### Postconditions

- Subtask s created.
- s.title set.
- s.isCompleted set to false.
- s associated with parent task.
- ActivityEntry created ("Subtask created from Task")

---

## CO11: setSubtaskCompletion(parentTaskId, subtaskId, isCompleted)

### Preconditions

- Parent task exists.
- Subtask exists and associated.

### Postconditions

- s.isCompleted updated.
- Parent task status not automatically changed.
- ActivityEntry created ("Subtask complete" or "Subtask set to incomplete").

