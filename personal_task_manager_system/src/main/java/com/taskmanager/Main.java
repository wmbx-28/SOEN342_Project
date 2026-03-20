package com.taskmanager;

import com.taskmanager.db.InMemoryRepository;
import com.taskmanager.services.CsvService;
import com.taskmanager.services.TaskService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialize system
        InMemoryRepository repository = new InMemoryRepository();
        TaskService taskService = new TaskService(repository);
        CsvService csvService = new CsvService(repository, taskService);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("|| Personal Task Management System PoC ||");
        System.out.println("=========================================");
    }
}