package pl.localhost.gildie.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pl.localhost.gildie.Main;
import pl.localhost.gildie.objects.Guild;

import java.util.*;
import java.util.stream.Collectors;

public class GuildCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public GuildCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;
        Guild playerGuild = plugin.getGuildManager().getGuildByPlayer(player.getUniqueId());

        if (args.length == 0) {
            if (playerGuild != null) {
                plugin.getGuildPanelGUI().openPanel(player, playerGuild);
            } else {
                sendHelp(player);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("pomoc") || sub.equals("help")) {
            sendHelp(player);
            return true;
        }

        if (sub.equals("zaloz") || sub.equals("stworz")) {
            if (playerGuild != null) {
                player.sendMessage(plugin.getConfigManager().getMessage("already-in-guild"));
                return true;
            }

            if (args.length < 3) {
                player.sendMessage(plugin.getConfigManager().color("&7Użycie: &e/g zaloz <tag> <nazwa>"));
                return true;
            }

            String tag = args[1].toUpperCase();
            String name = args[2];

            int minTag = plugin.getConfigManager().getTagMinLength();
            int maxTag = plugin.getConfigManager().getTagMaxLength();
            if (tag.length() < minTag || tag.length() > maxTag || !tag.matches("[A-Z0-9]+")) {
                String msg = plugin.getConfigManager().getMessage("tag-invalid")
                        .replace("%min%", String.valueOf(minTag))
                        .replace("%max%", String.valueOf(maxTag));
                player.sendMessage(msg);
                return true;
            }

            int minName = plugin.getConfigManager().getNameMinLength();
            int maxName = plugin.getConfigManager().getNameMaxLength();
            if (name.length() < minName || name.length() > maxName) {
                String msg = plugin.getConfigManager().getMessage("name-invalid")
                        .replace("%min%", String.valueOf(minName))
                        .replace("%max%", String.valueOf(maxName));
                player.sendMessage(msg);
                return true;
            }

            if (plugin.getGuildManager().getGuildByTag(tag) != null) {
                player.sendMessage(plugin.getConfigManager().getMessage("tag-exists"));
                return true;
            }

            Location loc = player.getLocation();
            Location spawnLoc = new Location(loc.getWorld(), 0, loc.getY(), 0);
            int minSpawnDist = plugin.getConfigManager().getMinDistanceFromSpawn();
            if (loc.distance(spawnLoc) < minSpawnDist) {
                String msg = plugin.getConfigManager().getMessage("spawn-too-close")
                        .replace("%dist%", String.valueOf(minSpawnDist));
                player.sendMessage(msg);
                return true;
            }

            if (plugin.getGuildManager().isClaimOverlapping(loc)) {
                player.sendMessage(plugin.getConfigManager().getMessage("claim-overlap"));
                return true;
            }

            if (!plugin.getGuildManager().hasCost(player)) {
                player.sendMessage(plugin.getConfigManager().getMessage("not-enough-items"));
                return true;
            }

            plugin.getGuildManager().removeCost(player);
            plugin.getGuildManager().createGuild(player, tag, name);

            String createdMsg = plugin.getConfigManager().getMessage("created")
                    .replace("%tag%", tag)
                    .replace("%name%", name);
            player.sendMessage(createdMsg);

            String bcast = plugin.getConfigManager().getMessage("created-broadcast")
                    .replace("%player%", player.getName())
                    .replace("%tag%", tag)
                    .replace("%name%", name);
            Bukkit.broadcastMessage(bcast);
            return true;
        }

        if (sub.equals("dolacz") || sub.equals("join")) {
            if (playerGuild != null) {
                player.sendMessage(plugin.getConfigManager().getMessage("already-in-guild"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(plugin.getConfigManager().color("&7Użycie: &e/g dolacz <tag>"));
                return true;
            }

            String tag = args[1].toUpperCase();
            String pendingTag = plugin.getGuildManager().getPendingInvites().get(player.getUniqueId());

            if (pendingTag == null || !pendingTag.equalsIgnoreCase(tag)) {
                String msg = plugin.getConfigManager().getMessage("not-invited").replace("%tag%", tag);
                player.sendMessage(msg);
                return true;
            }

            Guild g = plugin.getGuildManager().getGuildByTag(tag);
            if (g == null) {
                player.sendMessage(plugin.getConfigManager().color("&cTa gildia już nie istnieje!"));
                return true;
            }

            g.addMember(player.getUniqueId());
            plugin.getGuildManager().getPendingInvites().remove(player.getUniqueId());
            plugin.getGuildManager().saveGuilds();

            player.sendMessage(plugin.getConfigManager().getMessage("joined").replace("%tag%", tag));
            String bcast = plugin.getConfigManager().getMessage("joined-broadcast")
                    .replace("%player%", player.getName())
                    .replace("%tag%", tag);
            Bukkit.broadcastMessage(bcast);
            return true;
        }

        if (sub.equals("top") || sub.equals("ranking")) {
            List<Guild> sorted = new ArrayList<>(plugin.getGuildManager().getGuilds().values());
            sorted.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints()));

            player.sendMessage(plugin.getConfigManager().color("&8---------- &6Ranking Gildii (TOP 10) &8----------"));
            for (int i = 0; i < Math.min(10, sorted.size()); i++) {
                Guild g = sorted.get(i);
                player.sendMessage(plugin.getConfigManager().color(" &8» &e#" + (i + 1) + " &8[&e" + g.getTag() + "&8] &f" +
                        g.getName() + " &7- &b" + g.getPoints() + " pkt ELO &7(&c" + g.getLives() + " żyć&7)"));
            }
            player.sendMessage(plugin.getConfigManager().color("&8-------------------------------------------"));
            return true;
        }

        if (sub.equals("info")) {
            Guild targetGuild = playerGuild;
            if (args.length >= 2) {
                targetGuild = plugin.getGuildManager().getGuildByTag(args[1]);
            }

            if (targetGuild == null) {
                player.sendMessage(plugin.getConfigManager().color("&cNie znaleziono takiej gildii!"));
                return true;
            }

            player.sendMessage(plugin.getConfigManager().color("&8---------- &6Gildia [" + targetGuild.getTag() + "] &8----------"));
            player.sendMessage(plugin.getConfigManager().color(" &8» &7Nazwa: &f" + targetGuild.getName()));
            Player l = Bukkit.getPlayer(targetGuild.getLeader());
            player.sendMessage(plugin.getConfigManager().color(" &8» &7Lider: &e" + (l != null ? l.getName() : "Offline")));
            player.sendMessage(plugin.getConfigManager().color(" &8» &7Punkty: &b" + targetGuild.getPoints()));
            player.sendMessage(plugin.getConfigManager().color(" &8» &7Życia: &c" + targetGuild.getLives() + " / 3"));
            player.sendMessage(plugin.getConfigManager().color(" &8» &7Członkowie (&a" + targetGuild.getMembers().size() + "&7): &f" +
                    targetGuild.getMembers().stream().map(u -> {
                        Player p = Bukkit.getPlayer(u);
                        return p != null ? p.getName() : u.toString().substring(0, 5);
                    }).collect(Collectors.joining(", "))));
            player.sendMessage(plugin.getConfigManager().color("&8-------------------------------------------"));
            return true;
        }

        if (sub.equals("koszyk") || sub.equals("itemy") || sub.equals("przedmioty") || sub.equals("items")) {
            plugin.getGuildItemsGUI().openGUI(player);
            return true;
        }

        if (playerGuild == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("not-in-guild"));
            return true;
        }

        if (sub.equals("panel")) {
            plugin.getGuildPanelGUI().openPanel(player, playerGuild);
            return true;
        }

        if (sub.equals("baza") || sub.equals("dom") || sub.equals("home")) {
            plugin.getTeleportManager().startTeleport(player, playerGuild);
            return true;
        }

        if (sub.equals("ustawbaze")) {
            if (!playerGuild.isLeader(player.getUniqueId()) && !playerGuild.isOfficer(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
                return true;
            }

            int radius = plugin.getConfigManager().getRegionRadius();
            if (!playerGuild.isInClaim(player.getLocation(), radius)) {
                player.sendMessage(plugin.getConfigManager().getMessage("base-must-be-in-claim"));
                return true;
            }

            playerGuild.setBase(player.getLocation());
            plugin.getGuildManager().saveGuilds();
            player.sendMessage(plugin.getConfigManager().getMessage("base-set"));
            return true;
        }

        if (sub.equals("pvp")) {
            if (!playerGuild.isLeader(player.getUniqueId()) && !playerGuild.isOfficer(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
                return true;
            }

            playerGuild.setPvp(!playerGuild.isPvp());
            plugin.getGuildManager().saveGuilds();

            if (playerGuild.isPvp()) {
                player.sendMessage(plugin.getConfigManager().getMessage("pvp-enabled"));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("pvp-disabled"));
            }
            return true;
        }

        if (sub.equals("zapros") || sub.equals("invite")) {
            if (!playerGuild.isLeader(player.getUniqueId()) && !playerGuild.isOfficer(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(plugin.getConfigManager().color("&7Użycie: &e/g zapros <gracz>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(plugin.getConfigManager().color("&cPodany gracz jest offline!"));
                return true;
            }

            plugin.getGuildManager().getPendingInvites().put(target.getUniqueId(), playerGuild.getTag());

            player.sendMessage(plugin.getConfigManager().getMessage("invited-sender").replace("%player%", target.getName()));
            String msg = plugin.getConfigManager().getMessage("invited-target")
                    .replace("%tag%", playerGuild.getTag())
                    .replace("%name%", playerGuild.getName());
            target.sendMessage(msg);
            return true;
        }

        if (sub.equals("wyrzuc") || sub.equals("kick")) {
            if (!playerGuild.isLeader(player.getUniqueId()) && !playerGuild.isOfficer(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("not-officer"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(plugin.getConfigManager().color("&7Użycie: &e/g wyrzuc <gracz>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            UUID targetUuid = target != null ? target.getUniqueId() : null;

            if (targetUuid == null) {
                for (UUID u : playerGuild.getMembers()) {
                    Player p = Bukkit.getPlayer(u);
                    if (p != null && p.getName().equalsIgnoreCase(args[1])) {
                        targetUuid = u;
                        break;
                    }
                }
            }

            if (targetUuid == null || !playerGuild.isMember(targetUuid)) {
                player.sendMessage(plugin.getConfigManager().color("&cTen gracz nie jest w Twojej gildii!"));
                return true;
            }

            if (playerGuild.isLeader(targetUuid)) {
                player.sendMessage(plugin.getConfigManager().color("&cNie możesz wyrzucić Lidera gildii!"));
                return true;
            }

            playerGuild.removeMember(targetUuid);
            plugin.getGuildManager().saveGuilds();

            player.sendMessage(plugin.getConfigManager().getMessage("kicked").replace("%player%", args[1]));
            if (target != null && target.isOnline()) {
                target.sendMessage(plugin.getConfigManager().getMessage("kicked-target").replace("%tag%", playerGuild.getTag()));
            }
            return true;
        }

        if (sub.equals("opusc") || sub.equals("leave")) {
            if (playerGuild.isLeader(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("leader-cannot-leave"));
                return true;
            }

            playerGuild.removeMember(player.getUniqueId());
            plugin.getGuildManager().saveGuilds();

            player.sendMessage(plugin.getConfigManager().getMessage("left").replace("%tag%", playerGuild.getTag()));
            String bcast = plugin.getConfigManager().getMessage("left-broadcast")
                    .replace("%player%", player.getName())
                    .replace("%tag%", playerGuild.getTag());
            Bukkit.broadcastMessage(bcast);
            return true;
        }

        if (sub.equals("rozwiaz") || sub.equals("disband")) {
            if (!playerGuild.isLeader(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("not-leader"));
                return true;
            }

            String tag = playerGuild.getTag();
            String name = playerGuild.getName();
            plugin.getGuildManager().deleteGuild(tag);

            String bcast = plugin.getConfigManager().getMessage("disbanded-broadcast")
                    .replace("%player%", player.getName())
                    .replace("%tag%", tag)
                    .replace("%name%", name);
            Bukkit.broadcastMessage(bcast);
            return true;
        }

        if (sub.equals("lider")) {
            if (!playerGuild.isLeader(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessage("not-leader"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(plugin.getConfigManager().color("&7Użycie: &e/g lider <gracz>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !playerGuild.isMember(target.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().color("&cTen gracz musi być w Twojej gildii i być online!"));
                return true;
            }

            playerGuild.setLeader(target.getUniqueId());
            plugin.getGuildManager().saveGuilds();

            String msg = plugin.getConfigManager().getMessage("leader-changed")
                    .replace("%tag%", playerGuild.getTag())
                    .replace("%player%", target.getName());
            Bukkit.broadcastMessage(msg);
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getConfigManager().color("&8---------- &6System Gildii &8----------"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g zaloz <tag> <nazwa> &7- Zakłada nową gildię"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g panel &7- Otwiera menu zarządzania gildią"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g baza &7- Teleportuje do bazy gildii"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g ustawbaze &7- Ustawia nową bazę gildii"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g zapros <gracz> &7- Zaprasza gracza do gildii"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g dolacz <tag> &7- Dołącza do zaproszonej gildii"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g wyrzuc <gracz> &7- Wyrzuca gracza z gildii"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g opusc &7- Opuszcza obecną gildię"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g rozwiaz &7- Rozwiązuje Twoją gildię"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g pvp &7- Przełącza Friendly Fire w gildii"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g info [tag] &7- Wyświetla informacje o gildii"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g top &7- Ranking 10 najlepszych gildii"));
        player.sendMessage(plugin.getConfigManager().color(" &8» &e/g koszyk &7- Sprawdza przedmioty wymagane na gildię"));
        player.sendMessage(plugin.getConfigManager().color("&8-------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (!(sender instanceof Player)) return list;

        if (args.length == 1) {
            list.addAll(Arrays.asList("zaloz", "koszyk", "panel", "baza", "ustawbaze", "zapros", "dolacz", "wyrzuc", "opusc", "rozwiaz", "lider", "pvp", "info", "top"));
            return filter(list, args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("zapros") || args[0].equalsIgnoreCase("wyrzuc") || args[0].equalsIgnoreCase("lider"))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("dolacz") || args[0].equalsIgnoreCase("info"))) {
            return filter(new ArrayList<>(plugin.getGuildManager().getGuilds().keySet()), args[1]);
        }

        return list;
    }

    private List<String> filter(List<String> list, String query) {
        String lower = query.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
