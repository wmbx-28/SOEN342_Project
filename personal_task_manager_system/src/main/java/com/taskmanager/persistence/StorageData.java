package com.taskmanager.persistence;

import java.util.ArrayList;
import java.util.List;

public class StorageData {
    public List<StoredProject> projects = new ArrayList<>();
    public List<StoredCollaborator> collaborators = new ArrayList<>();
    public List<StoredTask> tasks = new ArrayList<>();
}
