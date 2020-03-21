package com.loohp.holomobhealth;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import com.loohp.holomobhealth.Utils.EntityTypeUtils;

import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!label.equals("holomobhealth") && !label.equals("hmh")) {
			return true;
		}
		if (args.length < 1) {
			sender.sendMessage(ChatColor.AQUA + "HoloMobHealth written by LOOHP!");
			sender.sendMessage(ChatColor.GOLD + "You are running HoloMobHealth version: " + HoloMobHealth.plugin.getDescription().getVersion());
			return true;
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("holomobhealth.reload")) {
				HoloMobHealth.plugin.reloadConfig();
				HoloMobHealth.loadConfig();
				EntityTypeUtils.reloadLang();
				sender.sendMessage(HoloMobHealth.ReloadPlugin);
				for (World world : Bukkit.getWorlds()) {
					for (Entity entity : world.getEntities()) {
						String name = "";
						if (entity.getCustomName() != null) {							
							if (!entity.getCustomName().equals("")) {
								name = entity.getCustomName();
							}
						}	
						boolean visible = entity.isCustomNameVisible();
						entity.setCustomName(ChatColor.RED + "" + ChatColor.RED + "" + ChatColor.RED + "" + ChatColor.RED + "");
						entity.setCustomName(name);
						entity.setCustomNameVisible(!visible);
						entity.setCustomNameVisible(visible);
					}
				}
			} else {
				sender.sendMessage(HoloMobHealth.NoPermission);
			}
			return true;
		}
		sender.sendMessage(Bukkit.spigot().getConfig().getString("messages.unknown-command"));
		return true;
	}

}
