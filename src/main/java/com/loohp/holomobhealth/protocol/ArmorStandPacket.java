package com.loohp.holomobhealth.protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.holders.HoloMobArmorStand;
import com.loohp.holomobhealth.utils.LanguageUtils;
import com.loohp.holomobhealth.utils.MCVersion;

import net.md_5.bungee.chat.ComponentSerializer;

public class ArmorStandPacket implements Listener {

	private static ProtocolManager protocolManager = HoloMobHealth.protocolManager;
	private static Plugin plugin = HoloMobHealth.plugin;
	public static Set<HoloMobArmorStand> active = Collections.synchronizedSet(new LinkedHashSet<>());
	public static Map<Player, Set<HoloMobArmorStand>> playerStatus = new ConcurrentHashMap<>();
	
	public static void update() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				try {
					Set<HoloMobArmorStand> activeList = playerStatus.get(player);
					if (activeList == null) {
						continue;
					}
					
					List<Player> playerList = new LinkedList<Player>();
					playerList.add(player);
					
					for (HoloMobArmorStand entity : activeList) {
						World world = player.getWorld();
						if (!entity.getWorld().equals(world) || entity.getLocation().distanceSquared(player.getLocation()) > ((HoloMobHealth.getUpdateRange(world) + 1) * (HoloMobHealth.getUpdateRange(world) + 1))) {						
							removeArmorStand(playerList, entity, false, true);
						}
					}
					
					for (HoloMobArmorStand entity : active) {
						World world = player.getWorld();
						if (entity.getWorld().equals(player.getWorld()) && entity.getLocation().distanceSquared(player.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world))) {
							if (activeList.contains(entity)) {
								continue;
							}
							if (!HoloMobHealth.playersEnabled.contains(player)) {
								continue;
							}
							
							sendArmorStandSpawn(playerList, entity, "", false);
							updateArmorStand(playerList, entity, "", false);
						}
					}
				} catch (ConcurrentModificationException ignore) {}
			}
		}, 0, 20);
	}
	
	public static void sendArmorStandSpawn(Collection<? extends Player> players, HoloMobArmorStand entity, String json, boolean visible) {
		if (players.isEmpty()) {
			return;
		}
		if (!active.contains(entity)) {
			active.add(entity);
		}
		
		World world = entity.getWorld();
		List<Player> playersInRange = new ArrayList<>();
		for (Player each : players) {
			if ((each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))) {
				Set<HoloMobArmorStand> list = playerStatus.get(each);
				if (list != null) {
					list.add(entity);
					playersInRange.add(each);
				}
			}
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
		packet1.getIntegers().write(0, entity.getEntityId());
		switch (HoloMobHealth.version) {
		case V1_16_4:
		case V1_16_2:
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 66);
			break;
		case V1_16:
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 65);
			break;
		case V1_15:
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 60);
			break;
		case V1_14:
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 59);
			break;
		case V1_13_1:
		case V1_13:
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 56);
			break;
		default:
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 30 : 101);
			break;
		}
		packet1.getIntegers().write(2, 0);
		packet1.getIntegers().write(3, 0);
		packet1.getIntegers().write(4, 0);		
		packet1.getDoubles().write(0, entity.getLocation().getX());
		packet1.getDoubles().write(1, entity.getLocation().getY());
		packet1.getDoubles().write(2, entity.getLocation().getZ());
		packet1.getBytes().write(0, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
		packet1.getBytes().write(1, (byte)(int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
		packet1.getBytes().write(2, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
		
		PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet2.getIntegers().write(0, entity.getEntityId());	
        WrappedDataWatcher wpw = buildWarppedDataWatcher(entity, json, visible);
        packet2.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        
    	try {
        	for (Player player : playersInRange) {
				protocolManager.sendServerPacket(player, packet1);
				protocolManager.sendServerPacket(player, packet2);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateArmorStand(Collection<? extends Player> players, HoloMobArmorStand entity, String json, boolean visible) {
		if (players.isEmpty()) {
			return;
		}
			
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			World world = entity.getWorld();
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());
			PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
			packet1.getIntegers().write(0, entity.getEntityId());	
	        WrappedDataWatcher wpw = buildWarppedDataWatcher(entity, json, visible);
	        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
	        
	        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
	        packet2.getIntegers().write(0, entity.getEntityId());
	        packet2.getDoubles().write(0, entity.getLocation().getX());
	        packet2.getDoubles().write(1, entity.getLocation().getY());
	        packet2.getDoubles().write(2, entity.getLocation().getZ());
	        packet2.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
	        packet2.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
	        
	        Bukkit.getScheduler().runTask(plugin, () -> {		
		        try {
		        	for (Player player : playersInRange) {
						protocolManager.sendServerPacket(player, packet1);
						protocolManager.sendServerPacket(player, packet2);
					}
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			});
		});
	}
	
	public static void updateArmorStand(Entity host, HoloMobArmorStand entity, String json, boolean visible) {
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet1.getIntegers().write(0, entity.getEntityId());	
	    WrappedDataWatcher wpw = buildWarppedDataWatcher(entity, json, visible);
	    packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
	    
	    PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
	    packet2.getIntegers().write(0, entity.getEntityId());
	    packet2.getDoubles().write(0, entity.getLocation().getX());
	    packet2.getDoubles().write(1, entity.getLocation().getY());
	    packet2.getDoubles().write(2, entity.getLocation().getZ());
	    packet2.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
	    packet2.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
	    
	    protocolManager.broadcastServerPacket(packet1, host, false);
	    protocolManager.broadcastServerPacket(packet2, host, false);
	}
	
	public static void updateArmorStandLocation(Collection<? extends Player> players, HoloMobArmorStand entity) {
		if (players.isEmpty()) {
			return;
		}
			
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			World world = entity.getWorld();
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());	        
	        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
	        packet1.getIntegers().write(0, entity.getEntityId());
	        packet1.getDoubles().write(0, entity.getLocation().getX());
	        packet1.getDoubles().write(1, entity.getLocation().getY());
	        packet1.getDoubles().write(2, entity.getLocation().getZ());
	        packet1.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
	        packet1.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
	        
	        Bukkit.getScheduler().runTask(plugin, () -> {		
		        try {
		        	for (Player player : playersInRange) {
						protocolManager.sendServerPacket(player, packet1);
					}
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			});
		});
	}
	
	public static void updateArmorStandLocation(Entity host, HoloMobArmorStand entity) {  
        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet1.getIntegers().write(0, entity.getEntityId());
        packet1.getDoubles().write(0, entity.getLocation().getX());
        packet1.getDoubles().write(1, entity.getLocation().getY());
        packet1.getDoubles().write(2, entity.getLocation().getZ());
        packet1.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
        packet1.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
        
        protocolManager.broadcastServerPacket(packet1, host, false);
	}
	
	public static void removeArmorStand(Collection<? extends Player> players, HoloMobArmorStand entity, boolean removeFromActive, boolean bypassFilter) {
		if (players.isEmpty()) {
			return;
		}
		if (removeFromActive) {
			active.remove(entity);
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			World world = entity.getWorld();
			List<Player> playersInRange;
			if (bypassFilter) {
				playersInRange = new ArrayList<>();
				for (Player each : players) {
					Set<HoloMobArmorStand> list = playerStatus.get(each);
					if (list != null) {
						list.remove(entity);
					}
					playersInRange.add(each);
				}
			} else {
				playersInRange = new ArrayList<>();
				for (Player each : players) {
					if ((each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))) {
						Set<HoloMobArmorStand> list = playerStatus.get(each);
						if (list != null) {
							list.remove(entity);
						}
						playersInRange.add(each);
					}
				}
			}
			
			PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
			packet1.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
			
			Bukkit.getScheduler().runTask(plugin, () -> {
				try {
					for (Player player : playersInRange) {
						protocolManager.sendServerPacket(player, packet1);
					}
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			});
		});
	}
	
	private static WrappedDataWatcher buildWarppedDataWatcher(HoloMobArmorStand entity, String entityNameJson, boolean visible) {
		String json = (entityNameJson == null || entityNameJson.equals("")) ? null : (ComponentSerializer.parse(entityNameJson)[0].toPlainText().equals("") ? null : entityNameJson);
		WrappedDataWatcher watcher = new WrappedDataWatcher(); 
	    
		if (entity.getType().equals(EntityType.ARMOR_STAND)) {
			byte bitmask = 0x20;
			if (HoloMobHealth.version.isOld()) {
				watcher.setObject(0, bitmask);
			} else {
				watcher.setObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), bitmask);
			}
			
			if (json != null) {
		    	if (HoloMobHealth.version.isOld()) {
			    	watcher.setObject(2, LanguageUtils.convert(ComponentSerializer.parse(json)[0], HoloMobHealth.language).toLegacyText());
		    	} else if (HoloMobHealth.version.isLegacy()) {
			    	Serializer serializer = Registry.get(String.class);
			    	WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
			    	watcher.setObject(object, LanguageUtils.convert(ComponentSerializer.parse(json)[0], HoloMobHealth.language).toLegacyText());
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
		    	watcher.setObject(5, (byte) 1);
		    } else {
		    	watcher.setObject(new WrappedDataWatcherObject(3, Registry.get(Boolean.class)), json != null ? ((ComponentSerializer.parse(json)[0].toPlainText().equals("")) ? false : visible) : false);
		    	watcher.setObject(new WrappedDataWatcherObject(5, Registry.get(Boolean.class)), true);
		    }
		    
		    byte standbitmask = 0x01 | 0x10;	
	
		    if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_15)) {
		    	watcher.setObject(new WrappedDataWatcherObject(14, Registry.get(Byte.class)), standbitmask);
		    } else if (HoloMobHealth.version.equals(MCVersion.V1_14)) {
		    	watcher.setObject(new WrappedDataWatcherObject(13, Registry.get(Byte.class)), standbitmask);
		    } else if (!HoloMobHealth.version.isOld()) {
				watcher.setObject(new WrappedDataWatcherObject(11, Registry.get(Byte.class)), standbitmask);
			} else {
				watcher.setObject(10, standbitmask);
			}
		} else {
			byte bitmask = 0x20;
			if (HoloMobHealth.version.isOld()) {
				watcher.setObject(0, bitmask);
			} else {
				watcher.setObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), bitmask);
			}
			
			if (!HoloMobHealth.version.isOld()) {
		    	watcher.setObject(new WrappedDataWatcherObject(4, Registry.get(Boolean.class)), true);
			} else {
				watcher.setObject(4, (byte) 1);
			}
	
		    if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_15)) {
		    	watcher.setObject(new WrappedDataWatcherObject(15, Registry.get(Boolean.class)), true);
		    } else if (HoloMobHealth.version.equals(MCVersion.V1_14)) {
		    	watcher.setObject(new WrappedDataWatcherObject(14, Registry.get(Boolean.class)), true);
		    } else if (!HoloMobHealth.version.isOld()) {
				watcher.setObject(new WrappedDataWatcherObject(12, Registry.get(Boolean.class)), true);
			} else {
				watcher.setObject(10, (byte) 1);
			}
		}
		
	    return watcher;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		playerStatus.put(event.getPlayer(), Collections.newSetFromMap(new ConcurrentHashMap<>()));
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		playerStatus.put(event.getPlayer(), Collections.newSetFromMap(new ConcurrentHashMap<>()));
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		playerStatus.remove(event.getPlayer());
	}

}
