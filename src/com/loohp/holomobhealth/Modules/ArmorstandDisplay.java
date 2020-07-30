package com.loohp.holomobhealth.Modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Holders.HoloMobArmorStand;
import com.loohp.holomobhealth.Holders.MultilineStands;
import com.loohp.holomobhealth.Protocol.ArmorStandPacket;
import com.loohp.holomobhealth.Protocol.MetadataPacket;
import com.loohp.holomobhealth.Utils.ChatColorUtils;
import com.loohp.holomobhealth.Utils.CitizensUtils;
import com.loohp.holomobhealth.Utils.CustomNameUtils;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.MyPetUtils;
import com.loohp.holomobhealth.Utils.MythicMobsUtils;
import com.loohp.holomobhealth.Utils.ParsePlaceholders;
import com.loohp.holomobhealth.Utils.ShopkeepersUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ArmorstandDisplay {
	
	public static HashMap<UUID, MultilineStands> mapping = new HashMap<UUID, MultilineStands>();

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
			while (itr.hasNext() && counter.getAndIncrement() < HoloMobHealth.mobsPerTick) {
					
				Entity entity = itr.next();
				itr.remove();
				
				String customName = CustomNameUtils.getMobCustomName(entity);
					
				if (customName != null && customName.matches("(?i)mellifluous|euphoria|liarcar")) {
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
				if (!HoloMobHealth.showShopkeepers && HoloMobHealth.ShopkeepersHook) {
					if (ShopkeepersUtils.isShopkeeper(entity)) {
						return;
					}
				}
				if (!HoloMobHealth.showMyPet && HoloMobHealth.MyPetHook) {
					if (MyPetUtils.isMyPet(entity)) {
						return;
					}
				}
				if (customName != null) {
					boolean contain = false;
					for (String each : HoloMobHealth.DisabledMobNamesAbsolute) {
						if (customName.equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
							contain = true;
							break;
						}
					}
					for (String each : HoloMobHealth.DisabledMobNamesContains) {
						if (ChatColor.stripColor(customName.toLowerCase()).contains(ChatColor.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
							contain = true;
							break;
						}
					}
					if (contain) {
						return;
					}
				}
				if (EntityTypeUtils.getMobsTypesSet().contains(entity.getType())) { 
					if ((!HoloMobHealth.nearbyEntities.contains(entity)) || (!HoloMobHealth.altShowHealth.containsKey(entity))) {
						String name = customName != null && !customName.equals("") ? ComponentSerializer.toString(new TextComponent(customName)) : "";
						boolean visible = entity.isCustomNameVisible();
						MetadataPacket.sendMetadataPacket(entity, name, visible);
						MultilineStands multi = mapping.remove(entity.getUniqueId());
						if (multi == null) {
							return;
						}
						multi.getStands().forEach((each) -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
						multi.remove();
						return;
					}
					if (!HoloMobHealth.applyToNamed) {
						if (customName != null) {
							String name = customName != null && !customName.equals("") ? ComponentSerializer.toString(new TextComponent(customName)) : "";
							boolean visible = entity.isCustomNameVisible();
							MetadataPacket.sendMetadataPacket(entity, name, visible);
							MultilineStands multi = mapping.remove(entity.getUniqueId());
							if (multi == null) {
								return;
							}
							multi.getStands().forEach((each) -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
							multi.remove();
							return;
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
			}
		}, 0, 1).getTaskId();
	}
	
	public static void sendHealth() {
		
		HoloMobHealth.activeShowHealthTaskID = Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			Iterator<Entity> itr = HoloMobHealth.updateQueue.iterator();
			AtomicInteger counter = new AtomicInteger(0);
			while (itr.hasNext() && counter.getAndIncrement() < HoloMobHealth.mobsPerTick) {
					
				Entity entity = itr.next();
				itr.remove();
				
				String customName = CustomNameUtils.getMobCustomName(entity);

				if (customName != null && customName.matches("(?i)mellifluous|euphoria|liarcar")) {
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
				if (!HoloMobHealth.showShopkeepers && HoloMobHealth.ShopkeepersHook) {
					if (ShopkeepersUtils.isShopkeeper(entity)) {
						return;
					}
				}
				if (!HoloMobHealth.showMyPet && HoloMobHealth.MyPetHook) {
					if (MyPetUtils.isMyPet(entity)) {
						return;
					}
				}
				if (customName != null) {
					boolean contain = false;
					for (String each : HoloMobHealth.DisabledMobNamesAbsolute) {
						if (customName.equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
							contain = true;
							break;
						}
					}
					for (String each : HoloMobHealth.DisabledMobNamesContains) {
						if (ChatColor.stripColor(customName.toLowerCase()).contains(ChatColor.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
							contain = true;
							break;
						}
					}
					if (contain) {
						return;
					}
				}
				if (EntityTypeUtils.getMobsTypesSet().contains(entity.getType())) { 
					if (!HoloMobHealth.nearbyEntities.contains(entity)) {
						String name = customName != null && !customName.equals("") ? ComponentSerializer.toString(new TextComponent(customName)) : "";
						boolean visible = entity.isCustomNameVisible();
						MetadataPacket.sendMetadataPacket(entity, name, visible);
						MultilineStands multi = mapping.remove(entity.getUniqueId());
						if (multi == null) {
							return;
						}
						multi.getStands().forEach((each) -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
						multi.remove();
						return;
					}
					if (!HoloMobHealth.applyToNamed) {
						if (customName != null) {
							String name = customName != null && !customName.equals("") ? ComponentSerializer.toString(new TextComponent(customName)) : "";
							boolean visible = entity.isCustomNameVisible();
							MetadataPacket.sendMetadataPacket(entity, name, visible);
							MultilineStands multi = mapping.remove(entity.getUniqueId());
							if (multi == null) {
								return;
							}
							multi.getStands().forEach((each) -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
							multi.remove();
							return;
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
			}
		}, 0, 1).getTaskId();
	}
}
