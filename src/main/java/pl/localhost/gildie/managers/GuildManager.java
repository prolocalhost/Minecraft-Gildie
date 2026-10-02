package pl.localhost.gildie.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.localhost.gildie.Main;
import pl.localhost.gildie.objects.Guild;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GuildManager {

    private final Main plugin;
    private final Map<String, Guild> guilds = new HashMap<>();
    private final Map<UUID, String> playerGuilds = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();

    private final File file;
    private FileConfiguration config;

    public GuildManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "gildie.yml");
        loadGuilds();
    }

    public void loadGuilds() {
        guilds.clear();
        playerGuilds.clear();

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Nie mozna utworzyc pliku gildie.yml: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = config.getConfigurationSection("gildie");
        if (sec == null) return;

        for (String tag : sec.getKeys(false)) {
            String name = sec.getString(tag + ".name");
            String leaderStr = sec.getString(tag + ".leader");
            if (leaderStr == null) continue;
            UUID leader = UUID.fromString(leaderStr);

            Location heart = sec.getLocation(tag + ".heart");
            Location base = sec.getLocation(tag + ".base");

            Guild guild = new Guild(tag, name, leader, heart, base);
            guild.setPoints(sec.getInt(tag + ".points", 1000));
            guild.setLives(sec.getInt(tag + ".lives", 3));
            guild.setPvp(sec.getBoolean(tag + ".pvp", false));
            guild.setExpireTime(sec.getLong(tag + ".expireTime", System.currentTimeMillis() + (7L * 24 * 3600 * 1000)));

            List<String> memList = sec.getStringList(tag + ".members");
            for (String m : memList) {
                try {
                    UUID u = UUID.fromString(m);
                    guild.addMember(u);
                    playerGuilds.put(u, tag);
                } catch (IllegalArgumentException ignored) {}
            }

            List<String> offList = sec.getStringList(tag + ".officers");
            for (String o : offList) {
                try {
                    guild.getOfficers().add(UUID.fromString(o));
                } catch (IllegalArgumentException ignored) {}
            }

            List<String> allyList = sec.getStringList(tag + ".allies");
            guild.getAllies().addAll(allyList);

            guilds.put(tag.toUpperCase(), guild);
            playerGuilds.put(leader, tag.toUpperCase());
        }
    }

    public void saveGuilds() {
        if (config == null) return;
        config.set("gildie", null);

        for (Guild g : guilds.values()) {
            String path = "gildie." + g.getTag();
            config.set(path + ".name", g.getName());
            config.set(path + ".leader", g.getLeader().toString());
            config.set(path + ".heart", g.getHeart());
            config.set(path + ".base", g.getBase());
            config.set(path + ".points", g.getPoints());
            config.set(path + ".lives", g.getLives());
            config.set(path + ".pvp", g.isPvp());
            config.set(path + ".expireTime", g.getExpireTime());

            List<String> memList = new ArrayList<>();
            for (UUID u : g.getMembers()) memList.add(u.toString());
            config.set(path + ".members", memList);

            List<String> offList = new ArrayList<>();
            for (UUID u : g.getOfficers()) offList.add(u.toString());
            config.set(path + ".officers", offList);

            config.set(path + ".allies", new ArrayList<>(g.getAllies()));
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Blad podczas zapisu gildie.yml: " + e.getMessage());
        }
    }

    public boolean createGuild(Player player, String tag, String name) {
        String upperTag = tag.toUpperCase();
        Location loc = player.getLocation().getBlock().getLocation();

        Material heartMat = plugin.getConfigManager().getHeartMaterial();
        loc.getBlock().setType(heartMat);

        Guild guild = new Guild(upperTag, name, player.getUniqueId(), loc, loc);
        guilds.put(upperTag, guild);
        playerGuilds.put(player.getUniqueId(), upperTag);

        saveGuilds();
        return true;
    }

    public void deleteGuild(String tag) {
        String upper = tag.toUpperCase();
        Guild g = guilds.remove(upper);
        if (g != null) {
            if (g.getHeart() != null) {
                g.getHeart().getBlock().setType(Material.AIR);
            }
            for (UUID u : g.getMembers()) {
                playerGuilds.remove(u);
            }
            saveGuilds();
        }
    }

    public Guild getGuildByPlayer(UUID uuid) {
        String tag = playerGuilds.get(uuid);
        return tag != null ? guilds.get(tag) : null;
    }

    public Guild getGuildByTag(String tag) {
        if (tag == null) return null;
        return guilds.get(tag.toUpperCase());
    }

    public Guild getGuildAt(Location loc) {
        int radius = plugin.getConfigManager().getRegionRadius();
        for (Guild g : guilds.values()) {
            if (g.isInClaim(loc, radius)) {
                return g;
            }
        }
        return null;
    }

    public boolean isClaimOverlapping(Location loc) {
        int radius = plugin.getConfigManager().getRegionRadius();
        int minDistance = plugin.getConfigManager().getMinDistanceBetweenGuilds();

        for (Guild g : guilds.values()) {
            if (g.getHeart() != null && g.getHeart().getWorld().equals(loc.getWorld())) {
                double dist = g.getHeart().distance(loc);
                if (dist < (radius * 2) || dist < minDistance) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasCost(Player player) {
        if (!plugin.getConfigManager().isCostEnabled()) return true;
        Map<Material, Integer> cost = plugin.getConfigManager().getCostItems();

        for (Map.Entry<Material, Integer> entry : cost.entrySet()) {
            if (!player.getInventory().containsAtLeast(new ItemStack(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public void removeCost(Player player) {
        if (!plugin.getConfigManager().isCostEnabled()) return;
        Map<Material, Integer> cost = plugin.getConfigManager().getCostItems();

        for (Map.Entry<Material, Integer> entry : cost.entrySet()) {
            player.getInventory().removeItem(new ItemStack(entry.getKey(), entry.getValue()));
        }
    }

    public Map<String, Guild> getGuilds() {
        return Collections.unmodifiableMap(guilds);
    }

    public Map<UUID, String> getPendingInvites() {
        return pendingInvites;
    }
}
