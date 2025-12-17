// Velocity module - Velocity provides Adventure, so don't relocate net.kyori

tasks.shadowJar {
    relocate("com.github.retrooper.packetevents", "io.th0rgal.packlayer.velocity.shaded.packetevents.api")
    relocate("io.github.retrooper.packetevents", "io.th0rgal.packlayer.velocity.shaded.packetevents.impl")
}

// Delete annotation-processor-generated velocity-plugin.json so our manually created one is used
tasks.compileJava {
    doLast {
        val apGeneratedJson = destinationDirectory.file("velocity-plugin.json").get().asFile
        if (apGeneratedJson.exists()) {
            apGeneratedJson.delete()
        }
    }
}
