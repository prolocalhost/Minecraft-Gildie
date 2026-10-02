package pl.localhost.gildie.objects;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Guild {

    private final String tag;
    private String name;
    private UUID leader;
    private final Set<UUID> officers = new HashSet<>();
    private final Set<UUID> members = new HashSet<>();
    private Location heart;
    private Location base;
    private int points;
    private int lives;
    private boolean pvp;
    private final Set<String> allies = new HashSet<>();
    private long expireTime;

    public Guild(String tag, String name, UUID leader, Location heart, Location base) {
        this.tag = tag.toUpperCase();
        this.name = name;
        this.leader = leader;
        this.heart = heart;
        this.base = base;
        this.points = 1000;
        this.lives = 3;
        this.pvp = false;
        this.members.add(leader);
        this.expireTime = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
        this.members.add(leader);
        this.officers.remove(leader);
    }

    public Set<UUID> getOfficers() {
        return officers;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Location getHeart() {
        return heart;
    }

    public void setHeart(Location heart) {
        this.heart = heart;
    }

    public Location getBase() {
        return base;
    }

    public void setBase(Location base) {
        this.base = base;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int amount) {
        this.points += amount;
    }

    public void removePoints(int amount) {
        this.points = Math.max(0, this.points - amount);
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public Set<String> getAllies() {
        return allies;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public boolean isOfficer(UUID uuid) {
        return officers.contains(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isAlly(String otherTag) {
        return allies.contains(otherTag.toUpperCase());
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        officers.remove(uuid);
    }

    public boolean isInClaim(Location loc, int radius) {
        if (heart == null || loc.getWorld() == null || heart.getWorld() == null) {
            return false;
        }
        if (!heart.getWorld().equals(loc.getWorld())) {
            return false;
        }

        int dx = Math.abs(loc.getBlockX() - heart.getBlockX());
        int dz = Math.abs(loc.getBlockZ() - heart.getBlockZ());

        return dx <= radius && dz <= radius;
    }
}
