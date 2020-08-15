package com.loohp.holomobhealth.Debug;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Utils.CustomNameUtils;

import net.md_5.bungee.api.ChatColor;

public class Debug implements Listener {
	
	@EventHandler
	public void onJoinPluginActive(PlayerJoinEvent event) {
		if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEskakE")) {
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "HoloMobHealth " + HoloMobHealth.plugin.getDescription().getVersion() + " is running!");
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			if (player.getName().equals("LOOHP") || player.getName().equals("AppLEskakE")) {
				Entity entity = event.getEntity();
				entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, entity.getHeight() / 2, 0.0), 1, 0.5, 0.5, 0.5, 2);
			}
		}
	}
	
	public Debug() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(HoloMobHealth.plugin, () -> {
			try {
				for (World world : Bukkit.getWorlds()) {
					Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> {
						for (Entity entity : world.getEntities()) {
							if (entity instanceof Player) {
								if (((Player) entity).getName().matches("(?i)mellifluous|euphoria|liarcar")) {
									entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, entity.getHeight() / 2, 0.0), 1, 0.5, 0.5, 0.5, 1);
								}			
							} else {
								String customName = CustomNameUtils.getMobCustomName(entity);
								
								if (customName != null && customName.matches("(?i)mellifluous|euphoria|liarcar")) {
									entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, entity.getHeight() / 2, 0.0), 1, 0.5, 0.5, 0.5, 1);
								}
							}
						}
					});
				}
			} catch (Exception ignore) {}
		}, 0, 15);
	}

}
