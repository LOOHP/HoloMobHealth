package com.loohp.holomobhealth.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityMountEvent;

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
import com.loohp.holomobhealth.Utils.NBTUtils;
import com.loohp.holomobhealth.Utils.NMSUtils;
import com.loohp.holomobhealth.Utils.ParsePlaceholders;
import com.loohp.holomobhealth.Utils.RayTrace;
import com.loohp.holomobhealth.Utils.ShopkeepersUtils;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ArmorstandDisplay implements Listener {
	
	private static final UUID EMPTY_UUID = new UUID(0, 0);
	
	private static Map<UUID, MultilineStands> mapping = new HashMap<>();
	private static Map<Player, UUID> focusingEntities = new HashMap<>();
	
	private static Set<Entity> requiresTicking = new HashSet<>();
	
	public static void run() {
		Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			if (HoloMobHealth.alwaysShow || !HoloMobHealth.armorStandMode) {
				return;
			}
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			int perTick = (int) Math.ceil((double) players.size() / 5);
			int current = 0;
			int count = 0;
			for (Player each : players) {
				UUID playerUUID = each.getUniqueId();
				if (count >= perTick) {
					count = 0;
					current++;
				}
				count++;
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
					Player player = Bukkit.getPlayer(playerUUID);
					if (player == null) {
						return;
					}
					if (HoloMobHealth.disabledWorlds.contains(player.getWorld().getName())) {
						return;
					}
					Entity entity = RayTrace.getLookingEntity(player, 6);
					UUID last = focusingEntities.get(player);
					if (entity != null) {
						UUID now = entity.getUniqueId();
						if (!now.equals(last)) {
							focusingEntities.put(player, now);
							EntityMetadata.updateEntity(player, entity);
						}
					} else {
						if (last != null) {
							focusingEntities.remove(player);
							Entity lastEntity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(last) : Bukkit.getEntity(last);
							if (lastEntity != null) {
								EntityMetadata.updateEntity(player, lastEntity);
							}
						}
					}
				}, current);
			}
		}, 0, 5);
		
		Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			Iterator<Entity> itr = requiresTicking.iterator();
			while (itr.hasNext()) {
				Entity entity = itr.next();
				if (entity.getVehicle() == null) {
					itr.remove();
				}
				MultilineStands multi = mapping.get(entity.getUniqueId());
				if (multi == null) {
					continue;
				}
				multi.setLocation(entity.getLocation());
				multi.getStands().forEach(each -> ArmorStandPacket.updateArmorStandLocation(entity, each));
			}
		}, 0, 1);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onMount(EntityMountEvent event) {
		if (event.isCancelled()) {
			return;
		}
		requiresTicking.add(event.getEntity());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChangeDimension(EntityPortalEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Entity entity = event.getEntity();
		MultilineStands multi = mapping.remove(entity.getUniqueId());
		if (multi == null) {
			return;
		}
		multi.getStands().forEach(each -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
		multi.remove();
		
		Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
			EntityMetadata.updateEntity(HoloMobHealth.playersEnabled, entity);
		}, 2);
	}

	public static void entityMetadataPacketListener() {
		Bukkit.getPluginManager().registerEvents(new ArmorstandDisplay(), HoloMobHealth.plugin);
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.ENTITY_METADATA) {
			@SuppressWarnings("deprecation")
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

					ArmorStandDisplayData data = getData(player, entityUUID, world, packet);
					
					if (data != null) {
						if (data.use()) {
							packet.getWatchableCollectionModifier().write(0, data.getWatcher().getWatchableObjects());
							
							Entity entity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(entityUUID) : Bukkit.getEntity(entityUUID);
							String customName = data.getCustomName();
							
							if (EntityTypeUtils.getMobsTypesSet().contains(entity.getType())) { 
								if (entity.getPassenger() != null || isInvisible(entity) || (!HoloMobHealth.applyToNamed && customName != null) || (HoloMobHealth.useAlterHealth && !HoloMobHealth.idleUse && !HoloMobHealth.altShowHealth.containsKey(entity.getUniqueId())) || (HoloMobHealth.rangeEnabled && !RangeModule.isEntityInRangeOfPlayer(player, entity))) {
									String name = customName != null && !customName.equals("") ? ComponentSerializer.toString(new TextComponent(customName)) : "";
									boolean visible = entity.isCustomNameVisible();
									EntityMetadata.sendMetadataPacket(entity, name, visible, Arrays.asList(player), true);
									MultilineStands multi = mapping.remove(entity.getUniqueId());
									if (multi == null) {
										return;
									}
									multi.getStands().forEach(each -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
									multi.remove();
									return;
								}
								MultilineStands multi = mapping.get(entity.getUniqueId());
								if (multi == null) {
									multi = new MultilineStands(entity);
									mapping.put(entity.getUniqueId(), multi);
									List<HoloMobArmorStand> stands = new ArrayList<>(multi.getStands());
									Collections.reverse(stands);
									for (HoloMobArmorStand stand : stands) {
										ArmorStandPacket.sendArmorStandSpawn(HoloMobHealth.playersEnabled, stand, "", HoloMobHealth.alwaysShow);
									}
								}
								UUID focusing = focusingEntities.getOrDefault(player, EMPTY_UUID);
								multi.setLocation(entity.getLocation());
								for (int i = 0; i < data.getJson().size(); i++) {
									String display = data.getJson().get(i);
									ArmorStandPacket.updateArmorStand(entity, multi.getStand(i), display, HoloMobHealth.alwaysShow || focusing.equals(entityUUID));
								}
							}
						} else {
							Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
								MultilineStands multi = mapping.remove(entityUUID);
								if (multi == null) {
									return;
								}
								Entity entity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(entityUUID) : Bukkit.getEntity(entityUUID);
								String name = NBTUtils.getString(entity, "CustomName");
								boolean visible = entity.isCustomNameVisible();
								EntityMetadata.sendMetadataPacket(entity, name, visible, Arrays.asList(player), true);
								multi.getStands().forEach(each -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
								multi.remove();
								EntityMetadata.sendMetadataPacket(entity, name, entity.isCustomNameVisible(), entity.getWorld().getPlayers(), true);
							}, 1);
						}
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
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.ENTITY_TELEPORT) {
			@Override
			public void onPacketSending(PacketEvent event) {
				try {
					if (!event.getPacketType().equals(PacketType.Play.Server.ENTITY_TELEPORT)) {
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
					MultilineStands multi = mapping.get(entityUUID);
					if (multi == null) {
						return;
					}
					multi.setLocation(entity.getLocation());
					multi.getStands().forEach(each -> ArmorStandPacket.updateArmorStandLocation(entity, each));
				} catch (UnsupportedOperationException e) {}
			}
		});
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.REL_ENTITY_MOVE) {
			@Override
			public void onPacketSending(PacketEvent event) {
				try {
					if (!event.getPacketType().equals(PacketType.Play.Server.REL_ENTITY_MOVE)) {
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
					MultilineStands multi = mapping.get(entityUUID);
					if (multi == null) {
						return;
					}
					multi.setLocation(entity.getLocation());
					multi.getStands().forEach(each -> ArmorStandPacket.updateArmorStandLocation(entity, each));
				} catch (UnsupportedOperationException e) {}
			}
		});
		HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
			@Override
			public void onPacketSending(PacketEvent event) {
				try {
					if (!event.getPacketType().equals(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK)) {
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
					MultilineStands multi = mapping.get(entityUUID);
					if (multi == null) {
						return;
					}
					multi.setLocation(entity.getLocation());
					multi.getStands().forEach(each -> ArmorStandPacket.updateArmorStandLocation(entity, each));
				} catch (UnsupportedOperationException e) {}
			}
		});
	}

	public static ArmorStandDisplayData getData(Player player, UUID entityUUID, World world, PacketContainer packet) {
		//ArmorStandDisplayCache cahcedData = cache.get(entityUUID);
		//if (cahcedData != null) {
		//	return cahcedData;
		//}
		
		Entity entity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(entityUUID) : Bukkit.getEntity(entityUUID);
		
		if (entity == null || !EntityTypeUtils.getMobsTypesSet().contains(entity.getType())) {
			return null;
		}
		
		if (HoloMobHealth.disabledMobTypes.contains(entity.getType())) {
			return new ArmorStandDisplayData();
		}
		
		String customName = CustomNameUtils.getMobCustomName(entity);

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
						return new ArmorStandDisplayData();
					}
				}
				for (String each : HoloMobHealth.disabledMobNamesContains) {
					if (ChatColorUtils.stripColor(customName.toLowerCase()).contains(ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
						return new ArmorStandDisplayData();
					}
				}
			}
			
			if (!HoloMobHealth.applyToNamed) {
				if (customName != null) {
					return null;
				}
			}
			
			boolean useIdle = false;
			if (HoloMobHealth.useAlterHealth && !HoloMobHealth.altShowHealth.containsKey(entityUUID)) {
				if (HoloMobHealth.idleUse) {
					useIdle = true;
				}
			}

			List<WrappedWatchableObject> data = packet.getWatchableCollectionModifier().read(0);
			WrappedDataWatcher watcher = new WrappedDataWatcher(data);
			
			List<String> json;
			if (useIdle) {
				json = HoloMobHealth.idleDisplayText.stream().map(each -> ParsePlaceholders.parse(player, (LivingEntity) entity, each)).collect(Collectors.toList());
			} else {
				json = HoloMobHealth.displayText.stream().map(each -> ParsePlaceholders.parse(player, (LivingEntity) entity, each)).collect(Collectors.toList());
			}

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
			
			ArmorStandDisplayData newData = new ArmorStandDisplayData(watcher, json, customName);
			
			//cache.put(entityUUID, newData);
			//Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> cache.remove(entityUUID), 1);
			
			return newData;
		}
		return null;
	}
	
	public static boolean isInvisible(Entity entity) {
		if (entity instanceof LivingEntity) {
			return ((LivingEntity) entity).getPotionEffect(PotionEffectType.INVISIBILITY) != null;
		}
		return false;
	}
	
	private static class ArmorStandDisplayData {
		
		private WrappedDataWatcher watcher;
		private List<String> json;
		private String customName;
		private boolean use;
		
		public ArmorStandDisplayData(WrappedDataWatcher watcher, List<String> json, String customName) {
			this.watcher = watcher;
			this.json = json;
			this.customName = customName;
			this.use = true;
		}
		
		public ArmorStandDisplayData() {
			this.use = false;
		}

		public WrappedDataWatcher getWatcher() {
			return watcher;
		}

		public List<String> getJson() {
			return json;
		}

		public String getCustomName() {
			return customName;
		}

		public boolean use() {
			return use;
		}
	}
}
