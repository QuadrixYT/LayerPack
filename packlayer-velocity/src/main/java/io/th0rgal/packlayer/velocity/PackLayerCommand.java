package io.th0rgal.packlayer.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.th0rgal.packlayer.core.PackInfo;
import io.th0rgal.packlayer.core.PackLayerService;
import io.th0rgal.packlayer.core.PackStatistics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class PackLayerCommand implements SimpleCommand {

    private final ProxyServer server;
    private final PackLayerService service;

    public PackLayerCommand(@NotNull ProxyServer server, @NotNull PackLayerService service) {
        this.server = server;
        this.service = service;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sendHelp(source);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(source);
            case "stats" -> handleStats(source);
            case "clear" -> handleClear(source, args);
            case "info" -> handleInfo(source, args);
            case "debug" -> handleDebug(source);
            default -> sendHelp(source);
        }
    }

    private void handleReload(CommandSource source) {
        service.reload();
        source.sendMessage(Component.text("Configuration reloaded.", NamedTextColor.GREEN));
    }

    private void handleStats(CommandSource source) {
        PackStatistics stats = service.getStatistics();
        source.sendMessage(Component.text("=== LayerPack Statistics ===", NamedTextColor.GOLD));
        source.sendMessage(Component.text(stats.getSummary(), NamedTextColor.YELLOW));
    }

    private void handleClear(CommandSource source, String[] args) {
        if (args.length < 2) {
            source.sendMessage(Component.text("Usage: /layerpack clear <player|*>", NamedTextColor.RED));
            return;
        }

        String target = args[1];

        if (target.equals("*")) {
            for (Player player : server.getAllPlayers()) {
                service.clearPlayer(player.getUniqueId());
            }
            source.sendMessage(Component.text("Cleared pack cache for all players.", NamedTextColor.GREEN));
        } else {
            Optional<Player> playerOpt = server.getPlayer(target);
            if (playerOpt.isEmpty()) {
                source.sendMessage(Component.text("Player not found: " + target, NamedTextColor.RED));
                return;
            }
            Player player = playerOpt.get();
            service.clearPlayer(player.getUniqueId());
            source.sendMessage(Component.text("Cleared pack cache for " + player.getUsername(), NamedTextColor.GREEN));
        }
    }

    private void handleInfo(CommandSource source, String[] args) {
        if (args.length < 2) {
            source.sendMessage(Component.text("Usage: /layerpack info <player>", NamedTextColor.RED));
            return;
        }

        Optional<Player> playerOpt = server.getPlayer(args[1]);
        if (playerOpt.isEmpty()) {
            source.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
            return;
        }

        Player player = playerOpt.get();
        Set<PackInfo> packs = service.getPlayerPacks(player.getUniqueId());

        source.sendMessage(Component.text("=== Pack info for " + player.getUsername() + " ===", NamedTextColor.GOLD));
        source.sendMessage(Component.text("Cached packs: " + packs.size(), NamedTextColor.YELLOW));

        for (PackInfo pack : packs) {
            String hash = pack.getHash();
            if (hash != null && hash.length() > 16) {
                hash = hash.substring(0, 16) + "...";
            }
            source.sendMessage(Component.text("  - Hash: " + hash, NamedTextColor.GRAY));
        }

        long skipped = service.getStatistics().getPlayerPacksSkipped(player.getUniqueId());
        source.sendMessage(Component.text("Packs skipped: " + skipped, NamedTextColor.YELLOW));
    }

    private void handleDebug(CommandSource source) {
        boolean current = service.getConfig().isDebugMode();
        source.sendMessage(Component.text("Debug mode is currently: " + (current ? "enabled" : "disabled"), NamedTextColor.YELLOW));
        source.sendMessage(Component.text("Toggle it in config.yml and use /layerpack reload", NamedTextColor.GRAY));
    }

    private void sendHelp(CommandSource source) {
        source.sendMessage(Component.text("=== LayerPack Commands ===", NamedTextColor.GOLD));
        source.sendMessage(Component.text("/layerpack reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload configuration", NamedTextColor.GRAY)));
        source.sendMessage(Component.text("/layerpack stats", NamedTextColor.YELLOW)
                .append(Component.text(" - Show statistics", NamedTextColor.GRAY)));
        source.sendMessage(Component.text("/layerpack clear <player|*>", NamedTextColor.YELLOW)
                .append(Component.text(" - Clear pack cache", NamedTextColor.GRAY)));
        source.sendMessage(Component.text("/layerpack info <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Show player pack info", NamedTextColor.GRAY)));
        source.sendMessage(Component.text("/layerpack debug", NamedTextColor.YELLOW)
                .append(Component.text(" - Debug mode info", NamedTextColor.GRAY)));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("layerpack.admin");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> completions = new ArrayList<>();

        if (args.length <= 1) {
            String partial = args.length == 1 ? args[0].toLowerCase() : "";
            for (String cmd : List.of("reload", "stats", "clear", "info", "debug")) {
                if (cmd.startsWith(partial)) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("clear") || subCmd.equals("info")) {
                String partial = args[1].toLowerCase();
                for (Player player : server.getAllPlayers()) {
                    if (player.getUsername().toLowerCase().startsWith(partial)) {
                        completions.add(player.getUsername());
                    }
                }
                if (subCmd.equals("clear") && "*".startsWith(partial)) {
                    completions.add("*");
                }
            }
        }

        return CompletableFuture.completedFuture(completions);
    }
}
