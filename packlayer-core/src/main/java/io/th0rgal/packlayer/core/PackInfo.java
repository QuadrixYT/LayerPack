package io.th0rgal.packlayer.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Information about a resource pack.
 */
public final class PackInfo {

    private final String url;
    private final String hash;
    private final UUID packUuid;
    private final long timestamp;

    public PackInfo(@Nullable String url, @Nullable String hash, @Nullable UUID packUuid) {
        this.url = url;
        this.hash = hash;
        this.packUuid = packUuid;
        this.timestamp = System.currentTimeMillis();
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getHash() {
        return hash;
    }

    @Nullable
    public UUID getPackUuid() {
        return packUuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if this pack matches another based on the given skip mode.
     */
    public boolean matches(@NotNull PackInfo other, @NotNull SkipMode mode) {
        return switch (mode) {
            case HASH_ONLY -> hashMatches(other);
            case URL_ONLY -> urlMatches(other);
            case HASH_OR_URL -> hashMatches(other) || urlMatches(other);
            case HASH_AND_URL -> hashMatches(other) && urlMatches(other);
            case ALWAYS_SKIP -> true;
            case NEVER_SKIP -> false;
        };
    }

    private boolean hashMatches(@NotNull PackInfo other) {
        if (hash == null || hash.isEmpty() || other.hash == null || other.hash.isEmpty()) {
            return false;
        }
        return hash.equalsIgnoreCase(other.hash);
    }

    private boolean urlMatches(@NotNull PackInfo other) {
        if (url == null || url.isEmpty() || other.url == null || other.url.isEmpty()) {
            return false;
        }
        return url.equals(other.url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackInfo packInfo = (PackInfo) o;
        // Consider both hash and URL to ensure packs with different URLs but null hashes
        // are tracked separately (important for URL_ONLY and HASH_OR_URL skip modes)
        return Objects.equals(hash, packInfo.hash) && Objects.equals(url, packInfo.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, url);
    }

    @Override
    public String toString() {
        return "PackInfo{url='" + url + "', hash='" + hash + "', uuid=" + packUuid + "}";
    }
}
