package io.th0rgal.packlayer.core;

/**
 * Determines how PackLayer decides whether to skip a resource pack send.
 */
public enum SkipMode {
    /**
     * Skip if the SHA-1 hash matches a previously sent pack (default).
     */
    HASH_ONLY,

    /**
     * Skip if the URL matches a previously sent pack.
     */
    URL_ONLY,

    /**
     * Skip if either the hash OR the URL matches.
     */
    HASH_OR_URL,

    /**
     * Skip if both the hash AND the URL match.
     */
    HASH_AND_URL,

    /**
     * Always skip pack sends (useful for testing or specific server setups).
     */
    ALWAYS_SKIP,

    /**
     * Never skip (effectively disables the plugin).
     */
    NEVER_SKIP
}
