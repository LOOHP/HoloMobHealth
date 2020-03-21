package com.loohp.holomobhealth;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Events implements Listener {
	
	@EventHandler
	public void onAttack(EntityDamageEvent event) {
		if (HoloMobHealth.AltOnlyPlayer == true) {
			return;
		}
		
		HoloMobHealth.altShowHealth.put(event.getEntity(), (System.currentTimeMillis() + (HoloMobHealth.AltHealthDisplayTime * 1000)));
	}
	
	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent event) {
		if (HoloMobHealth.AltOnlyPlayer == false) {
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
