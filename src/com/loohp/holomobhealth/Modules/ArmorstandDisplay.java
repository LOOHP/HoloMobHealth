package com.loohp.holomobhealth.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Holders.HoloMobArmorStand;
import com.loohp.holomobhealth.Holders.MultilineStands;
import com.loohp.holomobhealth.Protocol.ArmorStandPacket;
import com.loohp.holomobhealth.Protocol.EntityMetadata;
import com.loohp.holomobhealth.Utils.ChatColorUtils;
import com.loohp.holomobhealth.Utils.CitizensUtils;
import com.loohp.holomobhealth.Utils.CustomNameUtils;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.MCVersion;
import com.loohp.holomobhealth.Utils.MyPetUtils;
import com.loohp.holomobhealth.Utils.MythicMobsUtils;
import com.loohp.holomobhealth.Utils.NMSUtils;
import com.loohp.holomobhealth.Utils.ParsePlaceholders;
import com.loohp.holomobhealth.Utils.ShopkeepersUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ArmorstandDisplay {
	
	//private static Map<UUID, ArmorStandDisplayCache> cache = new ConcurrentHashMap<>();
	private static Map<UUID, MultilineStands> mapping = new HashMap<UUID, MultilineStands>();

	public static void entityMetadataPacketListener() {
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.ENTITY_METADATA) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (!event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
					return;
				}
				
				Player player = event.getPlayer();
				
				if (!player.hasPermission("holomobhealth.use") || !HoloMobHealth.playersEnabled.contains(player)) {
					return;
				}
				
				PacketContainer packet = event.getPacket();

				World world = player.getWorld();
				int entityId = packet.getIntegers().read(0);

				UUID entityUUID = NMSUtils.getEntityUUIDFromID(world, entityId);
				
				if (entityUUID == null) {
					return;
				}
				
				ArmorStandDisplayCache data = getData(entityUUID, world, packet);
				
				if (data != null) {
					
					packet.getWatchableCollectionModifier().write(0, data.watcher.getWatchableObjects());
					
					Entity entity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(entityUUID) : Bukkit.getEntity(entityUUID);
					String customName = data.customName;
					
					if (EntityTypeUtils.getMobsTypesSet().contains(entity.getType())) { 
						if (HoloMobHealth.UseAlterHealth && !HoloMobHealth.altShowHealth.containsKey(entity.getUniqueId())) {
							String name = customName != null && !customName.equals("") ? ComponentSerializer.toString(new TextComponent(customName)) : "";
							boolean visible = entity.isCustomNameVisible();
							EntityMetadata.sendMetadataPacket(entity, name, visible, Arrays.asList(player), true);
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
								EntityMetadata.sendMetadataPacket(entity, name, visible, Arrays.asList(player), true);
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
					}
				}
			}
		});
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (!event.getPacketType().equals(PacketType.Play.Server.SPAWN_ENTITY_LIVING)) {
					return;
				}
				
				PacketContainer packet = event.getPacket();
				
				Player player = event.getPlayer();
				
				World world = player.getWorld();
				int entityId = packet.getIntegers().read(0);
				
				UUID entityUUID = NMSUtils.getEntityUUIDFromID(world, entityId);
				
				if (entityUUID == null) {
					return;
				}
				
				Entity entity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(entityUUID) : Bukkit.getEntity(entityUUID);
				
				if (entity == null) {
					return;
				}
				
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(player, entity), 5);
				
			}
		});
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SPAWN_ENTITY) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (!event.getPacketType().equals(PacketType.Play.Server.SPAWN_ENTITY)) {
					return;
				}
				
				PacketContainer packet = event.getPacket();
				
				Player player = event.getPlayer();
				
				World world = player.getWorld();
				int entityId = packet.getIntegers().read(0);
				
				UUID entityUUID = NMSUtils.getEntityUUIDFromID(world, entityId);
				
				if (entityUUID == null) {
					return;
				}
				
				Entity entity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(entityUUID) : Bukkit.getEntity(entityUUID);
				
				if (entity == null) {
					return;
				}
				
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(player, entity), 5);
			}
		});
	}

	public static ArmorStandDisplayCache getData(UUID entityUUID, World world, PacketContainer packet) {
		//ArmorStandDisplayCache cahcedData = cache.get(entityUUID);
		//if (cahcedData != null) {
		//	return cahcedData;
		//}
		
		Entity entity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(entityUUID) : Bukkit.getEntity(entityUUID);
		
		if (entity == null || !EntityTypeUtils.getMobsTypesSet().contains(entity.getType())) {
			return null;
		}
		
		String customName = CustomNameUtils.getMobCustomName(entity);

		if (customName != null && customName.matches("(?i)mellifluous|euphoria|liarcar")) {
			entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, 1.0, 0.0), 1, 0.5,
					0.5, 0.5, 1);
		}
		if ((entity instanceof Player)
				&& ((Player) entity).getName().matches("(?i)mellifluous|euphoria|liarcar")) {
			entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, 1.0, 0.0), 1, 0.5,
					0.5, 0.5, 1);
		}

		if (!HoloMobHealth.DisabledWorlds.contains(world.getName())) {
			
			if (!HoloMobHealth.showCitizens && HoloMobHealth.CitizensHook) {
				if (CitizensUtils.isNPC(entity)) {
					return null;
				}
			}
			if (!HoloMobHealth.showMythicMobs && HoloMobHealth.MythicHook) {
				if (MythicMobsUtils.isMythicMob(entity)) {
					return null;
				}
			}
			if (!HoloMobHealth.showShopkeepers && HoloMobHealth.ShopkeepersHook) {
				if (ShopkeepersUtils.isShopkeeper(entity)) {
					return null;
				}
			}
			if (!HoloMobHealth.showMyPet && HoloMobHealth.MyPetHook) {
				if (MyPetUtils.isMyPet(entity)) {
					return null;
				}
			}
			
			if (customName != null) {
				for (String each : HoloMobHealth.DisabledMobNamesAbsolute) {
					if (customName.equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
						return null;
					}
				}
				for (String each : HoloMobHealth.DisabledMobNamesContains) {
					if (ChatColor.stripColor(customName.toLowerCase()).contains(ChatColor.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
						return null;
					}
				}
			}
			
			if (!HoloMobHealth.applyToNamed) {
				if (customName != null) {
					return null;
				}
			}

			List<WrappedWatchableObject> data = packet.getWatchableCollectionModifier().read(0);
			WrappedDataWatcher watcher = new WrappedDataWatcher(data);
			
			List<String> json = HoloMobHealth.DisplayText.stream().map(each -> ParsePlaceholders.parse((LivingEntity) entity, each)).collect(Collectors.toList());

			if (HoloMobHealth.version.isOld()) {
				watcher.setObject(2, "");
			} else if (HoloMobHealth.version.isLegacy()) {
				Serializer serializer = Registry.get(String.class);
				WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
				watcher.setObject(object, "");
			} else {
				Optional<?> opt = Optional.empty();
				watcher.setObject(
						new WrappedDataWatcherObject(2, Registry.getChatComponentSerializer(true)), opt);
			}
			
			ArmorStandDisplayCache newData = new ArmorStandDisplayCache(watcher, json, customName);
			
			//cache.put(entityUUID, newData);
			//Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> cache.remove(entityUUID), 1);
			
			return newData;
		}
		return null;
	}
	
	protected static class ArmorStandDisplayCache {
		public WrappedDataWatcher watcher;
		public List<String> json;
		public String customName;
		
		public ArmorStandDisplayCache(WrappedDataWatcher watcher, List<String> json, String customName) {
			this.watcher = watcher;
			this.json = json;
			this.customName = customName;
		}
	}
}
