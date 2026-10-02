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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuildItemsGUI implements Listener {

    private final Main plugin;
    private static final String GUI_IDENTIFIER = "§0";

    public GuildItemsGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        String title = plugin.getConfigManager().color("&8Przedmioty na Gildię") + GUI_IDENTIFIER;
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

        Map<Material, Integer> cost = plugin.getConfigManager().getCostItems();
        int[] slots = {10, 12, 14, 16};
        int index = 0;
        boolean hasAll = true;

        for (Map.Entry<Material, Integer> entry : cost.entrySet()) {
            Material mat = entry.getKey();
            int req = entry.getValue();

            int inInv = 0;
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && is.getType() == mat) {
                    inInv += is.getAmount();
                }
            }

            if (inInv < req) {
                hasAll = false;
            }

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(plugin.getConfigManager().color("&6&l" + mat.name().replace("_", " ")));
                List<String> lore = new ArrayList<>();
                lore.add(plugin.getConfigManager().color("&7Wymagana ilość: &e" + req + " szt."));
                lore.add(plugin.getConfigManager().color("&7Posiadasz w EQ: &b" + inInv + " &7/ &b" + req));
                lore.add("");
                if (inInv >= req) {
                    lore.add(plugin.getConfigManager().color("&a✔ POSIADASZ WYSTARCZAJĄCO"));
                } else {
                    int missing = req - inInv;
                    lore.add(plugin.getConfigManager().color("&c✖ BRAKUJE: &e" + missing + " szt."));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            if (index < slots.length) {
                inv.setItem(slots[index], item);
                index++;
            }
        }

        ItemStack statusItem = new ItemStack(hasAll ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        ItemMeta statusMeta = statusItem.getItemMeta();
        if (statusMeta != null) {
            if (hasAll) {
                statusMeta.setDisplayName(plugin.getConfigManager().color("&a&lMOŻESZ ZAŁOŻYĆ GILDIĘ!"));
                List<String> lore = new ArrayList<>();
                lore.add(plugin.getConfigManager().color("&7Posiadasz wszystkie wymagane przedmioty."));
                lore.add(plugin.getConfigManager().color("&7Wpisz: &e/g zaloz <tag> <nazwa>"));
                statusMeta.setLore(lore);
            } else {
                statusMeta.setDisplayName(plugin.getConfigManager().color("&c&lBRAKUJE PRZEDMIOTÓW!"));
                List<String> lore = new ArrayList<>();
                lore.add(plugin.getConfigManager().color("&7Zdobądź brakujące surowce,"));
                lore.add(plugin.getConfigManager().color("&7aby móc założyć własną gildię."));
                statusMeta.setLore(lore);
            }
            statusItem.setItemMeta(statusMeta);
        }
        inv.setItem(22, statusItem);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().endsWith(GUI_IDENTIFIER)) {
            event.setCancelled(true);
        }
    }
}
