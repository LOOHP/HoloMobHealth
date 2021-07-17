package com.loohp.holomobhealth.modules;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.protocol.EntityMetadata;
import com.loohp.holomobhealth.utils.ChatColorUtils;
import com.loohp.holomobhealth.utils.CitizensUtils;
import com.loohp.holomobhealth.utils.CustomNameUtils;
import com.loohp.holomobhealth.utils.EntityTypeUtils;
import com.loohp.holomobhealth.utils.LanguageUtils;
import com.loohp.holomobhealth.utils.MCVersion;
import com.loohp.holomobhealth.utils.MyPetUtils;
import com.loohp.holomobhealth.utils.MythicMobsUtils;
import com.loohp.holomobhealth.utils.NMSUtils;
import com.loohp.holomobhealth.utils.ParsePlaceholders;
import com.loohp.holomobhealth.utils.ShopkeepersUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class NameTagDisplay {
	
	//private static Map<UUID, WrappedDataWatcher> cache = new HashMap<>();

	public static void entityMetadataPacketListener() {
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_METADATA) {
			@Override
			public void onPacketSending(PacketEvent event) {
				try {
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
					WrappedDataWatcher watcher = getWatcher(player, entityUUID, world, packet);
					
					if (watcher != null) {
						boolean readOnly = event.isReadOnly();
						event.setReadOnly(true);
						packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
						event.setReadOnly(readOnly);
					}
				} catch (UnsupportedOperationException e) {}
			}
		});
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
			@Override
			public void onPacketSending(PacketEvent event) {
				try {
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
				} catch (UnsupportedOperationException e) {}
			}
		});
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SPAWN_ENTITY) {
			@Override
			public void onPacketSending(PacketEvent event) {
				try {
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
				} catch (UnsupportedOperationException e) {}
			}
		});
	}

	public static WrappedDataWatcher getWatcher(Player player, UUID entityUUID, World world, PacketContainer packet) {
		//WrappedDataWatcher cachedWatcher = cache.get(entityUUID);
		//if (cachedWatcher != null) {
		//	return cachedWatcher;
		//}
		
		Entity entity = NMSUtils.getEntityFromUUID(entityUUID);
		
		if (entity == null || !EntityTypeUtils.getMobsTypesSet().contains(entity.getType())) {
			return null;
		}
		
		if (HoloMobHealth.disabledMobTypes.contains(entity.getType())) {
			return null;
		}
		
		String customName = CustomNameUtils.getMobCustomName(entity);
		
		if (HoloMobHealth.rangeEnabled && !RangeModule.isEntityInRangeOfPlayer(player, entity)) {
			return null;
		}
		
		boolean useIdle = false;
		if (HoloMobHealth.useAlterHealth && !HoloMobHealth.altShowHealth.containsKey(entityUUID)) {
			if (!HoloMobHealth.idleUse) {
				return null;
			}
			useIdle = true;
		}

		if (!HoloMobHealth.disabledWorlds.contains(world.getName())) {
			
			if (!HoloMobHealth.showCitizens && HoloMobHealth.citizensHook) {
				if (CitizensUtils.isNPC(entity)) {
					return null;
				}
			}
			if (!HoloMobHealth.showMythicMobs && HoloMobHealth.mythicHook) {
				if (MythicMobsUtils.isMythicMob(entity)) {
					return null;
				}
			}
			if (!HoloMobHealth.showShopkeepers && HoloMobHealth.shopkeepersHook) {
				if (ShopkeepersUtils.isShopkeeper(entity)) {
					return null;
				}
			}
			if (!HoloMobHealth.showMyPet && HoloMobHealth.myPetHook) {
				if (MyPetUtils.isMyPet(entity)) {
					return null;
				}
			}
			
			if (customName != null) {
				for (String each : HoloMobHealth.disabledMobNamesAbsolute) {
					if (customName.equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
						return null;
					}
				}
				for (String each : HoloMobHealth.disabledMobNamesContains) {
					if (ChatColorUtils.stripColor(customName.toLowerCase()).contains(ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
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
			
			String json = ParsePlaceholders.parse(player, (LivingEntity) entity, useIdle ? HoloMobHealth.idleDisplayText.get(0) : HoloMobHealth.displayText.get(0));
			BaseComponent[] components = json == null ? null : ComponentSerializer.parse(json);
			boolean visible = HoloMobHealth.alwaysShow;

			if (json != null && components != null && components.length > 0) {
				if (HoloMobHealth.version.isOld()) {
					watcher.setObject(2, LanguageUtils.convert(components[0], HoloMobHealth.language).toLegacyText());
				} else if (HoloMobHealth.version.isLegacy()) {
					Serializer serializer = Registry.get(String.class);
					WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
					watcher.setObject(object, LanguageUtils.convert(components[0], HoloMobHealth.language).toLegacyText());
				} else {
					Optional<?> opt = Optional.of(WrappedChatComponent.fromJson(json).getHandle());
					watcher.setObject(new WrappedDataWatcherObject(2, Registry.getChatComponentSerializer(true)), opt);
				}
			} else {
				if (HoloMobHealth.version.isOld()) {
					watcher.setObject(2, "");
				} else if (HoloMobHealth.version.isLegacy()) {
					Serializer serializer = Registry.get(String.class);
					WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
					watcher.setObject(object, "");
				} else {
					Optional<?> opt = Optional.empty();
					watcher.setObject(new WrappedDataWatcherObject(2, Registry.getChatComponentSerializer(true)), opt);
				}
			}

			if (HoloMobHealth.version.isOld()) {
				watcher.setObject(3, (byte) (visible ? 1 : 0));
			} else {
				watcher.setObject(new WrappedDataWatcherObject(3, Registry.get(Boolean.class)), visible);
			}
			
			//cache.put(entityUUID, watcher);
			//Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> cache.remove(entityUUID), 1);
			
			return watcher;
		}
		return null;
	}

}
