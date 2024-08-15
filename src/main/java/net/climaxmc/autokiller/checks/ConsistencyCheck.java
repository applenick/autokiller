package net.climaxmc.autokiller.checks;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.climaxmc.autokiller.AutoKiller;
import net.climaxmc.autokiller.packets.PacketUseEntityEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConsistencyCheck extends Check implements Listener {

    public ConsistencyCheck(AutoKiller plugin) {
        super(plugin, "Consistent-Clicks");

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
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Consistency 1 Check
     */

    private final Map<UUID, Long> lastTime = new HashMap<>();
    private final Map<UUID, BlockingQueue<Long>> lastDifferences = new HashMap<>();
    private final Map<UUID, Long> lastTimeDifference = new HashMap<>();
    private final Map<UUID, Float> lastTargetYaw = new HashMap<>();

    @EventHandler
    public void onClick(PacketUseEntityEvent event) {
        if (!event.getAttacked().getType().equals(EntityType.PLAYER)) {
            return;
        }
        UUID player = event.getAttacker().getUniqueId();
        UUID target = event.getAttacked().getUniqueId();

        if (event.getAction() == EnumWrappers.EntityUseAction.ATTACK) {
            if (Objects.equals(lastTargetYaw.get(target), event.getAttacked().getLocation().getYaw())) {
                return;
            }
            lastTargetYaw.put(target, event.getAttacked().getLocation().getYaw());

            long plLastTime = lastTime.computeIfAbsent(player, p -> System.currentTimeMillis());
            long plLastTimeDifference = lastTimeDifference.computeIfAbsent(player, p -> 200L);
            BlockingQueue<Long> plLastDifferences = lastDifferences.computeIfAbsent(player,
                    p -> new ArrayBlockingQueue<>(plugin.config.getSensitivity()));
            if (plLastDifferences.remainingCapacity() == 0) plLastDifferences.poll();

            long currentTimeDifference = System.currentTimeMillis() - plLastTime;
            long diff = currentTimeDifference - plLastTimeDifference;
            plLastDifferences.add(Math.abs(diff) > 200 ? 70 : diff);

            lastTimeDifference.put(player, currentTimeDifference);
            lastTime.put(player, System.currentTimeMillis());

            for (Long differences : lastDifferences.get(player)) {
                if (Math.abs(differences) > 70) {
                    return;
                }
            }

            if (plugin.speedCheck.getCps(player) > 6) {
                increaseVL(player, 1);
            }
        }
    }

    @Override
    protected void cleanup(UUID uuid) {
        super.cleanup(uuid);
        lastTime.remove(uuid);
        lastDifferences.remove(uuid);
        lastTimeDifference.remove(uuid);
        lastTargetYaw.remove(uuid);
    }

}
