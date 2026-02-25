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
        gradlePluginPortal()

    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // ✅ JitPack (para TouchImageView, MPAndroidChart, PhotoView, uCrop)
        maven("https://jitpack.io")

        // ✅ Mapbox
        maven("https://api.mapbox.com/downloads/v2/releases/maven")

        // opcional
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "GeinzWork"
include(":app")

 