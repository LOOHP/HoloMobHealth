package com.loohp.holomobhealth;

import com.loohp.holomobhealth.database.Database;
import com.loohp.holomobhealth.modules.RangeModule;
import com.loohp.holomobhealth.protocol.EntityMetadata;
import com.loohp.holomobhealth.updater.Updater;
import com.loohp.holomobhealth.updater.Updater.UpdaterResponse;
import com.loohp.holomobhealth.utils.ChatColorUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("holomobhealth") && !label.equalsIgnoreCase("hmh")) {
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.AQUA + "HoloMobHealth written by LOOHP!");
            sender.sendMessage(ChatColor.GOLD + "You are running HoloMobHealth version: " + HoloMobHealth.plugin.getDescription().getVersion());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("holomobhealth.reload")) {
                HoloMobHealth.loadConfig();
                RangeModule.reloadNumbers();
                for (World world : Bukkit.getWorlds()) {
                    List<Player> playersInWorld = world.getPlayers();
                    for (Entity entity : world.getEntities()) {
                        EntityMetadata.updateEntity(playersInWorld, entity);
                    }
                }
                sender.sendMessage(HoloMobHealth.reloadPluginMessage);
            } else {
                sender.sendMessage(HoloMobHealth.noPermissionMessage);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (sender.hasPermission("holomobhealth.toggle")) {
                if (args.length == 1 || args[1].equalsIgnoreCase(sender.getName())) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (Database.toggle(player)) {
                            sender.sendMessage(HoloMobHealth.toggleDisplayOnMessage);
                        } else {
                            sender.sendMessage(HoloMobHealth.toggleDisplayOffMessage);
                        }
                    } else {
                        sender.sendMessage(HoloMobHealth.playersOnlyMessage);
                    }
                } else {
                    if (sender.hasPermission("holomobhealth.toggle.others")) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player != null) {
                            if (Database.toggle(player)) {
                                sender.sendMessage(HoloMobHealth.toggleDisplayOnMessage);
                            } else {
                                sender.sendMessage(HoloMobHealth.toggleDisplayOffMessage);
                            }
                        } else {
                            sender.sendMessage(HoloMobHealth.playersNotFoundMessage);
                        }
                    } else {
                        sender.sendMessage(HoloMobHealth.noPermissionMessage);
                    }
                }
            } else {
                sender.sendMessage(HoloMobHealth.noPermissionMessage);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("update")) {
            if (sender.hasPermission("holomobhealth.update")) {
                sender.sendMessage(ChatColor.AQUA + "[HoloMobHealth] HoloMobHealth written by LOOHP!");
                sender.sendMessage(ChatColor.GOLD + "[HoloMobHealth] You are running HoloMobHealth version: " + HoloMobHealth.plugin.getDescription().getVersion());
                Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> {
                    UpdaterResponse version = Updater.checkUpdate();
                    if (version.getResult().equals("latest")) {
                        if (version.isDevBuildLatest()) {
                            sender.sendMessage(ChatColor.GREEN + "[HoloMobHealth] You are running the latest version!");
                        } else {
                            Updater.sendUpdateMessage(sender, version.getResult(), version.getSpigotPluginId(), true);
                        }
                    } else {
                        Updater.sendUpdateMessage(sender, version.getResult(), version.getSpigotPluginId());
                    }
                });
            } else {
                sender.sendMessage(HoloMobHealth.noPermissionMessage);
            }
            return true;
        }

        sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', Bukkit.spigot().getConfig().getString("messages.unknown-command")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tab = new LinkedList<>();
        if (!label.equalsIgnoreCase("holomobhealth") && !label.equalsIgnoreCase("hmh")) {
            return tab;
        }

        switch (args.length) {
            case 0:
                if (sender.hasPermission("holomobhealth.reload")) {
                    tab.add("reload");
                }
                if (sender.hasPermission("holomobhealth.toggle")) {
                    tab.add("toggle");
                }
                if (sender.hasPermission("holomobhealth.update")) {
                    tab.add("update");
                }
                return tab;
            case 1:
                if (sender.hasPermission("holomobhealth.reload")) {
                    if ("reload".startsWith(args[0].toLowerCase())) {
                        tab.add("reload");
                    }
                }
                if (sender.hasPermission("holomobhealth.toggle")) {
                    if ("toggle".startsWith(args[0].toLowerCase())) {
                        tab.add("toggle");
                    }
                }
                if (sender.hasPermission("holomobhealth.update")) {
                    if ("update".startsWith(args[0].toLowerCase())) {
                        tab.add("update");
                    }
                }
                return tab;
            case 2:
                if (sender.hasPermission("holomobhealth.toggle")) {
                    if (args[0].equalsIgnoreCase("toggle")) {
                        if (sender.hasPermission("holomobhealth.toggle.others")) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                    tab.add(player.getName());
                                }
                            }
                        }
                    }
                }
        }
        return tab;
    }

}
