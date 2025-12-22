import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.gradleup.shadow") version "8.3.2"
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
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.processResources {
        expand(mapOf("version" to pluginVersion, "description" to pluginDescription))
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
        maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
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
        compileOnly(project(path = ":packlayer-core", configuration = "shadow"))
        implementation("com.github.retrooper:packetevents-bungeecord:2.11.1")
    }
}

project(":packlayer-velocity") {
    dependencies {
        compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
        compileOnly("org.jetbrains:annotations:24.0.1")
        compileOnly(project(path = ":packlayer-core", configuration = "shadow"))
        annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
        implementation("com.github.retrooper:packetevents-velocity:2.11.1")
    }
}

tasks.shadowJar {
    // Relocate snakeyaml to avoid conflicts
    relocate("org.yaml.snakeyaml", "io.th0rgal.packlayer.shaded.snakeyaml")

    manifest {
        attributes(
            "Built-By" to System.getProperty("user.name"),
            "Version" to pluginVersion,
            "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ").format(Date()),
            "Created-By" to "Gradle ${gradle.gradleVersion}",
            "Build-Jdk" to "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})",
            "Build-OS" to "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}"
        )
    }
    archiveFileName.set("packlayer-${pluginVersion}.jar")
}

dependencies {
    implementation(project(path = ":packlayer-core", configuration = "shadow"))
    implementation(project(path = ":packlayer-bungee", configuration = "shadow"))
    implementation(project(path = ":packlayer-velocity", configuration = "shadow"))
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
