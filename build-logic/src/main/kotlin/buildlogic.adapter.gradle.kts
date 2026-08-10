import buildlogic.getVersion
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import buildlogic.stringyLibs

plugins {
    `java-library`
    id("buildlogic.common")
    id("buildlogic.common-java")
    id("io.papermc.paperweight.userdev")
}

val requiresReobfJar = project.name.startsWith("adapter-1_")

paperweight {
    injectPaperRepository = false
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
}

repositories {
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
        content {
            // excludeModule("io.papermc.paper", "dev-bundle")
        }
    }
    maven {
        name = "EngineHub Repository"
        url = uri("https://maven.enginehub.org/repo/")
        content {
            // excludeModule("io.papermc.paper", "dev-bundle")
        }
    }
/*    maven {
        name = "IntellectualSites"
        url = uri("https://repo.intellectualsites.dev/repository/paper-dev-bundles/")
        content {
            // includeModule("io.papermc.paper", "dev-bundle")
        }
    }*/
    mavenCentral()
    // FAWE-Folia: paperweight resolves yarn param mappings for the older adapters here. EngineHub's
    // mirrors 404 for them, so without this a build with a cold cache cannot resolve adapter-1_21.
    maven {
        name = "FabricMC"
        url = uri("https://maven.fabricmc.net/")
        content {
            includeGroup("net.fabricmc")
        }
    }
    afterEvaluate {
        killNonEngineHubRepositories()
        // FAWE-Folia: temporary diagnostic, remove once adapter param mappings resolve.
        logger.lifecycle(
            "FAWE-Folia repo diagnostic for {}: {}",
            project.path,
            repositories.filterIsInstance<MavenArtifactRepository>().joinToString { it.name + "=" + it.url }
        )
    }
}

dependencies {
    implementation(project(":worldedit-bukkit"))
    constraints {
        //Reduces the amount of libraries Gradle and IntelliJ need to resolve
        implementation("net.kyori:adventure-bom") {
            version { strictly(stringyLibs.getVersion("adventure").strictVersion) }
            because("Ensure a consistent version of adventure is used.")
        }
    }
}

java {
    // Required when we de-sync release option and declared Java versions.
    disableAutoTargetJvm()
}

tasks.named("assemble") {
    if (requiresReobfJar) {
        dependsOn("reobfJar")
    }
}

tasks.named<Javadoc>("javadoc") {
    enabled = false
}
