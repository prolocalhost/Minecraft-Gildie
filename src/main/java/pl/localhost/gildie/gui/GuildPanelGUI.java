package pl.localhost.gildie.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.localhost.gildie.Main;
import pl.localhost.gildie.objects.Guild;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GuildPanelGUI implements Listener {

    private final Main plugin;
    private static final String GUI_IDENTIFIER = "§0";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public GuildPanelGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void openPanel(Player player, Guild guild) {
        String title = plugin.getConfigManager().color("&8Panel Gildii &7[" + guild.getTag() + "]") + GUI_IDENTIFIER;
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            bg.setItemMeta(bgMeta);
        }
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, bg);
        }

        ItemStack info = new ItemStack(Material.NETHER_STAR);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(plugin.getConfigManager().color("&6&lInformacje o Gildii"));
            List<String> lore = new ArrayList<>();
            lore.add(plugin.getConfigManager().color("&7Tag: &e" + guild.getTag()));
            lore.add(plugin.getConfigManager().color("&7Nazwa: &f" + guild.getName()));
            Player leaderP = Bukkit.getPlayer(guild.getLeader());
            String leaderName = (leaderP != null) ? leaderP.getName() : "Offline";
            lore.add(plugin.getConfigManager().color("&7Lider: &e" + leaderName));
            lore.add(plugin.getConfigManager().color("&7Członkowie: &a" + guild.getMembers().size()));
            lore.add(plugin.getConfigManager().color("&7Punkty ELO: &b" + guild.getPoints()));
            lore.add(plugin.getConfigManager().color("&7Życia: &c" + guild.getLives() + " &7/ &c3"));
            lore.add(plugin.getConfigManager().color("&7Ważność do: &e" + dateFormat.format(new Date(guild.getExpireTime()))));
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        inv.setItem(11, info);

        ItemStack base = new ItemStack(Material.ENDER_PEARL);
        ItemMeta baseMeta = base.getItemMeta();
        if (baseMeta != null) {
            baseMeta.setDisplayName(plugin.getConfigManager().color("&a&lTeleport do Bazy"));
            List<String> lore = new ArrayList<>();
            lore.add(plugin.getConfigManager().color("&7Kliknij, aby rozpocząć teleportację"));
            lore.add(plugin.getConfigManager().color("&7do bazy Twojej gildii."));
            lore.add("");
            lore.add(plugin.getConfigManager().color("&8» &eKliknij, aby się przeteleportować"));
            baseMeta.setLore(lore);
            base.setItemMeta(baseMeta);
        }
        inv.setItem(13, base);

        ItemStack pvp = new ItemStack(guild.isPvp() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta pvpMeta = pvp.getItemMeta();
        if (pvpMeta != null) {
            pvpMeta.setDisplayName(plugin.getConfigManager().color("&c&lPvP w Gildii (Friendly Fire)"));
            List<String> lore = new ArrayList<>();
            lore.add(plugin.getConfigManager().color("&7Status: " + (guild.isPvp() ? "&aWłączone (Można się bić)" : "&cWyłączone (Ochrona sojuszników)")));
            lore.add("");
            if (guild.isLeader(player.getUniqueId()) || guild.isOfficer(player.getUniqueId())) {
                lore.add(plugin.getConfigManager().color("&8» &eKliknij, aby przełączyć!"));
            } else {
                lore.add(plugin.getConfigManager().color("&cTylko Lider lub Zastępca może to zmienić."));
            }
            pvpMeta.setLore(lore);
            pvp.setItemMeta(pvpMeta);
        }
        inv.setItem(15, pvp);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().endsWith(GUI_IDENTIFIER)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Guild guild = plugin.getGuildManager().getGuildByPlayer(player.getUniqueId());
        if (guild == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();

        if (slot == 13) {
            player.closeInventory();
            plugin.getTeleportManager().startTeleport(player, guild);
            return;
        }

        if (slot == 15) {
            if (!guild.isLeader(player.getUniqueId()) && !guild.isOfficer(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            guild.setPvp(!guild.isPvp());
            plugin.getGuildManager().saveGuilds();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.5f);

            if (guild.isPvp()) {
                player.sendMessage(plugin.getConfigManager().getMessage("pvp-enabled"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("pvp-disabled"));
            }

            openPanel(player, guild);
        }
    }
}
