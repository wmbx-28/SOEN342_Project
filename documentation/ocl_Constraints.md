# SOEN 342 - Iteration III: OCL Constraints

**1."A task cannot have more than 20 sub-tasks."**

```ocl
context Task
inv: self.subtasks->size() <= 20
```

**2. "The number of open tasks without a due date should not exceed 50."**

```ocl
context Task
inv: Task.allInstances()->select(t | t.status = TaskStatus::OPEN and t.dueDate.oclIsUndefined())->size() <= 50
```

**3. "The limit for open tasks for each collaborator category is a positive integer." (i.e. Senior collaborators are limited to 2 open tasks.)**

```ocl
context CollaboratorCategory
inv: self.maxOpenTasks > 0
```

**4. "No collaborator must be overloaded." (The number of assigned tasks that are open should not exceed the limit.)**

```ocl
context Collaborator
inv: self.openTaskCount <= self.category.maxOpenTasks
```
