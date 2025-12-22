package io.th0rgal.packlayer.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Main service for LayerPack functionality.
 * Platform plugins (Velocity/Bungee) should create and configure this service.
 */
public final class PackLayerService {

    private final PackLayerConfig config;
    private final PackTracker tracker;
    private final PackStatistics statistics;
    private final List<PackSkipEventHandler> eventHandlers;
    private Consumer<String> logger;
    private Consumer<String> debugLogger;

    public PackLayerService(@NotNull Path dataFolder) {
        this.config = new PackLayerConfig(dataFolder);
        this.tracker = new PackTracker();
        this.statistics = new PackStatistics();
        this.eventHandlers = new CopyOnWriteArrayList<>();
    }

    /**
     * Initializes the service (loads config).
     */
    public void init() {
        config.load();
        log("LayerPack initialized with skip-mode: " + config.getSkipMode());
    }

    /**
     * Shuts down the service.
     */
    public void shutdown() {
        tracker.clear();
        log("LayerPack shut down. " + statistics.getSummary());
    }

    /**
     * Reloads the configuration.
     */
    public void reload() {
        config.load();
        log("Configuration reloaded. Skip-mode: " + config.getSkipMode());
    }

    /**
     * Checks if a resource pack should be skipped for a player.
     *
     * @param playerId   the player's UUID
     * @param playerName the player's name (for logging/events)
     * @param url        the pack URL
     * @param hash       the pack SHA-1 hash
     * @param packUuid   the pack UUID (1.20.3+)
     * @param serverName the backend server name (for filtering)
     * @return true if the pack should be skipped, false if it should be sent
     */
    public boolean shouldSkipPack(
            @NotNull UUID playerId,
            @Nullable String playerName,
            @Nullable String url,
            @Nullable String hash,
            @Nullable UUID packUuid,
            @Nullable String serverName
    ) {
        // Check server filter
        if (serverName != null && !config.isServerAllowed(serverName)) {
            debug("Server " + serverName + " not in filter, allowing pack for " + playerName);
            return false;
        }

        PackInfo packInfo = new PackInfo(url, hash, packUuid);

        // Check if should skip
        boolean shouldSkip = tracker.shouldSkip(playerId, packInfo, config);

        if (shouldSkip) {
            // Fire event
            PackSkipEvent event = new PackSkipEvent(playerId, playerName, packInfo, serverName);
            for (PackSkipEventHandler handler : eventHandlers) {
                try {
                    handler.onPackSkip(event);
                } catch (Exception e) {
                    // Don't let handler exceptions break the flow
                }
            }

            // Check if event was cancelled
            if (event.isCancelled()) {
                debug("Skip cancelled by event handler for " + playerName);
                tracker.recordPack(playerId, packInfo);
                if (config.isStatisticsEnabled()) {
                    statistics.recordSent(playerId);
                }
                return false;
            }

            debug("Skipping duplicate pack for " + playerName + " (hash: " + hash + ")");
            if (config.isStatisticsEnabled()) {
                statistics.recordSkipped(playerId, hash);
            }
            return true;
        }

        debug("Sending new pack to " + playerName + " (hash: " + hash + ")");
        if (config.isStatisticsEnabled()) {
            statistics.recordSent(playerId);
        }
        return false;
    }

    /**
     * Records a server switch for grace period handling.
     */
    public void recordServerSwitch(@NotNull UUID playerId) {
        tracker.recordServerSwitch(playerId);
    }

    /**
     * Clears all pack data for a player.
     */
    public void clearPlayer(@NotNull UUID playerId) {
        tracker.clearPlayer(playerId);
        statistics.clearPlayer(playerId);
    }

    /**
     * Gets pack info for a player (for admin commands).
     */
    @NotNull
    public Set<PackInfo> getPlayerPacks(@NotNull UUID playerId) {
        return tracker.getPlayerPacks(playerId);
    }

    /**
     * Registers an event handler.
     */
    public void registerEventHandler(@NotNull PackSkipEventHandler handler) {
        eventHandlers.add(handler);
    }

    /**
     * Unregisters an event handler.
     */
    public void unregisterEventHandler(@NotNull PackSkipEventHandler handler) {
        eventHandlers.remove(handler);
    }

    /**
     * Sets the logger for info messages.
     */
    public void setLogger(@NotNull Consumer<String> logger) {
        this.logger = logger;
    }

    /**
     * Sets the logger for debug messages.
     */
    public void setDebugLogger(@NotNull Consumer<String> debugLogger) {
        this.debugLogger = debugLogger;
    }

    public PackLayerConfig getConfig() {
        return config;
    }

    public PackStatistics getStatistics() {
        return statistics;
    }

    private void log(String message) {
        if (logger != null) {
            logger.accept(message);
        }
    }

    private void debug(String message) {
        if (config.isDebugMode() && debugLogger != null) {
            debugLogger.accept("[DEBUG] " + message);
        }
    }
}
