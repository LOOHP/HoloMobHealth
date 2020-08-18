package com.loohp.holomobhealth.Listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Database.Database;
import com.loohp.holomobhealth.Protocol.EntityMetadata;

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
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		Entity entity = event.getEntity();
		Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity), 5);
	}
	
	@EventHandler
	public void onAttack(EntityDamageEvent event) {	
		if (!HoloMobHealth.UseAlterHealth) {
			return;
		}
		
		if (HoloMobHealth.DisabledWorlds.contains(event.getEntity().getWorld().getName())) {
			return;
		}
		
		if (HoloMobHealth.AltOnlyPlayer) {
			return;
		}
		
		Entity entity = event.getEntity();
		UUID uuid = entity.getUniqueId();		
		HoloMobHealth.altShowHealth.put(uuid, System.currentTimeMillis() + HoloMobHealth.AltHealthDisplayTime * 1000);
		EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity);
		Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
			Long timeout = HoloMobHealth.altShowHealth.get(uuid);
			if (timeout != null && System.currentTimeMillis() > timeout) {
				HoloMobHealth.altShowHealth.remove(uuid);
				EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity);
			}
		}, HoloMobHealth.AltHealthDisplayTime * 20 + 5);
	}
	
	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if (!HoloMobHealth.UseAlterHealth) {
			return;
		}
		
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
		
		Entity entity = event.getEntity();
		UUID uuid = entity.getUniqueId();		
		HoloMobHealth.altShowHealth.put(uuid, System.currentTimeMillis() + HoloMobHealth.AltHealthDisplayTime * 1000);
		EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity);
		Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
			Long timeout = HoloMobHealth.altShowHealth.get(uuid);
			if (timeout != null && System.currentTimeMillis() > timeout) {
				HoloMobHealth.altShowHealth.remove(uuid);
				EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity);
			}
		}, HoloMobHealth.AltHealthDisplayTime * 20 + 5);
	}
}
