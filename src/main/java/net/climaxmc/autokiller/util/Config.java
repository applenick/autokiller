package net.climaxmc.autokiller.util;

import net.climaxmc.autokiller.AutoKiller;
import net.climaxmc.autokiller.checks.Check;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.rmi.server.ExportException;

public class Config {

    private AutoKiller plugin;

    private File file;
    private FileConfiguration config;

    public Config(AutoKiller plugin) {
        this.plugin = plugin;

        reloadConfig();
    }

    public void reloadConfig() {
        try {
            this.file = new File(plugin.getDataFolder(), "config.yml");
            if (!file.exists()) {
                plugin.saveResource("config.yml", false);
            }
            this.config = YamlConfiguration.loadConfiguration(file);
            if (!config.contains("config-version") || (double) config.get("config-version") < 2.5) {
                File old = new File(plugin.getDataFolder(), "config.yml");
                old.renameTo(new File(plugin.getDataFolder(), "old-config-" + 2.5 + ".yml"));
                plugin.saveResource("config.yml", true);
            }
        } catch (Exception e) {
            Bukkit.getLogger().info(e.getMessage() + " Exception thrown while reloading the config!");
        }
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getBannable() {
        if (config.get("ban") == null) {
            config.set("ban", true);
            saveConfig();
        }
        return (boolean) config.get("ban");
    }

    public int getMaxPing() {
        if (config.get("max-ping") == null) {
            config.set("max-ping", 260);
            saveConfig();
        }
        return (int) config.get("max-ping");
    }

    public String getBanCommand() {
        if (config.get("ban-command") == null) {
            config.set("ban-command", "ban %player% &6AutoKiller &f» &cUnfair Advantage");
            saveConfig();
        }
        return (String) config.get("ban-command");
    }
    public String getClickSpeedAlert() {
        if (config.get("clickspeed-alert") == null) {
            config.set("clickspeed-alert", "&8[&6AutoKiller&8] &c%player% &8[&e%ping%ms&8] &7is suspected for &c%cheat% &8[&b%vl% cps&8]");
            saveConfig();
        }
        return (String) config.get("clickspeed-alert");
    }
    public String getNormalAlert() {
        if (config.get("normal-alert") == null) {
            config.set("normal-alert", "&8[&6AutoKiller&8] &c%player% &8[&e%ping%ms&8] &7is suspected for &c%cheat% &7VL:%vl%");
            saveConfig();
        }
        return (String) config.get("normal-alert");
    }

    public boolean getCustomCommand() {
        if (config.get("custom-command") == null) {
            config.set("custom-command", false);
            saveConfig();
        }
        return (boolean) config.get("custom-command");
    }

    public String getAlertCommand() {
        if (config.get("alert-command") == null) {
            config.set("alert-command", "");
            saveConfig();
        }
        return (String) config.get("alert-command");
    }

    public int getMaxSpeed() {
        if (config.get("click-speed.max-speed") == null) {
            config.set("click-speed.max-speed", 16);
            saveConfig();
        }
        return (int) config.get("click-speed.max-speed");
    }

    public int getSensitivity() {
        if (config.get("consistent-clicks.sensitivity") == null) {
            config.set("consistent-clicks.sensitivity", 21);
            saveConfig();
        }
        return (int) config.get("consistent-clicks.sensitivity");
    }

    public boolean getEnabled(Check check) {
        try {
            String name = check.getName().toLowerCase();
            return (boolean) config.get(name + ".enabled");
        } catch (Exception e) {
            Bukkit.getLogger().severe("AutoKiller couldn't get config! Reload the plugin!");
            return true;
        }
    }

    public int getVLAlert(Check check) {
        try {
            String name = check.getName().toLowerCase();
            return (int) config.get(name + ".vl-to-alert");
        } catch (Exception e) {
            Bukkit.getLogger().severe("AutoKiller couldn't get config! Reload the plugin!");
            return 100;
        }
    }
    public int getVLBan(Check check) {
        try {
            String name = check.getName().toLowerCase();
            return (int) config.get(name + ".vl-to-ban");
        } catch (Exception e) {
            Bukkit.getLogger().severe("AutoKiller couldn't get config! Reload the plugin!");
            return 100;
        }
    }
}
