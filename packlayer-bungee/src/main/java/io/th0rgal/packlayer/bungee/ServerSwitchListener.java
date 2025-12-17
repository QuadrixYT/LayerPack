package io.th0rgal.packlayer.bungee;

import io.th0rgal.packlayer.core.PackLayerService;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public final class ServerSwitchListener implements Listener {

    private final PackLayerService service;

    public ServerSwitchListener(@NotNull PackLayerService service) {
        this.service = service;
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        if (event.getFrom() != null) {
            // Player switched from one server to another (not initial join)
            service.recordServerSwitch(event.getPlayer().getUniqueId());
        }
    }
}
