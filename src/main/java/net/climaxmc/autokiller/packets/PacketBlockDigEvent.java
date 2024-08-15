package net.climaxmc.autokiller.packets;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketBlockDigEvent extends Event {

    private final Player player;
    private final Location blockLocation;
    private static final HandlerList handlers = new HandlerList();

    public PacketBlockDigEvent(Player player, Location blockLocation)
    {
        this.player = player;
        this.blockLocation = blockLocation;
    }

    public Player getPlayer()
    {
        return this.player;
    }

    public Location getBlockLocation() {
        return this.blockLocation;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
