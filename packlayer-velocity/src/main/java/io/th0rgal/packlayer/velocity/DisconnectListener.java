package io.th0rgal.packlayer.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import io.th0rgal.packlayer.core.PackLayerService;
import org.jetbrains.annotations.NotNull;

final class DisconnectListener {

    private final PackLayerService service;

    DisconnectListener(@NotNull PackLayerService service) {
        this.service = service;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        service.clearPlayer(event.getPlayer().getUniqueId());
    }
}
