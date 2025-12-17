package io.th0rgal.packlayer.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe tracker for resource packs per player.
 * Supports multi-pack scenarios (Minecraft 1.20.3+).
 */
public final class PackTracker {

    // For multi-pack support: track all packs per player
    private final Map<UUID, Set<PackInfo>> playerPacks = new ConcurrentHashMap<>();

    // Track when players switched servers (for grace period)
    private final Map<UUID, Long> serverSwitchTimestamps = new ConcurrentHashMap<>();

    /**
     * Checks if a pack should be skipped based on the configuration.
     *
     * @param playerId the player's UUID
     * @param packInfo the pack information
     * @param config   the plugin configuration
     * @return true if this pack should be skipped (duplicate), false if it should be sent
     */
    public boolean shouldSkip(@NotNull UUID playerId, @NotNull PackInfo packInfo, @NotNull PackLayerConfig config) {
        SkipMode mode = config.getSkipMode();

        // Never skip mode
        if (mode == SkipMode.NEVER_SKIP) {
            recordPack(playerId, packInfo);
            return false;
        }

        // Always skip mode
        if (mode == SkipMode.ALWAYS_SKIP) {
            return true;
        }

        // Check grace period
        if (config.getServerSwitchGraceMs() > 0) {
            Long switchTime = serverSwitchTimestamps.get(playerId);
            if (switchTime != null) {
                long elapsed = System.currentTimeMillis() - switchTime;
                if (elapsed < config.getServerSwitchGraceMs()) {
                    // Within grace period, don't skip but do record
                    recordPack(playerId, packInfo);
                    return false;
                }
            }
        }

        // Check against existing packs
        Set<PackInfo> existingPacks = playerPacks.get(playerId);
        String url = packInfo.getUrl();
        boolean isTrustedDomain = url != null && config.isTrustedDomain(url);

        if (existingPacks != null) {
            for (PackInfo existing : existingPacks) {
                // For trusted domains, skip if player already has ANY pack from this domain
                // (more aggressive duplicate detection - skip based on domain alone)
                if (isTrustedDomain) {
                    String existingUrl = existing.getUrl();
                    if (existingUrl != null && config.isTrustedDomain(existingUrl)) {
                        return true; // Already has a pack from a trusted domain
                    }
                }
                // Standard duplicate check based on skip mode
                if (packInfo.matches(existing, mode)) {
                    return true; // Duplicate found
                }
            }
        }

        // Not a duplicate, record it
        recordPack(playerId, packInfo);
        return false;
    }

    /**
     * Legacy method for simple hash-only checking.
     */
    public boolean isDuplicate(@NotNull UUID playerId, @Nullable String packHash) {
        if (packHash == null || packHash.isEmpty()) {
            return false;
        }

        PackInfo packInfo = new PackInfo(null, packHash, null);
        PackLayerConfig defaultConfig = new PackLayerConfig(null);
        return shouldSkip(playerId, packInfo, defaultConfig);
    }

    /**
     * Records a pack for a player.
     */
    public void recordPack(@NotNull UUID playerId, @NotNull PackInfo packInfo) {
        playerPacks.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(packInfo);
    }

    /**
     * Clears all packs for a player.
     */
    public void clearPlayer(@NotNull UUID playerId) {
        playerPacks.remove(playerId);
        serverSwitchTimestamps.remove(playerId);
    }

    /**
     * Records a server switch for grace period handling.
     */
    public void recordServerSwitch(@NotNull UUID playerId) {
        serverSwitchTimestamps.put(playerId, System.currentTimeMillis());
    }

    /**
     * Gets all tracked packs for a player.
     */
    @NotNull
    public Set<PackInfo> getPlayerPacks(@NotNull UUID playerId) {
        Set<PackInfo> packs = playerPacks.get(playerId);
        return packs != null ? new HashSet<>(packs) : Collections.emptySet();
    }

    /**
     * Removes tracking data for a player (call on disconnect).
     */
    public void removePlayer(@NotNull UUID playerId) {
        clearPlayer(playerId);
    }

    /**
     * Clears all tracking data.
     */
    public void clear() {
        playerPacks.clear();
        serverSwitchTimestamps.clear();
    }
}
