package pl.localhost.gildie.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pl.localhost.gildie.Main;
import pl.localhost.gildie.objects.Guild;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuildAdminCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public GuildAdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("gildie.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().color("&8---------- &cAdministracja Gildii &8----------"));
            sender.sendMessage(plugin.getConfigManager().color(" &8» &e/ga usun <tag> &7- Usuwa gildię"));
            sender.sendMessage(plugin.getConfigManager().color(" &8» &e/ga tp <tag> &7- Teleportuje do serca gildii"));
            sender.sendMessage(plugin.getConfigManager().color(" &8» &e/ga punkty <tag> <set/add/take> <ilość> &7- Zarządza punktami ELO"));
            sender.sendMessage(plugin.getConfigManager().color(" &8» &e/ga zycia <tag> <set/add/take> <ilość> &7- Zarządza życiami"));
            sender.sendMessage(plugin.getConfigManager().color(" &8» &e/ga reload &7- Przeładowuje plik config.yml"));
            sender.sendMessage(plugin.getConfigManager().color("&8--------------------------------------------"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getMessage("reload"));
            return true;
        }

        if (sub.equals("usun") && args.length >= 2) {
            String tag = args[1].toUpperCase();
            Guild g = plugin.getGuildManager().getGuildByTag(tag);
            if (g == null) {
                sender.sendMessage(plugin.getConfigManager().color("&cNie znaleziono gildii o tagu: " + tag));
                return true;
            }

            plugin.getGuildManager().deleteGuild(tag);
            sender.sendMessage(plugin.getConfigManager().color("&aPomyślnie usunięto gildię &e[" + tag + "]&a!"));
            return true;
        }

        if (sub.equals("tp") && args.length >= 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().color("&cTa komenda jest tylko dla graczy!"));
                return true;
            }

            String tag = args[1].toUpperCase();
            Guild g = plugin.getGuildManager().getGuildByTag(tag);
            if (g == null || g.getHeart() == null) {
                sender.sendMessage(plugin.getConfigManager().color("&cNie znaleziono gildii lub serca!"));
                return true;
            }

            ((Player) sender).teleport(g.getHeart());
            sender.sendMessage(plugin.getConfigManager().color("&aPrzeteleportowano do serca gildii &e[" + tag + "]&a!"));
            return true;
        }

        if (sub.equals("punkty") && args.length >= 4) {
            String tag = args[1].toUpperCase();
            Guild g = plugin.getGuildManager().getGuildByTag(tag);
            if (g == null) {
                sender.sendMessage(plugin.getConfigManager().color("&cNie znaleziono gildii: " + tag));
                return true;
            }

            try {
                int amount = Integer.parseInt(args[3]);
                String action = args[2].toLowerCase();
                if (action.equals("set")) g.setPoints(amount);
                else if (action.equals("add")) g.addPoints(amount);
                else if (action.equals("take")) g.removePoints(amount);

                plugin.getGuildManager().saveGuilds();
                sender.sendMessage(plugin.getConfigManager().color("&aZaktualizowano punkty gildii &e[" + tag + "] &ana: &b" + g.getPoints()));
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getConfigManager().color("&cPodaj poprawną liczbę!"));
            }
            return true;
        }

        if (sub.equals("zycia") && args.length >= 4) {
            String tag = args[1].toUpperCase();
            Guild g = plugin.getGuildManager().getGuildByTag(tag);
            if (g == null) {
                sender.sendMessage(plugin.getConfigManager().color("&cNie znaleziono gildii: " + tag));
                return true;
            }

            try {
                int amount = Integer.parseInt(args[3]);
                String action = args[2].toLowerCase();
                if (action.equals("set")) g.setLives(amount);
                else if (action.equals("add")) g.setLives(g.getLives() + amount);
                else if (action.equals("take")) g.setLives(Math.max(0, g.getLives() - amount));

                plugin.getGuildManager().saveGuilds();
                sender.sendMessage(plugin.getConfigManager().color("&aZaktualizowano życia gildii &e[" + tag + "] &ana: &c" + g.getLives()));
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getConfigManager().color("&cPodaj poprawną liczbę!"));
            }
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().color("&cZłe użycie! Wpisz &e/ga &caby zobaczyć pomoc."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (!sender.hasPermission("gildie.admin")) return list;

        if (args.length == 1) {
            list.addAll(Arrays.asList("usun", "tp", "punkty", "zycia", "reload"));
            return filter(list, args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("usun") || args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("punkty") || args[0].equalsIgnoreCase("zycia"))) {
            return filter(new ArrayList<>(plugin.getGuildManager().getGuilds().keySet()), args[1]);
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("punkty") || args[0].equalsIgnoreCase("zycia"))) {
            return filter(Arrays.asList("set", "add", "take"), args[2]);
        }

        return list;
    }

    private List<String> filter(List<String> list, String query) {
        String lower = query.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
