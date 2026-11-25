import buildlogic.getVersion
import buildlogic.stringyLibs

plugins {
    `java-library`
    id("buildlogic.common")
    id("buildlogic.common-java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    maven {
        name = "EngineHub Repository"
        url = uri("https://maven.enginehub.org/repo/")
        content {
            excludeModule("net.fabricmc", "yarn")
        }
    }
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "FabricMC (Yarn)"
        url = uri("https://maven.fabricmc.net/#yarn-only")
    }
    mavenCentral()
    // FAWE-Folia: kept on top of upstream's "FabricMC (Yarn)" repository above. Paperweight adds its own
    // FabricMC repository for the newer dev bundles, but not for adapter-1_21's pinned one, so its yarn param mappings resolve against EngineHub's mirror alone -
    // which 404s for them. A plain repository declaration is not enough, as net.fabricmc is bound
    // exclusively to paperweight's repository; claim the group exclusively so it always has a home.
    exclusiveContent {
        forRepository {
            maven {
                name = "FabricMC"
                url = uri("https://maven.fabricmc.net/")
            }
        }
        filter {
            includeGroup("net.fabricmc")
        }
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
        "remapper"("net.fabricmc:tiny-remapper:[${stringyLibs.getVersion("minimumTinyRemapper")},)") {
            because("Need remapper to support Java 21")
        }
    }
}

java {
    // Required when we de-sync release option and declared Java versions.
    disableAutoTargetJvm()
}

tasks.named<Javadoc>("javadoc") {
    enabled = false
}
