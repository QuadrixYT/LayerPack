package io.th0rgal.packlayer.bungee;

import io.th0rgal.packlayer.core.PackLayerService;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

final class DisconnectListener implements Listener {

    private final PackLayerService service;

    DisconnectListener(@NotNull PackLayerService service) {
        this.service = service;
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        service.clearPlayer(event.getPlayer().getUniqueId());
    }
}
