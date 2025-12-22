package io.th0rgal.packlayer.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import io.th0rgal.packlayer.core.PackLayerService;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "layerpack",
        name = "LayerPack",
        version = "${version}",
        description = "${description}",
        authors = {"Th0rgal"}
)
public final class PackLayerVelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final PluginContainer pluginContainer;
    private final Path dataDirectory;
    private PackLayerService service;

    @Inject
    public PackLayerVelocityPlugin(
            ProxyServer server,
            Logger logger,
            PluginContainer pluginContainer,
            @DataDirectory Path dataDirectory
    ) {
        this.server = server;
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, pluginContainer, logger, dataDirectory));
        PacketEvents.getAPI().load();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Initialize service
        service = new PackLayerService(dataDirectory);
        service.setLogger(logger::info);
        service.setDebugLogger(logger::info);
        service.init();

        // Register packet listener
        PacketEvents.getAPI().getEventManager().registerListener(
                new ResourcePackListener(server, service),
                PacketListenerPriority.HIGHEST
        );
        PacketEvents.getAPI().init();

        // Register event listeners
        server.getEventManager().register(pluginContainer, new DisconnectListener(service));
        server.getEventManager().register(pluginContainer, new ServerSwitchListener(service));

        // Register commands
        CommandMeta meta = server.getCommandManager()
                .metaBuilder("layerpack")
                .aliases("lp")
                .plugin(pluginContainer)
                .build();
        server.getCommandManager().register(meta, new PackLayerCommand(server, service));

        logger.info("LayerPack enabled - " + service.getConfig().getSkipMode() + " mode");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (service != null) {
            service.shutdown();
        }
        PacketEvents.getAPI().terminate();
        logger.info("LayerPack disabled");
    }

    public PackLayerService getService() {
        return service;
    }
}
