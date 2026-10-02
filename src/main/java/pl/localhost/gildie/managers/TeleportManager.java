package pl.localhost.gildie.managers;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.localhost.gildie.Main;
import pl.localhost.gildie.objects.Guild;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final Main plugin;
    private final Map<UUID, BukkitTask> activeTeleports = new HashMap<>();

    public TeleportManager(Main plugin) {
        this.plugin = plugin;
    }

    public void startTeleport(Player player, Guild guild) {
        if (guild.getBase() == null) {
            player.sendMessage(plugin.getConfigManager().color("&cTwoja gildia nie ma ustawionej bazy!"));
            return;
        }

        cancelTeleport(player.getUniqueId());

        int delay = plugin.getConfigManager().getTeleportDelay();
        Location startLoc = player.getLocation().clone();

        player.sendMessage(plugin.getConfigManager().getMessage("teleport-started")
                .replace("%time%", String.valueOf(delay)));

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = delay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTeleports.remove(player.getUniqueId());
                    return;
                }

                if (player.getLocation().distance(startLoc) > 0.5) {
                    player.sendMessage(plugin.getConfigManager().getMessage("teleport-cancelled"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    cancel();
                    activeTeleports.remove(player.getUniqueId());
                    return;
                }

                if (timeLeft <= 0) {
                    player.teleport(guild.getBase());
                    player.sendMessage(plugin.getConfigManager().getMessage("teleport-success"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    cancel();
                    activeTeleports.remove(player.getUniqueId());
                } else {
                    player.sendActionBar(plugin.getConfigManager().color("&aTeleportacja za: &e" + timeLeft + "s"));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.4f);
                    timeLeft--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        activeTeleports.put(player.getUniqueId(), task);
    }

    public void cancelTeleport(UUID uuid) {
        BukkitTask task = activeTeleports.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isTeleporting(UUID uuid) {
        return activeTeleports.containsKey(uuid);
    }
}
