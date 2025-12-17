package io.th0rgal.packlayer.core;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Configuration for PackLayer plugin.
 */
public final class PackLayerConfig {

    private static final String CONFIG_FILE = "config.yml";

    private SkipMode skipMode = SkipMode.HASH_ONLY;
    private ServerFilterMode serverFilterMode = ServerFilterMode.DISABLED;
    private Set<String> serverList = new HashSet<>();
    private List<Pattern> trustedDomainPatterns = new ArrayList<>();
    private List<String> trustedDomains = new ArrayList<>();
    private long serverSwitchGraceMs = 0;
    private boolean debugMode = false;
    private boolean statisticsEnabled = true;

    private final Path dataFolder;

    public PackLayerConfig(@NotNull Path dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void load() {
        Path configPath = dataFolder.resolve(CONFIG_FILE);

        if (!Files.exists(configPath)) {
            saveDefault();
        }

        try (InputStream in = Files.newInputStream(configPath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(in);
            if (data == null) {
                data = new HashMap<>();
            }
            parseConfig(data);
        } catch (IOException e) {
            // Use defaults
        }
    }

    @SuppressWarnings("unchecked")
    private void parseConfig(Map<String, Object> data) {
        // Skip mode
        String skipModeStr = getString(data, "skip-mode", "HASH_ONLY");
        try {
            skipMode = SkipMode.valueOf(skipModeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            skipMode = SkipMode.HASH_ONLY;
        }

        // Server filter - reset to defaults first to handle section removal on reload
        serverFilterMode = ServerFilterMode.DISABLED;
        serverList = new HashSet<>();
        Map<String, Object> serverFilter = getMap(data, "server-filter");
        if (serverFilter != null) {
            String modeStr = getString(serverFilter, "mode", "DISABLED");
            try {
                serverFilterMode = ServerFilterMode.valueOf(modeStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                serverFilterMode = ServerFilterMode.DISABLED;
            }

            List<String> servers = getList(serverFilter, "servers");
            for (String server : servers) {
                serverList.add(server.toLowerCase(Locale.ROOT));
            }
        }

        // Trusted domains
        trustedDomains = getList(data, "trusted-domains");
        trustedDomainPatterns.clear();
        for (String domain : trustedDomains) {
            try {
                String regex = domain
                        .replace(".", "\\.")
                        .replace("*", ".*");
                trustedDomainPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                // Invalid pattern, skip
            }
        }

        // Server switch grace
        serverSwitchGraceMs = getLong(data, "server-switch-grace-ms", 0);

        // Debug mode
        debugMode = getBoolean(data, "debug", false);

        // Statistics
        statisticsEnabled = getBoolean(data, "statistics-enabled", true);
    }

    public void saveDefault() {
        try {
            Files.createDirectories(dataFolder);
            Path configPath = dataFolder.resolve(CONFIG_FILE);

            String defaultConfig = """
                    # PackLayer Configuration
                    # Optimizes resource pack distribution on proxy networks

                    # Skip Mode - determines how PackLayer decides to skip duplicate packs
                    # Options:
                    #   HASH_ONLY    - Skip if SHA-1 hash matches (default, recommended)
                    #   URL_ONLY     - Skip if URL matches
                    #   HASH_OR_URL  - Skip if either hash OR URL matches
                    #   HASH_AND_URL - Skip only if both hash AND URL match
                    #   ALWAYS_SKIP  - Always skip pack sends (testing only)
                    #   NEVER_SKIP   - Never skip (effectively disables plugin)
                    skip-mode: HASH_ONLY

                    # Server Filter - control which backend servers PackLayer applies to
                    server-filter:
                      # Mode: DISABLED (all servers), WHITELIST, or BLACKLIST
                      mode: DISABLED
                      # List of server names (as defined in Velocity/BungeeCord config)
                      servers:
                        - lobby
                        - hub

                    # Trusted Domains - always skip packs from these domains (matches host only)
                    # Supports subdomain wildcards: *.example.com matches cdn.example.com
                    trusted-domains:
                      # - atlas.oraxen.com
                      # - cdn.example.com

                    # Grace period (ms) after server switch before applying skip logic
                    # Set to 0 to disable. Useful if backend servers intentionally re-send packs.
                    server-switch-grace-ms: 0

                    # Enable debug logging
                    debug: false

                    # Enable statistics tracking (packs skipped, bandwidth saved)
                    statistics-enabled: true
                    """;

            Files.writeString(configPath, defaultConfig);
        } catch (IOException e) {
            // Ignore, will use defaults
        }
    }

    // Getters
    public SkipMode getSkipMode() {
        return skipMode;
    }

    public ServerFilterMode getServerFilterMode() {
        return serverFilterMode;
    }

    public Set<String> getServerList() {
        return serverList;
    }

    public boolean isServerAllowed(String serverName) {
        if (serverFilterMode == ServerFilterMode.DISABLED) {
            return true;
        }
        boolean inList = serverList.contains(serverName.toLowerCase(Locale.ROOT));
        return serverFilterMode == ServerFilterMode.WHITELIST ? inList : !inList;
    }

    public boolean isTrustedDomain(String url) {
        if (trustedDomainPatterns.isEmpty() || url == null || url.isEmpty()) {
            return false;
        }

        // Extract host from URL to prevent matching domain patterns in other URL parts
        String host;
        try {
            URI uri = new URI(url);
            host = uri.getHost();
            if (host == null || host.isEmpty()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        for (Pattern pattern : trustedDomainPatterns) {
            // Use matches() on the host only, requiring full host match
            if (pattern.matcher(host).matches()) {
                return true;
            }
        }
        return false;
    }

    public long getServerSwitchGraceMs() {
        return serverSwitchGraceMs;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    // Helper methods for parsing
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> getList(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    private String getString(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private boolean getBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    private long getLong(Map<String, Object> data, String key, long defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }
}
