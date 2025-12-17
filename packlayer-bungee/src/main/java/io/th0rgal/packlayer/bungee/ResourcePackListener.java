package io.th0rgal.packlayer.bungee;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResourcePackSend;
import io.th0rgal.packlayer.core.PackLayerService;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class ResourcePackListener implements PacketListener {

    private final PackLayerService service;

    ResourcePackListener(@NotNull PackLayerService service) {
        this.service = service;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.RESOURCE_PACK_SEND) {
            return;
        }

        User user = event.getUser();
        if (user == null || user.getUUID() == null) {
            return;
        }

        UUID playerId = user.getUUID();
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);

        WrapperPlayServerResourcePackSend packet = new WrapperPlayServerResourcePackSend(event);

        String url = packet.getUrl();
        String hash = packet.getHash();
        // Note: Pack UUID is available in 1.20.3+ but we primarily use hash for deduplication

        String serverName = player != null && player.getServer() != null
                ? player.getServer().getInfo().getName()
                : null;

        String playerName = player != null ? player.getName() : null;

        if (service.shouldSkipPack(playerId, playerName, url, hash, null, serverName)) {
            event.setCancelled(true);
        }
    }
}
