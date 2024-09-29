package com.bobbyl140.boulder;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        logger.info("Plugin is working!");
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
                logger.error("Could not create directory for whitelist", e);
            }
        }
        if (Files.exists(whitelistFile)) {
            logger.info("File exists");
            try {
                List<String> lines = Files.readAllLines(whitelistFile);
                whitelist.addAll(lines);
                logger.info("Whitelist loaded with {} entries", whitelist.size());
            } catch (IOException e) {
                logger.error("Failed to load whitelist", e);
            }
        } else {
            logger.info("File does not exist");
            try {
                Files.createFile(whitelistFile);
                logger.info("Whitelist file created");
            } catch (IOException e) {
                logger.error("Failed to create whitelist", e);
            }
        }
    }

    private void saveWhitelist() {
        try {
            Files.write(whitelistFile, whitelist);
            logger.info("Whitelist saved");
        } catch (IOException e) {
            logger.error("Failed to save whitelist", e);
        }
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String username = event.getUsername();

        if (!whitelist.contains(username)) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("You have not been whitelisted on this server. Please contact a moderator for access.")));
            logger.info("Denied login from " + event.getUsername() + " with UUID " + event.getUniqueId() + " and IP " + event.getConnection().getRemoteAddress() + ".");
        }
    }

    public class WhitelistCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] args = invocation.arguments();

            if (args.length == 0) {
                source.sendPlainMessage("Usage: /whitelist add|remove <username>");
                return;
            }

            String action = args[0];

            switch (action.toLowerCase()) {
                case "add":
                    if (args.length < 2) {
                        source.sendPlainMessage("Usage: /whitelist add <username>");
                        return;
                    }
                    String usernameAdd = args[1];
                    if (whitelist.add(usernameAdd)) {
                        saveWhitelist();
                        source.sendMessage(Component.text(usernameAdd + " has been added to the whitelist"));
                    } else {
                        source.sendMessage(Component.text(usernameAdd + " is already on the whitelist!"));
                    }
                    break;
                case "remove":
                    if (args.length < 2) {
                        source.sendPlainMessage("Usage: /whitelist remove <username>");
                        return;
                    }
                    String usernameRemove = args[1];
                    if (whitelist.remove(usernameRemove)) {
                        saveWhitelist();
                        source.sendMessage(Component.text(usernameRemove + " has been removed from the whitelist"));
                    } else {
                        source.sendMessage(Component.text(usernameRemove + " is not on the whitelist!"));
                    }
                    break;
                default:
                    source.sendPlainMessage("Usage: /whitelist add|remove <username>");
            }
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("boulder.use");
        }
    }

}