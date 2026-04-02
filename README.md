# SOEN342_Project

William Ma 40215391 wmbx-28  
Remi C. Jubinville 40228517 LEMMYYYYY | codingbuddha69  
Khaled Daoud 40231852 khaleddaoud9

## Repository Structure

This repository contains all deliverables for Iteration II:

- `/diagrams/`: Contains the updated Domain Model, Use-Case Diagram, System Sequence Diagrams (SSDs), Interaction Diagrams for critical use cases, and the UML Class Diagram (Partial Design Model). Both `.drawio` source files and `.png` exports are provided.
- `/personal_task_manager_system/`: Contains the Java-based Proof of Concept (PoC) fulfilling the Iteration II functional requirements.
- `sample_tasks.csv`: A pre-formatted CSV file provided in the root directory to test the PoC's import functionality.

## Proof of Concept (PoC) - How to Run

The PoC is a terminal-based Java application managed via Maven. It demonstrates the Task Search, CSV Import, and CSV Export functionalities.

**Prerequisites:**

- Java Development Kit (JDK) 11 or higher installed.
- Maven installed.

**Build and Run Instructions:**

1.  Navigate to the PoC directory:

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

Testing the System:

When prompted by the CLI to import tasks, you can use the `plaintext ../sample_tasks.csv` file located in the root directory to instantly populate the system with dummy data.
