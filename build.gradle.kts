import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.gradleup.shadow") version "9.4.1"
    id("java")
}

val pluginVersion: String by project
val pluginDescription = "Optimizes resource pack distribution on proxy networks by preventing duplicate pack sends."

allprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    group = "io.th0rgal.packlayer"
    version = pluginVersion

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"

        // Wichtig:
        // Baut mit JDK 25, aber erzeugt Java-17-kompatible Bytecode-Version.
        options.release.set(25)
    }

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(listOf("plugin.yml", "bungee.yml", "velocity-plugin.json")) {
            expand(
                mapOf(
                    "version" to pluginVersion,
                    "description" to pluginDescription
                )
            )
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()

        maven {
            name = "PaperMC"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            name = "SonatypeSnapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }

        maven {
            name = "CodeMCReleases"
            url = uri("https://repo.codemc.io/repository/maven-releases/")
        }

        maven {
            name = "CodeMCSnapshots"
            url = uri("https://repo.codemc.io/repository/maven-snapshots/")
        }
    }
}

project(":packlayer-core") {
    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.1")
        implementation("org.yaml:snakeyaml:2.2")
    }
}

project(":packlayer-bungee") {
    dependencies {
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
        compileOnly("org.jetbrains:annotations:24.0.1")

        implementation(project(":packlayer-core"))

        implementation("com.github.retrooper:packetevents-bungeecord:2.12.1")
    }
}

project(":packlayer-velocity") {
    dependencies {
        compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
        annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

        compileOnly("org.jetbrains:annotations:24.0.1")

        implementation(project(":packlayer-core"))

        implementation("com.github.retrooper:packetevents-velocity:2.12.1")
    }
}

dependencies {
    implementation(project(":packlayer-core"))
    implementation(project(":packlayer-bungee"))
    implementation(project(":packlayer-velocity"))
}

tasks.shadowJar {
    archiveFileName.set("layerpack-${pluginVersion}.jar")

    relocate("org.yaml.snakeyaml", "io.th0rgal.packlayer.shaded.snakeyaml")

    manifest {
        attributes(
            "Built-By" to System.getProperty("user.name"),
            "Version" to pluginVersion,
            "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(Date()),
            "Created-By" to "Gradle ${gradle.gradleVersion}",
            "Build-Jdk" to "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})",
            "Build-OS" to "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}"
        )
    }

    dependsOn(
        project(":packlayer-core").tasks.named("jar"),
        project(":packlayer-bungee").tasks.named("jar"),
        project(":packlayer-velocity").tasks.named("jar")
    )
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
