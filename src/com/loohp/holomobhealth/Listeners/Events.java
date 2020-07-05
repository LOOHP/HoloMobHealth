package com.loohp.holomobhealth.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Database.Database;

public class Events implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> {
			if (!Database.playerExists(player)) {
				Database.createPlayer(player);
			}
			Database.loadPlayer(player);
		});
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		HoloMobHealth.playersEnabled.remove(player);
	}
	
	@EventHandler
	public void onAttack(EntityDamageEvent event) {	
		if (HoloMobHealth.DisabledWorlds.contains(event.getEntity().getWorld().getName())) {
			return;
		}
		
		HoloMobHealth.updateQueue.add(event.getEntity());
		
		if (HoloMobHealth.AltOnlyPlayer) {
			return;
		}
		
		HoloMobHealth.altShowHealth.put(event.getEntity(), (System.currentTimeMillis() + (HoloMobHealth.AltHealthDisplayTime * 1000)));
	}
	
	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if (HoloMobHealth.DisabledWorlds.contains(event.getEntity().getWorld().getName())) {
			return;
		}
		
		if (!HoloMobHealth.AltOnlyPlayer) {
			return;
		}
		
		if (!event.getDamager().getType().equals(EntityType.PLAYER)) {
			if (event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if (projectile.getShooter() == null) {
					return;
				} else {
					if (!(projectile.getShooter() instanceof Player)) {
						return;
					}
				}
			} else {
				return;
			}
		}
		
		HoloMobHealth.altShowHealth.put(event.getEntity(), (System.currentTimeMillis() + (HoloMobHealth.AltHealthDisplayTime * 1000)));
	}
}
