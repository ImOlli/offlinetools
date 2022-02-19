package de.imolli.offlinetools;

import de.imolli.offlinetools.commands.OfflineLocationCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class OfflineToolsPlugin extends JavaPlugin {
    @Getter
    private static OfflineToolsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getPluginCommand("offlinelocation").setExecutor(new OfflineLocationCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
