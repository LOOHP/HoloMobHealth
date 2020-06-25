package com.loohp.holomobhealth.Modules;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Protocol.MetadataPacket;
import com.loohp.holomobhealth.Utils.CitizensUtils;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.MythicMobsUtils;
import com.loohp.holomobhealth.Utils.ParsePlaceholders;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class NameTagDisplay {

	public static void sendAltHealth() {
		
		HoloMobHealth.activeShowHealthTaskID = Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			Iterator<Entry<Entity, Long>> itr0 = HoloMobHealth.altShowHealth.entrySet().iterator();
			while (itr0.hasNext()) {
				Entry<Entity, Long> entry = itr0.next();
				long unix = System.currentTimeMillis();
				if (entry.getValue() < unix) {
					itr0.remove();
				}
			}
			
			Iterator<Entity> itr = HoloMobHealth.updateQueue.iterator();
			AtomicInteger counter = new AtomicInteger(0);
			while (itr.hasNext() && counter.getAndIncrement() < 2) {
					
				Entity entity = itr.next();
				itr.remove();
					
				if (entity.getCustomName() != null && entity.getCustomName().matches("(?i)mellifluous|euphoria|liarcar")) {
					entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, 1.0, 0.0), 1, 0.5, 0.5, 0.5, 1);
				}
				if ((entity instanceof Player) && ((Player) entity).getName().matches("(?i)mellifluous|euphoria|liarcar")) {
					entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, 1.0, 0.0), 1, 0.5, 0.5, 0.5, 1);
				}
				
				if (HoloMobHealth.DisabledMobTypes.contains(entity.getType())) {
					return;
				}
				if (!HoloMobHealth.showCitizens && HoloMobHealth.CitizensHook) {
					if (CitizensUtils.isNPC(entity)) {
						return;
					}
				}
				if (!HoloMobHealth.showMythicMobs && HoloMobHealth.MythicHook) {
					if (MythicMobsUtils.isMythicMob(entity)) {
						return;
					}
				}
				if (entity.getCustomName() != null) {
					if (!entity.getCustomName().equals("")) {
						boolean contain = false;
						for (String each : HoloMobHealth.DisabledMobNamesAbsolute) {
							if (entity.getCustomName().equals(ChatColor.translateAlternateColorCodes('&', each))) {
								contain = true;
								break;
							}
						}
						for (String each : HoloMobHealth.DisabledMobNamesContains) {
							if (ChatColor.stripColor(entity.getCustomName().toLowerCase()).contains(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', each).toLowerCase()))) {
								contain = true;
								break;
							}
						}
						if (contain) {
							return;
						}
					}
				}
				if (EntityTypeUtils.getMobList().contains(entity.getType())) { 
					if ((!HoloMobHealth.nearbyEntities.contains(entity)) || (!HoloMobHealth.altShowHealth.containsKey(entity))) {
						String name = entity.getCustomName() != null && !entity.getCustomName().equals("") ? ComponentSerializer.toString(new TextComponent(entity.getCustomName())) : "";
						boolean visible = entity.isCustomNameVisible();
						MetadataPacket.sendMetadataPacket(entity, name, visible);
						return;
					}
					if (!HoloMobHealth.applyToNamed) {
						if (entity.getCustomName() != null) {
							if (!entity.getCustomName().equals("")) {
								String name = entity.getCustomName() != null && !entity.getCustomName().equals("") ? ComponentSerializer.toString(new TextComponent(entity.getCustomName())) : "";
								boolean visible = entity.isCustomNameVisible();
								MetadataPacket.sendMetadataPacket(entity, name, visible);
								return;
							}
						}	
					}
					String display = ParsePlaceholders.parse((LivingEntity) entity, HoloMobHealth.DisplayText.get(0));
					MetadataPacket.sendMetadataPacket(entity, display, HoloMobHealth.alwaysShow);
				}
			}
		}, 0, 1).getTaskId();
	}
	
	public static void sendHealth() {
		
		HoloMobHealth.activeShowHealthTaskID = Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			
			Iterator<Entity> itr = HoloMobHealth.updateQueue.iterator();
			AtomicInteger counter = new AtomicInteger(0);
			while (itr.hasNext() && counter.getAndIncrement() < 2) {
					
				Entity entity = itr.next();
				itr.remove();
				
				if (entity.getCustomName() != null && entity.getCustomName().matches("(?i)mellifluous|euphoria|liarcar")) {
					entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, 1.0, 0.0), 1, 0.5, 0.5, 0.5, 1);
				}
				if ((entity instanceof Player) && ((Player) entity).getName().matches("(?i)mellifluous|euphoria|liarcar")) {
					entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, 1.0, 0.0), 1, 0.5, 0.5, 0.5, 1);
				}
	
				if (HoloMobHealth.DisabledMobTypes.contains(entity.getType())) {
					return;
				}
				if (!HoloMobHealth.showCitizens && HoloMobHealth.CitizensHook) {
					if (CitizensUtils.isNPC(entity)) {
						return;
					}
				}
				if (!HoloMobHealth.showMythicMobs && HoloMobHealth.MythicHook) {
					if (MythicMobsUtils.isMythicMob(entity)) {
						return;
					}
				}
				if (entity.getCustomName() != null) {
					if (!entity.getCustomName().equals("")) {
						boolean contain = false;
						for (String each : HoloMobHealth.DisabledMobNamesAbsolute) {
							if (entity.getCustomName().equals(ChatColor.translateAlternateColorCodes('&', each))) {
								contain = true;
								break;
							}
						}
						for (String each : HoloMobHealth.DisabledMobNamesContains) {
							if (ChatColor.stripColor(entity.getCustomName().toLowerCase()).contains(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', each).toLowerCase()))) {
								contain = true;
								break;
							}
						}
						if (contain) {
							return;
						}
					}
				}
				if (EntityTypeUtils.getMobList().contains(entity.getType())) { 
					if (!HoloMobHealth.nearbyEntities.contains(entity)) {
						String name = entity.getCustomName() != null && !entity.getCustomName().equals("") ? ComponentSerializer.toString(new TextComponent(entity.getCustomName())) : "";
						boolean visible = entity.isCustomNameVisible();
						MetadataPacket.sendMetadataPacket(entity, name, visible);
						return;
					}
					if (!HoloMobHealth.applyToNamed) {
						if (entity.getCustomName() != null) {
							if (!entity.getCustomName().equals("")) {
								String name = entity.getCustomName() != null && !entity.getCustomName().equals("") ? ComponentSerializer.toString(new TextComponent(entity.getCustomName())) : "";
								boolean visible = entity.isCustomNameVisible();
								MetadataPacket.sendMetadataPacket(entity, name, visible);
								return;
							}
						}	
					}
					String display = ParsePlaceholders.parse((LivingEntity) entity, HoloMobHealth.DisplayText.get(0));
					MetadataPacket.sendMetadataPacket(entity, display, HoloMobHealth.alwaysShow);
				}
			}
		}, 0, 1).getTaskId();
	}
}
