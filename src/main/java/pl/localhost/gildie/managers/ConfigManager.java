package pl.localhost.gildie.managers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pl.localhost.gildie.Main;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Main plugin;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getPrefix() {
        return color(getConfig().getString("messages.prefix", "&8[&6Gildie&8] &7"));
    }

    public String getMessage(String path) {
        String msg = getConfig().getString("messages." + path, "&cWiadomość nieznaleziona: " + path);
        return getPrefix() + color(msg);
    }

    public String getRawMessage(String path) {
        String msg = getConfig().getString("messages." + path, "");
        return color(msg);
    }

    public int getRegionRadius() {
        return getConfig().getInt("settings.region-radius", 35);
    }

    public int getMinDistanceBetweenGuilds() {
        return getConfig().getInt("settings.min-distance-between-guilds", 80);
    }

    public int getMinDistanceFromSpawn() {
        return getConfig().getInt("settings.min-distance-from-spawn", 150);
    }

    public int getTeleportDelay() {
        return getConfig().getInt("settings.teleport-delay", 5);
    }

    public int getTagMinLength() {
        return getConfig().getInt("settings.tag-min-length", 2);
    }

    public int getTagMaxLength() {
        return getConfig().getInt("settings.tag-max-length", 5);
    }

    public int getNameMinLength() {
        return getConfig().getInt("settings.name-min-length", 3);
    }

    public int getNameMaxLength() {
        return getConfig().getInt("settings.name-max-length", 16);
    }

    public Material getHeartMaterial() {
        String matName = getConfig().getString("settings.heart-block", "CRYING_OBSIDIAN");
        Material mat = Material.matchMaterial(matName);
        return mat != null ? mat : Material.CRYING_OBSIDIAN;
    }

    public boolean isCostEnabled() {
        return getConfig().getBoolean("cost.enabled", true);
    }

    public Map<Material, Integer> getCostItems() {
        Map<Material, Integer> items = new HashMap<>();
        ConfigurationSection sec = getConfig().getConfigurationSection("cost.items");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    items.put(mat, sec.getInt(key, 1));
                }
            }
        }
        return items;
    }
}
