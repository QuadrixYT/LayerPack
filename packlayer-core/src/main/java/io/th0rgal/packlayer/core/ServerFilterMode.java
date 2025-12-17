package io.th0rgal.packlayer.core;

/**
 * Determines how server filtering works.
 */
public enum ServerFilterMode {
    /**
     * No filtering - apply to all servers.
     */
    DISABLED,

    /**
     * Only apply to servers in the list.
     */
    WHITELIST,

    /**
     * Apply to all servers except those in the list.
     */
    BLACKLIST
}
