// BungeeCord module - BungeeCord doesn't provide Adventure, so we shade it

tasks.shadowJar {
    relocate("com.github.retrooper.packetevents", "io.th0rgal.packlayer.bungee.shaded.packetevents.api")
    relocate("io.github.retrooper.packetevents", "io.th0rgal.packlayer.bungee.shaded.packetevents.impl")
    relocate("net.kyori", "io.th0rgal.packlayer.bungee.shaded.kyori")
}
