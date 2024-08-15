package net.climaxmc.autokiller.checks;

import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClickSpeedCheck extends Check implements Listener {

    public ClickSpeedCheck(AutoKiller plugin) {
        super(plugin, "Click-Speed");

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
        }.runTaskTimer(plugin, 0L, 20L * 3);
    }

    public int getCps(UUID uuid) {
        return clicks.getOrDefault(uuid, 0);
    }

    /**
     * Click Speed Check
     */

    private final Map<UUID, Integer> clicks = new HashMap<>();

    @EventHandler
    public void speedCheck(PlayerInteractEvent event) {
        if (!event.getPlayer().getType().equals(EntityType.PLAYER)) return;
        if (!event.getAction().equals(Action.LEFT_CLICK_AIR)) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.getItemInHand() != null && player.getItemInHand().getType().equals(Material.FISHING_ROD)) {
            return;
        }

        if (clicks.compute(uuid, (p, c) -> (c != null ? c : 0) + 1) == 1) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Integer currClicks = clicks.get(uuid);

                if (currClicks >= plugin.config.getMaxSpeed()) {
                    if (!isEnabled()) {
                        resetVL(player.getUniqueId());
                        return;
                    }
                    plugin.logCheat(uuid, getName(), currClicks);
                    increaseVL(uuid, 3);

                }
                clicks.put(uuid, 0);
            }, 20L);
        }
    }

    @Override
    protected void cleanup(UUID uuid) {
        super.cleanup(uuid);
        clicks.remove(uuid);
    }

}
