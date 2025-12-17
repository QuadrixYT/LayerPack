package io.th0rgal.packlayer.core;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks statistics about pack skipping.
 */
public final class PackStatistics {

    private final AtomicLong totalPacksSkipped = new AtomicLong(0);
    private final AtomicLong totalPacksSent = new AtomicLong(0);
    private final Map<UUID, AtomicLong> playerPacksSkipped = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> hashSkipCount = new ConcurrentHashMap<>();

    /**
     * Records a pack being skipped.
     */
    public void recordSkipped(@NotNull UUID playerId, String hash) {
        totalPacksSkipped.incrementAndGet();
        playerPacksSkipped.computeIfAbsent(playerId, k -> new AtomicLong(0)).incrementAndGet();
        if (hash != null && !hash.isEmpty()) {
            hashSkipCount.computeIfAbsent(hash, k -> new AtomicLong(0)).incrementAndGet();
        }
    }

    /**
     * Records a pack being sent (not skipped).
     */
    public void recordSent(@NotNull UUID playerId) {
        totalPacksSent.incrementAndGet();
    }

    /**
     * Gets total packs skipped across all players.
     */
    public long getTotalPacksSkipped() {
        return totalPacksSkipped.get();
    }

    /**
     * Gets total packs sent (not skipped).
     */
    public long getTotalPacksSent() {
        return totalPacksSent.get();
    }

    /**
     * Gets packs skipped for a specific player.
     */
    public long getPlayerPacksSkipped(@NotNull UUID playerId) {
        AtomicLong count = playerPacksSkipped.get(playerId);
        return count != null ? count.get() : 0;
    }

    /**
     * Gets the most commonly skipped pack hashes.
     */
    public Map<String, AtomicLong> getHashSkipCounts() {
        return new ConcurrentHashMap<>(hashSkipCount);
    }

    /**
     * Clears statistics for a player (on disconnect).
     */
    public void clearPlayer(@NotNull UUID playerId) {
        playerPacksSkipped.remove(playerId);
    }

    /**
     * Resets all statistics.
     */
    public void reset() {
        totalPacksSkipped.set(0);
        totalPacksSent.set(0);
        playerPacksSkipped.clear();
        hashSkipCount.clear();
    }

    /**
     * Gets a summary string of statistics.
     */
    public String getSummary() {
        long skipped = totalPacksSkipped.get();
        long sent = totalPacksSent.get();
        long total = skipped + sent;
        double skipRate = total > 0 ? (skipped * 100.0 / total) : 0;
        return String.format("Packs: %d sent, %d skipped (%.1f%% skip rate)", sent, skipped, skipRate);
    }
}
