package com.framework.config;

import org.aeonbits.owner.ConfigFactory;

/**
 * Singleton accessor for {@link FrameworkConfig}.
 * Use: ConfigManager.get().browser()
 */
public final class ConfigManager {

    private static FrameworkConfig config;

    private ConfigManager() {}

    public static synchronized FrameworkConfig get() {
        if (config == null) {
            config = ConfigFactory.create(FrameworkConfig.class, System.getProperties(), System.getenv());
        }
        return config;
    }
}
