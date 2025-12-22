package io.th0rgal.packlayer.bungee;

import io.th0rgal.packlayer.core.PackInfo;
import io.th0rgal.packlayer.core.PackLayerService;
import io.th0rgal.packlayer.core.PackStatistics;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PackLayerCommand extends Command implements TabExecutor {

    private final PackLayerService service;

    public PackLayerCommand(@NotNull PackLayerService service) {
        super("layerpack", "layerpack.admin", "lp");
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "stats" -> handleStats(sender);
            case "clear" -> handleClear(sender, args);
            case "info" -> handleInfo(sender, args);
            case "debug" -> handleDebug(sender);
            default -> sendHelp(sender);
        }
    }

    private void handleReload(CommandSender sender) {
        service.reload();
        sendMessage(sender, ChatColor.GREEN + "Configuration reloaded.");
    }

    private void handleStats(CommandSender sender) {
        PackStatistics stats = service.getStatistics();
        sendMessage(sender, ChatColor.GOLD + "=== LayerPack Statistics ===");
        sendMessage(sender, ChatColor.YELLOW + stats.getSummary());
    }

    private void handleClear(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, ChatColor.RED + "Usage: /layerpack clear <player|*>");
            return;
        }

        String target = args[1];

        if (target.equals("*")) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                service.clearPlayer(player.getUniqueId());
            }
            sendMessage(sender, ChatColor.GREEN + "Cleared pack cache for all players.");
        } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(target);
            if (player == null) {
                sendMessage(sender, ChatColor.RED + "Player not found: " + target);
                return;
            }
            service.clearPlayer(player.getUniqueId());
            sendMessage(sender, ChatColor.GREEN + "Cleared pack cache for " + player.getName());
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, ChatColor.RED + "Usage: /layerpack info <player>");
            return;
        }

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
        if (player == null) {
            sendMessage(sender, ChatColor.RED + "Player not found: " + args[1]);
            return;
        }

        Set<PackInfo> packs = service.getPlayerPacks(player.getUniqueId());
        sendMessage(sender, ChatColor.GOLD + "=== Pack info for " + player.getName() + " ===");
        sendMessage(sender, ChatColor.YELLOW + "Cached packs: " + packs.size());

        for (PackInfo pack : packs) {
            String hash = pack.getHash();
            if (hash != null && hash.length() > 16) {
                hash = hash.substring(0, 16) + "...";
            }
            sendMessage(sender, ChatColor.GRAY + "  - Hash: " + hash);
        }

        long skipped = service.getStatistics().getPlayerPacksSkipped(player.getUniqueId());
        sendMessage(sender, ChatColor.YELLOW + "Packs skipped: " + skipped);
    }

    private void handleDebug(CommandSender sender) {
        boolean current = service.getConfig().isDebugMode();
        sendMessage(sender, ChatColor.YELLOW + "Debug mode is currently: " + (current ? "enabled" : "disabled"));
        sendMessage(sender, ChatColor.GRAY + "Toggle it in config.yml and use /layerpack reload");
    }

    private void sendHelp(CommandSender sender) {
        sendMessage(sender, ChatColor.GOLD + "=== LayerPack Commands ===");
        sendMessage(sender, ChatColor.YELLOW + "/layerpack reload" + ChatColor.GRAY + " - Reload configuration");
        sendMessage(sender, ChatColor.YELLOW + "/layerpack stats" + ChatColor.GRAY + " - Show statistics");
        sendMessage(sender, ChatColor.YELLOW + "/layerpack clear <player|*>" + ChatColor.GRAY + " - Clear pack cache");
        sendMessage(sender, ChatColor.YELLOW + "/layerpack info <player>" + ChatColor.GRAY + " - Show player pack info");
        sendMessage(sender, ChatColor.YELLOW + "/layerpack debug" + ChatColor.GRAY + " - Debug mode info");
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(message));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String cmd : List.of("reload", "stats", "clear", "info", "debug")) {
                if (cmd.startsWith(partial)) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("clear") || subCmd.equals("info")) {
                String partial = args[1].toLowerCase();
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    if (player.getName().toLowerCase().startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
                if (subCmd.equals("clear") && "*".startsWith(partial)) {
                    completions.add("*");
                }
            }
        }

        return completions;
    }
}
