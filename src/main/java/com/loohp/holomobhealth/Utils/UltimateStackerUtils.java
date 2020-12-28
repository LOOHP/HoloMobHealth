package com.loohp.holomobhealth.Utils;

import java.util.Comparator;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.entity.EntityStackManager;

public class UltimateStackerUtils {
	
	private static EntityStackManager manager;
	
	static {
		manager = UltimateStacker.getInstance().getEntityStackManager();
	}
	
	public static boolean isStacked(UUID uuid) {
		return manager.getStack(uuid) != null;
	}
	
	public static UUID getHost(UUID uuid) {
		LivingEntity host = manager.getStack(uuid).getHostEntity();
		Comparator<Entity> c = Comparator.comparing(e -> e.getLocation().distanceSquared(host.getLocation()));
		return host.getNearbyEntities(3, 3, 3).stream().filter(e -> !e.equals(host)).min(c).orElse(host).getUniqueId();
	}

}
