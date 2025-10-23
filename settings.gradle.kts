pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        // Ensure Gradle Plugin Portal is listed HERE
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // You might need mavenCentral() here too if any regular dependencies rely on it
    }
}

rootProject.name = "WearPokeHelper" // Or WearPokeHelper2, match your project folder
include(":app")
