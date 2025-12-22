package io.th0rgal.packlayer.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Event fired when LayerPack is about to skip a resource pack send.
 * Other plugins can listen to this event to override the skip decision.
 */
public final class PackSkipEvent {

    private final UUID playerId;
    private final String playerName;
    private final PackInfo packInfo;
    private final String serverName;
    private boolean cancelled;

    public PackSkipEvent(
            @NotNull UUID playerId,
            @Nullable String playerName,
            @NotNull PackInfo packInfo,
            @Nullable String serverName
    ) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.packInfo = packInfo;
        this.serverName = serverName;
        this.cancelled = false;
    }

    /**
     * Gets the player's UUID.
     */
    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Gets the player's name.
     */
    @Nullable
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets information about the pack being skipped.
     */
    @NotNull
    public PackInfo getPackInfo() {
        return packInfo;
    }

    /**
     * Gets the backend server name (if available).
     */
    @Nullable
    public String getServerName() {
        return serverName;
    }

    /**
     * Returns true if the skip has been cancelled (pack will be sent).
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Set to true to cancel the skip (force the pack to be sent).
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
