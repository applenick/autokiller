package net.climaxmc.autokiller;

import net.climaxmc.autokiller.checks.ClickSpeedCheck;
import net.climaxmc.autokiller.checks.ConsistencyCheck;
import net.climaxmc.autokiller.checks.ZeroDelayCheck;
import net.climaxmc.autokiller.commands.AutoKillerCommand;
import net.climaxmc.autokiller.events.AutoKillCheatEvent;
import net.climaxmc.autokiller.packets.PacketCore;
import net.climaxmc.autokiller.util.Config;
import net.climaxmc.autokiller.util.LogFile;
import net.climaxmc.autokiller.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class AutoKiller extends JavaPlugin {

    public static AutoKiller instance;

    public Config config;

    public void onEnable() {
        instance = this;

        config = new Config(this);

        new PacketCore(this);

        this.getServer().getPluginManager().registerEvents(new ClickSpeedCheck(this), this);
        this.getServer().getPluginManager().registerEvents(new ConsistencyCheck(this), this);
        this.getServer().getPluginManager().registerEvents(new ZeroDelayCheck(this), this);

        this.getCommand("autokiller").setExecutor(new AutoKillerCommand(this));
    }

    @SuppressWarnings("deprecation")
    public void logCheat(UUID uuid, String cheat, int vl) {
        Player player = Bukkit.getPlayer(uuid);

        if (Utils.getPing(player) > config.getMaxPing()) {
            return;
        }
        
    	String alert = cheat.equals("Click-Speed") ? config.getClickSpeedAlert() : config.getNormalAlert();
    	alert = alert.replace("%player%", player.getName())
    			.replace("%ping%", Utils.getPing(player) + "")
    			.replace("%cheat%", cheat)
    			.replace("%vl%", vl + "");
    	alert = ChatColor.translateAlternateColorCodes('&', alert);

        if (config.getCustomCommand()) {
            String command = config.getAlertCommand()
                .replace("%player%", player.getName())
                .replace("%alert%", alert);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (players.isOp() || players.hasPermission("autokiller.staff")) {
                    players.sendMessage(alert);                
                }
            }
        }
        
        // Call Event to broadcast violation to other plugins
        getServer().getPluginManager().callEvent(new AutoKillCheatEvent(uuid, vl, alert));

        LogFile logFile = new LogFile(this);
        DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        Date dateobj = new Date();
        String date = df.format(dateobj.getTime());
        logFile.write(player, "[" + date + "] " + player.getName() + " [" + Utils.getPing(player) + "] failed check " + cheat + " VL:" + vl);
    }

    public ArrayList<UUID> playersToBeBannedUnlessCanceledYay = new ArrayList<>();

    public void autoBanPlayer(UUID uuid, String type, String reason) {

        if (playersToBeBannedUnlessCanceledYay.contains(uuid)) {
            return;
        }
        if (!config.getBannable()) {
            return;
        }
        if (Utils.getPing(Bukkit.getPlayer(uuid)) > config.getMaxPing()) {
            return;
        }

        playersToBeBannedUnlessCanceledYay.add(uuid);
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (players.isOp() || players.hasPermission("autokiller.staff")) {
                if (type.equals("AutoBan")) {

                    String bannedName = Bukkit.getOfflinePlayer(uuid).getName();

                    players.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6AutoKiller&8] &cAuto-Banning player &e" + bannedName + " &cfor &e" + reason));

                    Bukkit.getLogger().log(Level.INFO, "[AutoKiller] [Auto Ban] Banning player " + bannedName);
                }
            }
        }
        this.getServer().getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                for (UUID uuids : playersToBeBannedUnlessCanceledYay) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuids);
                    String banCommand = config.getBanCommand().replace("%player%", player.getName());
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), banCommand);

                    LogFile logFile = new LogFile(AutoKiller.instance);
                    DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    Date dateobj = new Date();
                    String date = df.format(dateobj.getTime());
                    logFile.write(player.getPlayer(), "[" + date + "] " + player.getName() + " was AutoBanned");
                }
            }
        }, 20L);
        this.getServer().getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                ArrayList<UUID> temp = new ArrayList<>();
                for (UUID uuids : playersToBeBannedUnlessCanceledYay) {
                    temp.add(uuids);
                }
                for (UUID uuids : temp) {
                    playersToBeBannedUnlessCanceledYay.remove(uuids);
                }
            }
        }, 20L * 2);
    }
}
