package io.th0rgal.packlayer.bungee;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.bungee.factory.BungeePacketEventsBuilder;
import io.th0rgal.packlayer.core.PackLayerService;
import net.md_5.bungee.api.plugin.Plugin;

public final class PackLayerBungeePlugin extends Plugin {

    private PackLayerService service;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(BungeePacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        // Initialize service
        service = new PackLayerService(getDataFolder().toPath());
        service.setLogger(msg -> getLogger().info(msg));
        service.setDebugLogger(msg -> getLogger().info(msg));
        service.init();

        // Register packet listener
        PacketEvents.getAPI().getEventManager().registerListener(
                new ResourcePackListener(service),
                PacketListenerPriority.HIGHEST
        );
        PacketEvents.getAPI().init();

        // Register event listeners
        getProxy().getPluginManager().registerListener(this, new DisconnectListener(service));
        getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(service));

        // Register commands
        getProxy().getPluginManager().registerCommand(this, new PackLayerCommand(service));

        getLogger().info("LayerPack enabled - " + service.getConfig().getSkipMode() + " mode");
    }

    @Override
    public void onDisable() {
        if (service != null) {
            service.shutdown();
        }
        PacketEvents.getAPI().terminate();
    }

    public PackLayerService getService() {
        return service;
    }
}
