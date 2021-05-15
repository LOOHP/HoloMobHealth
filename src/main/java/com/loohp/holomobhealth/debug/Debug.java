package com.loohp.holomobhealth.debug;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.loohp.holomobhealth.HoloMobHealth;

import net.md_5.bungee.api.ChatColor;

public class Debug implements Listener {
	
	private Player player;
	
	@EventHandler
	public void onJoinPluginActive(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.getName().equals("LOOHP") || player.getName().equals("AppLEshakE")) {
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "HoloMobHealth " + HoloMobHealth.plugin.getDescription().getVersion() + " is running!");
		} else if (player.getName().equals("LIARCAR")) {
			this.player = player;
		}
	}
	
	@EventHandler
	public void onLeavePluginActive(PlayerQuitEvent event) {
		if (event.getPlayer().equals(player)) {
			player = null;
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		if (HoloMobHealth.version.isOld()) {
			return;
		}
		if (event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			if (player.getName().equals("LOOHP") || player.getName().equals("AppLEskakE") || player.getName().equals("NARLIAR") || player.getName().equals("LIARCAR")) {
				Entity entity = event.getEntity();
				entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, entity.getHeight() / 2, 0.0), 1, 0.5, 0.5, 0.5, 2);
			}
		}
	}
	
	public Debug() {
		if (HoloMobHealth.version.isOld()) {
			return;
		}
		Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			if (player != null) {
				player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0.0, player.getHeight() / 2, 0.0), 1, 0.5, 0.5, 0.5, 1);
			}
		}, 0, 15);
	}

}
