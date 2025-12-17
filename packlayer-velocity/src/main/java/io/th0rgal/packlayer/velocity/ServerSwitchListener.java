package io.th0rgal.packlayer.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import io.th0rgal.packlayer.core.PackLayerService;
import org.jetbrains.annotations.NotNull;

public final class ServerSwitchListener {

    private final PackLayerService service;

    public ServerSwitchListener(@NotNull PackLayerService service) {
        this.service = service;
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isPresent()) {
            // Player switched from one server to another (not initial join)
            service.recordServerSwitch(event.getPlayer().getUniqueId());
        }
    }
}
