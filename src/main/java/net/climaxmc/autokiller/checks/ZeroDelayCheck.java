package net.climaxmc.autokiller.checks;

import net.climaxmc.autokiller.AutoKiller;
import net.climaxmc.autokiller.packets.PacketBlockDigEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZeroDelayCheck extends Check implements Listener {

    public ZeroDelayCheck(AutoKiller plugin) {
        super(plugin, "Zero-Delay");

        new BukkitRunnable() {
            @Override
            public void run() {
                checkForAlert();
            }
        }.runTaskTimer(plugin, 0L, 10L);
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForBan();
            }
        }.runTaskTimer(plugin, 0L, 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                decreaseAllVL(1);
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    /**
     * Zero Delay Check
     */

    private final Map<UUID, Long> lastBlockTime = new HashMap<>();
    private final Map<UUID, Vector> lastBlockLocation = new HashMap<>();

    @EventHandler
    public void blockCheck(PacketBlockDigEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        long lastMs = lastBlockTime.getOrDefault(uuid,0L);
        Vector lastBlock = lastBlockLocation.getOrDefault(uuid, null);

        if (event.getBlockLocation().toVector().equals(lastBlock) && System.currentTimeMillis() == lastMs) {
            increaseVL(uuid, 1);
        }
        lastBlockTime.put(uuid, System.currentTimeMillis());
        lastBlockLocation.put(uuid, event.getBlockLocation().toVector());
    }

    @Override
    protected void cleanup(UUID uuid) {
        super.cleanup(uuid);
        lastBlockTime.remove(uuid);
        lastBlockLocation.remove(uuid);
    }
}
