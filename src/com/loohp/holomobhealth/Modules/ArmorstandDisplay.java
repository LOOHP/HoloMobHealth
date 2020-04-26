package com.loohp.holomobhealth.Modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Holders.HoloMobArmorStand;
import com.loohp.holomobhealth.Holders.MultilineStands;
import com.loohp.holomobhealth.Protocol.ArmorStandPacket;
import com.loohp.holomobhealth.Protocol.MetadataPacket;
import com.loohp.holomobhealth.Utils.CitizensUtils;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.MythicMobsUtils;
import com.loohp.holomobhealth.Utils.ParsePlaceholders;

import net.md_5.bungee.api.ChatColor;

public class ArmorstandDisplay {
	
	public static HashMap<UUID, MultilineStands> mapping = new HashMap<UUID, MultilineStands>();

	public static void sendAltHealth() {
		
		HoloMobHealth.activeShowHealthTaskID = Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			Iterator<Entry<Entity, Long>> itr = HoloMobHealth.altShowHealth.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<Entity, Long> entry = itr.next();
				long unix = System.currentTimeMillis();
				if (entry.getValue() < unix) {
					itr.remove();
				}
			}
			
			int delay = 1;
			int count = 0;
			int maxper = (int) Math.ceil((double) HoloMobHealth.nearbyPlus10Entities.size() / (double) 3);
			Set<Entity> inRange = HoloMobHealth.nearbyEntities;
			for (Entity entity : HoloMobHealth.nearbyPlus10Entities) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
					if (!entity.isValid()) {
						return;
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
						if ((!inRange.contains(entity)) || (!HoloMobHealth.altShowHealth.containsKey(entity))) {
							String name = entity.getCustomName();							
							boolean visible = entity.isCustomNameVisible();
							MetadataPacket.sendMetadataPacket(entity, name, visible);
							MultilineStands multi = mapping.remove(entity.getUniqueId());
							if (multi == null) {
								return;
							}
							multi.getStands().forEach((each) -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true));
							multi.remove();
							return;
						}
						if (!HoloMobHealth.applyToNamed) {
							if (entity.getCustomName() != null) {
								if (!entity.getCustomName().equals("")) {
									String name = entity.getCustomName();							
									boolean visible = entity.isCustomNameVisible();
									MetadataPacket.sendMetadataPacket(entity, name, visible);
									MultilineStands multi = mapping.remove(entity.getUniqueId());
									if (multi == null) {
										return;
									}
									multi.getStands().forEach((each) -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true));
									multi.remove();
									return;
								}
							}	
						}
						MultilineStands multi = mapping.get(entity.getUniqueId());
						if (multi == null) {
							multi = new MultilineStands(entity);
							mapping.put(entity.getUniqueId(), multi);
							List<HoloMobArmorStand> stands = new ArrayList<HoloMobArmorStand>(multi.getAllRelatedEntities());
							Collections.reverse(stands);
							for (HoloMobArmorStand stand : stands) {
								ArmorStandPacket.sendArmorStandSpawn(HoloMobHealth.playersEnabled, stand, "", HoloMobHealth.alwaysShow);
							}
						}
						for (int i = 0; i < HoloMobHealth.DisplayText.size(); i++) {
							String display = ParsePlaceholders.parse((LivingEntity) entity, HoloMobHealth.DisplayText.get(i));
							ArmorStandPacket.updateArmorStand(HoloMobHealth.playersEnabled, multi.getStand(i), display, HoloMobHealth.alwaysShow);
						}
						MetadataPacket.sendMetadataPacket(entity, "", false);
					}
				}, delay);
			}
		}, 0, 4).getTaskId();
	}
	
	public static void sendHealth() {
		
		HoloMobHealth.activeShowHealthTaskID = Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			int delay = 1;
			int count = 0;
			int maxper = (int) Math.ceil((double) HoloMobHealth.nearbyPlus10Entities.size() / (double) 3);
			Set<Entity> inRange = HoloMobHealth.nearbyEntities;
			for (Entity entity : HoloMobHealth.nearbyPlus10Entities) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
					if (!entity.isValid()) {
						return;
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
						if (!inRange.contains(entity)) {
							String name = entity.getCustomName();							
							boolean visible = entity.isCustomNameVisible();
							MetadataPacket.sendMetadataPacket(entity, name, visible);
							MultilineStands multi = mapping.remove(entity.getUniqueId());
							if (multi == null) {
								return;
							}
							multi.getStands().forEach((each) -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true));
							multi.remove();
							return;
						}
						if (!HoloMobHealth.applyToNamed) {
							if (entity.getCustomName() != null) {
								if (!entity.getCustomName().equals("")) {
									String name = entity.getCustomName();
									boolean visible = entity.isCustomNameVisible();
									MetadataPacket.sendMetadataPacket(entity, name, visible);
									MultilineStands multi = mapping.remove(entity.getUniqueId());
									if (multi == null) {
										return;
									}
									multi.getStands().forEach((each) -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true));
									multi.remove();
									return;
								}
							}	
						}
						MultilineStands multi = mapping.get(entity.getUniqueId());
						if (multi == null) {
							multi = new MultilineStands(entity);
							mapping.put(entity.getUniqueId(), multi);
							List<HoloMobArmorStand> stands = new ArrayList<HoloMobArmorStand>(multi.getAllRelatedEntities());
							Collections.reverse(stands);
							for (HoloMobArmorStand stand : stands) {
								ArmorStandPacket.sendArmorStandSpawn(HoloMobHealth.playersEnabled, stand, "", HoloMobHealth.alwaysShow);
							}
						}
						for (int i = 0; i < HoloMobHealth.DisplayText.size(); i++) {
							String display = ParsePlaceholders.parse((LivingEntity) entity, HoloMobHealth.DisplayText.get(i));
							ArmorStandPacket.updateArmorStand(HoloMobHealth.playersEnabled, multi.getStand(i), display, HoloMobHealth.alwaysShow);
						}
						MetadataPacket.sendMetadataPacket(entity, "", false);
					}
				}, delay);
			}
		}, 0, 4).getTaskId();
	}
}
