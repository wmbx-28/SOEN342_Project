# SOEN342_Project

William Ma 40215391 wmbx-28  
Remi C. Jubinville 40228517 LEMMYYYYY | codingbuddha69  
Khaled Daoud 40231852 khaleddaoud9

## Repository Structure

This repository contains the project deliverables and the Java CLI application used for the course iterations:

- `/diagrams/`: Contains the updated Domain Model, Use-Case Diagram, System Sequence Diagrams (SSDs), Interaction Diagrams for critical use cases, and the UML Class Diagram (Partial Design Model). Both `.drawio` source files and `.png` exports are provided.
- `/personal_task_manager_system/`: Contains the Java-based CLI application. It now supports JSON-based local persistence in addition to the CSV import/export functionality developed earlier.
- `sample_tasks.csv`: A pre-formatted CSV file provided in the root directory to test the PoC's import functionality.

## How to Run

The application is a terminal-based Java program managed via Maven. It supports task search, CSV import/export, and simple file-based persistence through JSON.

**Prerequisites:**

- Java Development Kit (JDK) 17 or higher installed.
- Maven installed.

**Build and Run Instructions:**

1.  Navigate to the application directory:

    ```bash
    cd personal_task_manager_system
    ```

    Compile the project using Maven:

    ```bash
     mvn clean compile
    ```

    Execute the main application:

    ```bash
    mvn exec:java "-Dexec.mainClass=com.taskmanager.Main"
    ```

## Persistence

- The application automatically creates a local JSON persistence file at `personal_task_manager_system/data/task-manager-data.json`.
- Imported tasks, created related records, and other in-memory changes are written back to this file automatically.
- Restarting the application reloads the saved data from the JSON file.

## Quick Test Flow

When prompted by the CLI to import tasks, you can use `../sample_tasks.csv` to populate the system with sample data.

To confirm persistence:

1. Import `../sample_tasks.csv`.
2. Exit the application.
3. Start it again.
4. Search tasks or export CSV to confirm the previously imported data was reloaded from `data/task-manager-data.json`.
