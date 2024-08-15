package net.climaxmc.autokiller.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Packet method based on Janitor.
 */

public class PacketCore {
    public AutoKiller plugin;

    public PacketCore(AutoKiller plugin) {
        this.plugin = plugin;
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY) {
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                if (player == null) {
                    return;
                }
                EnumWrappers.EntityUseAction type = packet.getEntityUseActions().read(0);
                int entityId = packet.getIntegers().read(0);
                // Run in main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Entity entity = null;
                    for (World worlds : Bukkit.getWorlds()) {
                        for (Entity entities : worlds.getEntities()) {
                            if (entities.getEntityId() == entityId) {
                                entity = entities;
                            }
                        }
                    }
                    if (entity == null) {
                        entity = player;
                    }
                    Bukkit.getServer().getPluginManager().callEvent(new PacketUseEntityEvent(type, player, entity));
                });
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.BLOCK_DIG) {
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                if (player == null) {
                    return;
                }
                Location blockLocation;
                if (Bukkit.getServer().getVersion().contains("1.7")) {
                    blockLocation = new Location(player.getWorld(), packet.getIntegers().read(0),
                            packet.getIntegers().read(1),
                            packet.getIntegers().read(2));
                } else {
                    blockLocation = new Location(player.getWorld(), packet.getBlockPositionModifier().read(0).getX(),
                            packet.getBlockPositionModifier().read(0).getY(),
                            packet.getBlockPositionModifier().read(0).getZ());
                }
                Bukkit.getServer().getPluginManager().callEvent(new PacketBlockDigEvent(player, blockLocation));
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_STATUS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_VELOCITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                event.setCancelled(false);
            }
        });
    }
}