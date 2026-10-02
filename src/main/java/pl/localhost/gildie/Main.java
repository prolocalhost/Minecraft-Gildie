package pl.localhost.gildie;

import org.bukkit.plugin.java.JavaPlugin;
import pl.localhost.gildie.commands.GuildAdminCommand;
import pl.localhost.gildie.commands.GuildCommand;
import pl.localhost.gildie.gui.GuildItemsGUI;
import pl.localhost.gildie.gui.GuildPanelGUI;
import pl.localhost.gildie.listeners.GuildListener;
import pl.localhost.gildie.managers.ConfigManager;
import pl.localhost.gildie.managers.GuildManager;
import pl.localhost.gildie.managers.TeleportManager;

public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private GuildManager guildManager;
    private TeleportManager teleportManager;
    private GuildPanelGUI guildPanelGUI;
    private GuildItemsGUI guildItemsGUI;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.guildManager = new GuildManager(this);
        this.teleportManager = new TeleportManager(this);
        this.guildPanelGUI = new GuildPanelGUI(this);
        this.guildItemsGUI = new GuildItemsGUI(this);

        GuildCommand guildCmd = new GuildCommand(this);
        if (getCommand("gildia") != null) {
            getCommand("gildia").setExecutor(guildCmd);
            getCommand("gildia").setTabCompleter(guildCmd);
        }

        GuildAdminCommand adminCmd = new GuildAdminCommand(this);
        if (getCommand("gadmin") != null) {
            getCommand("gadmin").setExecutor(adminCmd);
            getCommand("gadmin").setTabCompleter(adminCmd);
        }

        if (getCommand("koszyk") != null) {
            getCommand("koszyk").setExecutor((sender, command, label, args) -> {
                if (sender instanceof org.bukkit.entity.Player) {
                    guildItemsGUI.openGUI((org.bukkit.entity.Player) sender);
                } else {
                    sender.sendMessage(configManager.getMessage("player-only"));
                }
                return true;
            });
        }

        getServer().getPluginManager().registerEvents(new GuildListener(this), this);
        getServer().getPluginManager().registerEvents(guildPanelGUI, this);
        getServer().getPluginManager().registerEvents(guildItemsGUI, this);

        getLogger().info("gildie smigaja jak nalezy!");
    }

    @Override
    public void onDisable() {
        if (this.guildManager != null) {
            this.guildManager.saveGuilds();
        }
        getLogger().info("gildie wylaczone!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public GuildPanelGUI getGuildPanelGUI() {
        return guildPanelGUI;
    }

    public GuildItemsGUI getGuildItemsGUI() {
        return guildItemsGUI;
    }
}
