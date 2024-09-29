package com.bobbyl140.boulder;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
        id = "boulder",
        name = "Boulder",
        version = BuildConstants.VERSION
        , description = "A name-based allowlist for Velocity that supports Bedrock clients through Geyser."
        , authors = {"bobbyl140"}
)
public class Boulder {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
