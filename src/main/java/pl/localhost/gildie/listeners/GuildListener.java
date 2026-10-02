package pl.localhost.gildie.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.localhost.gildie.Main;
import pl.localhost.gildie.objects.Guild;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GuildListener implements Listener {

    private final Main plugin;
    private final Map<UUID, String> lastRegion = new HashMap<>();

    public GuildListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Guild guild = plugin.getGuildManager().getGuildAt(block.getLocation());

        if (guild == null) {
            return;
        }

        if (guild.isMember(player.getUniqueId())) {
            if (guild.getHeart() != null && guild.getHeart().equals(block.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getMessage("heart-protected"));
            }
            return;
        }

        if (guild.getHeart() != null && guild.getHeart().equals(block.getLocation())) {
            event.setCancelled(true);
            handleHeartAttack(guild, player);
            return;
        }

        if (!player.hasPermission("gildie.admin")) {
            event.setCancelled(true);
            String msg = plugin.getConfigManager().getMessage("territory-protected")
                    .replace("%tag%", guild.getTag())
                    .replace("%name%", guild.getName());
            player.sendMessage(msg);
        }
    }

    private void handleHeartAttack(Guild guild, Player attacker) {
        Location loc = guild.getHeart();
        loc.getWorld().spawnParticle(Particle.LAVA, loc.clone().add(0.5, 1.0, 0.5), 20);
        loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);

        String alarmMsg = plugin.getConfigManager().getMessage("heart-attacked")
                .replace("%tag%", guild.getTag())
                .replace("%player%", attacker.getName());
        for (UUID u : guild.getMembers()) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                p.sendMessage(alarmMsg);
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.0f);
            }
        }

        int newLives = guild.getLives() - 1;
        guild.setLives(newLives);

        if (newLives <= 0) {
            String conquered = plugin.getConfigManager().getMessage("heart-conquered")
                    .replace("%tag%", guild.getTag())
                    .replace("%player%", attacker.getName());
            Bukkit.broadcastMessage(conquered);
            plugin.getGuildManager().deleteGuild(guild.getTag());
        } else {
            String lostMsg = plugin.getConfigManager().getMessage("heart-lost-life")
                    .replace("%tag%", guild.getTag())
                    .replace("%lives%", String.valueOf(newLives));
            Bukkit.broadcastMessage(lostMsg);
            plugin.getGuildManager().saveGuilds();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Guild guild = plugin.getGuildManager().getGuildAt(event.getBlock().getLocation());

        if (guild == null || guild.isMember(player.getUniqueId())) {
            return;
        }

        if (!player.hasPermission("gildie.admin")) {
            event.setCancelled(true);
            String msg = plugin.getConfigManager().getMessage("territory-protected")
                    .replace("%tag%", guild.getTag())
                    .replace("%name%", guild.getName());
            player.sendMessage(msg);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Block block = event.getClickedBlock();
        Material mat = block.getType();

        boolean isContainer = mat.name().contains("CHEST") || mat.name().contains("SHULKER") ||
                mat.name().contains("DOOR") || mat.name().contains("BUTTON") || mat.name().contains("LEVER") ||
                mat == Material.FURNACE || mat == Material.BLAST_FURNACE || mat == Material.SMOKER ||
                mat == Material.HOPPER || mat == Material.BARREL || mat == Material.ANVIL;

        if (!isContainer) return;

        Guild guild = plugin.getGuildManager().getGuildAt(block.getLocation());
        if (guild == null || guild.isMember(event.getPlayer().getUniqueId())) {
            return;
        }

        if (!event.getPlayer().hasPermission("gildie.admin")) {
            event.setCancelled(true);
            String msg = plugin.getConfigManager().getMessage("territory-protected")
                    .replace("%tag%", guild.getTag())
                    .replace("%name%", guild.getName());
            event.getPlayer().sendMessage(msg);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = null;

        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }

        plugin.getTeleportManager().cancelTeleport(victim.getUniqueId());

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        Guild victimGuild = plugin.getGuildManager().getGuildByPlayer(victim.getUniqueId());
        Guild attackerGuild = plugin.getGuildManager().getGuildByPlayer(attacker.getUniqueId());

        if (victimGuild != null && attackerGuild != null) {
            if (victimGuild.equals(attackerGuild)) {
                if (!victimGuild.isPvp()) {
                    event.setCancelled(true);
                    attacker.sendMessage(plugin.getConfigManager().color("&cFriendly Fire w Twojej gildii jest wyłączony!"));
                    return;
                }
            }

            if (victimGuild.isAlly(attackerGuild.getTag())) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getConfigManager().color("&cTa gildia to Twój sojusznik!"));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) return;

        Guild victimGuild = plugin.getGuildManager().getGuildByPlayer(victim.getUniqueId());
        Guild killerGuild = plugin.getGuildManager().getGuildByPlayer(killer.getUniqueId());

        if (victimGuild != null && killerGuild != null && !victimGuild.equals(killerGuild)) {
            int gain = plugin.getConfigManager().getConfig().getInt("points.kill-gain", 30);
            int loss = plugin.getConfigManager().getConfig().getInt("points.death-loss", 20);

            killerGuild.addPoints(gain);
            victimGuild.removePoints(loss);
            plugin.getGuildManager().saveGuilds();

            killer.sendMessage(plugin.getConfigManager().color("&aTwoja gildia zdobyła &e+" + gain + " pkt ELO &aza zabicie gracza &e" + victim.getName() + "&a!"));
            victim.sendMessage(plugin.getConfigManager().color("&cTwoja gildia straciła &e-" + loss + " pkt ELO &aza śmierć z rąk &e" + killer.getName() + "&c!"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Guild toGuild = plugin.getGuildManager().getGuildAt(event.getTo());
        String currentTag = (toGuild != null) ? toGuild.getTag() : null;
        String prevTag = lastRegion.get(player.getUniqueId());

        if (Objects.equals(currentTag, prevTag)) {
            return;
        }

        lastRegion.put(player.getUniqueId(), currentTag);

        if (toGuild != null) {
            String msg = plugin.getConfigManager().color("&aWkroczyłeś na teren gildii &8[&e" + toGuild.getTag() + "&8] &f" + toGuild.getName());
            player.sendActionBar(msg);
        } else if (prevTag != null) {
            String msg = plugin.getConfigManager().color("&7Opuściłeś teren gildii &8[&e" + prevTag + "&8]");
            player.sendActionBar(msg);
        }
    }
}
