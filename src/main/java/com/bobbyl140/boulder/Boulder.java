package com.bobbyl140.boulder;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.kyori.adventure.text.Component;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;

import com.velocitypowered.api.event.connection.PreLoginEvent;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Plugin(
        id = "boulder",
        name = "Boulder",
        version = BuildConstants.VERSION
        , description = "A name-based allowlist for Velocity that supports Bedrock clients through Geyser."
        , authors = {"bobbyl140"}
)
public class Boulder {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Set<String> whitelist = new HashSet<>();
    private final Path whitelistFile;

    @Inject
    public Boulder(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.whitelistFile = dataDirectory.resolve("whitelist.txt");

        loadWhitelist();

        logger.info("Plugin fully loaded!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("whitelist")
                .plugin(this)
                .build();
        SimpleCommand commandToRegister = new WhitelistCommand();
        commandManager.register(commandMeta, commandToRegister);
    }

    private void loadWhitelist() {
        if (!dataDirectory.toFile().exists()) {
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                logger.error("Could not create directory for whitelist.", e);
            }
        }
        if (Files.exists(whitelistFile)) {
            try {
                List<String> lines = Files.readAllLines(whitelistFile);
                whitelist.addAll(lines);
                logger.info("Whitelist loaded with {} entries", whitelist.size());
            } catch (IOException e) {
                logger.error("Failed to load whitelist.", e);
            }
        } else {
            try {
                Files.createFile(whitelistFile);
                logger.info("Whitelist file created.");
            } catch (IOException e) {
                logger.error("Failed to create whitelist.", e);
            }
        }
    }

    private void saveWhitelist() {
        try {
            Files.write(whitelistFile, whitelist);
            logger.info("Whitelist saved.");
        } catch (IOException e) {
            logger.error("Failed to save whitelist.", e);
        }
    }

    private static boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void sendNotificationToStaff(String message) {
        TextComponent stringToSend = Component.text("[Boulder] ", TextColor.color(0x67689E))
                .append(Component.text(message, TextColor.color((0xFFFFFF))));

        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission("boulder.notify")) {
                player.sendMessage(stringToSend);
            }
        }
    }

    public void sendNotificationToStaff(String message, String uuid) {
        TextComponent stringToSend = Component.text("[Boulder] ", TextColor.color(0x67689E))
                .append(Component.text(message, TextColor.color((0xFFFFFF))))
                .clickEvent(ClickEvent.suggestCommand("/whitelist add " + uuid));

        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission("boulder.notify")) {
                player.sendMessage(stringToSend);
            }
        }
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        UUID account = event.getUniqueId();

        if (isValidUUID(account.toString()) && !whitelist.contains(account.toString())) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("You have not been whitelisted on this server. Please contact a moderator for access.")));
            logger.info("User {} with UUID {} tried to login.", event.getUsername(), event.getUniqueId());
            logger.info("To whitelist this user, run the following: /whitelist add " + account);
            sendNotificationToStaff("User " + event.getUsername() + " with UUID " + event.getUniqueId() + " tried to login. Click HERE and press enter to whitelist them.", event.getUniqueId().toString());
        }
    }

    public class WhitelistCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();

            String[] args = invocation.arguments();

            if (args.length == 0) {
                source.sendPlainMessage("Usage: /whitelist add|remove <UUID>");
                return;
            }

            String action = args[0];

            if (!isValidUUID(args[1])) {
                source.sendPlainMessage("Invalid UUID.");
                return;
            }

            switch (action.toLowerCase()) {
                case "add":
                    if (args.length < 2) {
                        source.sendPlainMessage("Usage: /whitelist add <UUID>");
                        return;
                    }
                    String uuidAdd = args[1];
                    if (whitelist.add(uuidAdd)) {
                        saveWhitelist();
                        if (invocation.source() instanceof Player) {
                            sendNotificationToStaff("UUID " + uuidAdd + " has been added to the whitelist by " + ((Player)source).getUsername() + ".");
                        } else if (invocation.source() instanceof ConsoleCommandSource) {
                            sendNotificationToStaff("UUID " + uuidAdd + " has been added to the whitelist by CONSOLE.");
                        }
                    } else {
                        source.sendMessage(Component.text("That player is already on the whitelist!"));
                    }
                    break;
                case "remove":
                    if (args.length < 2) {
                        source.sendPlainMessage("Usage: /whitelist remove <UUID>");
                        return;
                    }
                    String uuidRemove = args[1];
                    if (whitelist.remove(uuidRemove)) {
                        saveWhitelist();
                        if (invocation.source() instanceof Player) {
                            sendNotificationToStaff("UUID " + uuidRemove + " has been removed from the whitelist by " + ((Player)source).getUsername() + ".");
                        } else if (invocation.source() instanceof ConsoleCommandSource) {
                            sendNotificationToStaff("UUID " + uuidRemove + " has been removed from the whitelist by CONSOLE.");
                        }
                    } else {
                        source.sendMessage(Component.text("That player is not on the whitelist!"));
                    }
                    break;
                default:
                    source.sendPlainMessage("Usage: /whitelist add|remove <UUID>");
            }
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("boulder.use");
        }
    }

}