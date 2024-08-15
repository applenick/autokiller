package net.climaxmc.autokiller.checks;

import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Check implements Listener {

    protected final AutoKiller plugin;
    private final String name;

    public final Map<UUID, Long> disableTime = new HashMap<>();
    public final Map<UUID, Integer> vls = new HashMap<>();
    public final Map<UUID, Integer> lastVLs = new HashMap<>();

    public Check(AutoKiller plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public void increaseVL(UUID uuid, int amount) {
        int vl = getVL(uuid);
        if (vl != 0) lastVLs.put(uuid, vl);
        vls.put(uuid, vl + amount);
    }

    public int getVL(UUID uuid) {
        return vls.getOrDefault(uuid, 0);
    }

    public int getLastVL(UUID uuid) {
        return lastVLs.getOrDefault(uuid, 0);
    }

    public void decreaseVL(UUID uuid, int amount) {
        int vl = getVL(uuid);
        if (vl != 0) lastVLs.put(uuid, vl);
        vls.put(uuid, Math.max(vl - amount, 0));
    }

    public void decreaseAllVL(int amount) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            decreaseVL(player.getUniqueId(), amount);
        }
    }

    public void resetVL(UUID uuid) {
        lastVLs.put(uuid, vls.get(uuid));
        vls.put(uuid, 0);
    }

    protected void checkForBan() {
        if (!isEnabled()) {
            vls.clear();
            lastVLs.clear();
            return;
        }
        if (!plugin.config.getBannable()) return;

        int vlBan = plugin.config.getVLBan(this);
        vls.entrySet().removeIf(e -> {
            boolean ban = e.getValue() >= vlBan;
            if (ban) plugin.autoBanPlayer(e.getKey(), getName());
            return ban;
        });
    }

    protected void checkForAlert() {
        if (!isEnabled()) {
            vls.clear();
            lastVLs.clear();
            return;
        }
        int vlAlert = plugin.config.getVLAlert(this);
        vls.forEach((uuid, vl) -> {
            if (vl >= vlAlert && getLastVL(uuid) <= vl) plugin.logCheat(uuid, getName(), vl);
        });
    }

    public String getName() {
        return name;
    }

    public boolean isDisabled(Player player) {
        if (!disableTime.containsKey(player.getUniqueId())) {
            disableTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
        return disableTime.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public boolean isEnabled() {
        return plugin.config.getEnabled(this);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;

        Player player = (Player) event.getEntity();
        disableTime.put(player.getUniqueId(), System.currentTimeMillis() + 1500);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        disableTime.put(player.getUniqueId(), System.currentTimeMillis() + 1500);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer().getUniqueId());
    }

    protected void cleanup(UUID uuid) {
        disableTime.remove(uuid);
        vls.remove(uuid);
        lastVLs.remove(uuid);
    }

}
