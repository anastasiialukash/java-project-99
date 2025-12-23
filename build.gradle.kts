// Root project build file

plugins {
    base
}

// Register tasks using the recommended approach
tasks.register("setup") {
    description = "Setup the project"
    doLast {
        println("Setting up the project...")
    }
}

tasks.register("projectSetup") {
    description = "Additional project setup tasks"
    doLast {
        println("Performing additional project setup...")
    }
}

// Default task when running gradle without arguments
defaultTasks("setup")