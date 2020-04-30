package com.loohp.holomobhealth.Protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Holders.HoloMobArmorStand;
import com.loohp.holomobhealth.Holders.HoloMobCache;

import net.md_5.bungee.chat.ComponentSerializer;

public class ArmorStandPacket implements Listener {
	
	private static ProtocolManager protocolManager = HoloMobHealth.protocolManager;
	private static String version = HoloMobHealth.version;
	public static Set<HoloMobArmorStand> active = Collections.newSetFromMap(Collections.synchronizedMap(new LinkedHashMap<HoloMobArmorStand, Boolean>()));
	private static HashMap<Integer, HoloMobCache> cache = new HashMap<Integer, HoloMobCache>();
	
	public static void sendArmorStandSpawn(Collection<? extends Player> players, HoloMobArmorStand entity, String json, boolean visible) {
		if (players.isEmpty()) {
			return;
		}
		if (!active.contains(entity)) {
			active.add(entity);
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
		packet1.getIntegers().write(0, entity.getEntityId());
		if (version.equals("1.15")) {
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 60);
		} else if (version.equals("1.14")) {
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 59);
		} else if (version.equals("1.13")) {
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 56);
		} else {
			packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 30 : 101);
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
        
        PacketContainer packet3 = protocolManager.createPacket(PacketType.Play.Server.MOUNT);
    	packet3.getIntegers().write(0, entity.getMountId());
    	packet3.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
        
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
				protocolManager.sendServerPacket(player, packet2);
				protocolManager.sendServerPacket(player, packet3);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateArmorStand(Collection<? extends Player> players, HoloMobArmorStand entity, String json, boolean visible) {
		updateArmorStand(players, entity, json, visible, false);
	}
	
	public static void updateArmorStand(Collection<? extends Player> players, HoloMobArmorStand entity, String json, boolean visible, boolean bypasscache) {
		if (players.isEmpty()) {
			return;
		}
		if (!bypasscache) {
			HoloMobCache hmc = cache.get(entity.getEntityId());
			if (hmc != null) {
				if (hmc.getCustomName().equals(json) && hmc.getCustomNameVisible() == visible) {
					return;
				}
			}
		}
			
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet1.getIntegers().write(0, entity.getEntityId());	
        WrappedDataWatcher wpw = buildWarppedDataWatcher(entity, json, visible);
        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
        
        cache.put(entity.getEntityId(), new HoloMobCache(json, visible));
	}
	
	public static void removeArmorStand(Collection<? extends Player> players, HoloMobArmorStand entity, boolean removeFromActive) {
		if (players.isEmpty()) {
			return;
		}
		if (removeFromActive) {
			active.remove(entity);
		}
		cache.remove(entity.getEntityId());
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet1.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
		
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	private static WrappedDataWatcher buildWarppedDataWatcher(HoloMobArmorStand entity, String entityNameJson, boolean visible) {
		String json = (entityNameJson == null || entityNameJson.equals("")) ? null : (ComponentSerializer.parse(entityNameJson)[0].toPlainText().equals("") ? null : entityNameJson);
		WrappedDataWatcher watcher = new WrappedDataWatcher(); 
	    
		if (entity.getType().equals(EntityType.ARMOR_STAND)) {
			byte bitmask = 0x20;
			if (HoloMobHealth.version.contains("OLD")) {
				watcher.setObject(0, bitmask);
			} else {
				watcher.setObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), bitmask);
			}
			
			if (json != null) {
		    	if (HoloMobHealth.version.contains("OLD")) {
			    	watcher.setObject(2, json);
		    	} else if (HoloMobHealth.version.contains("legacy")) {
			    	Serializer serializer = Registry.get(String.class);
			    	WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
			    	watcher.setObject(object, json);
			    } else {
			    	Optional<?> opt = Optional.of(WrappedChatComponent.fromJson(json).getHandle());
			    	watcher.setObject(new WrappedDataWatcherObject(2, Registry.getChatComponentSerializer(true)), opt);
			    }
		    } else {
		    	if (HoloMobHealth.version.contains("OLD")) {
		    		watcher.setObject(2, "");
		    	} else if (HoloMobHealth.version.contains("legacy")) {
			    	Serializer serializer = Registry.get(String.class);
			    	WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
			    	watcher.setObject(object, "");
			    } else {
			    	Optional<?> opt = Optional.empty();
			    	watcher.setObject(new WrappedDataWatcherObject(2, Registry.getChatComponentSerializer(true)), opt);
			    }
		    }
		    
		    if (HoloMobHealth.version.contains("OLD")) {
		    	watcher.setObject(3, (byte) (visible ? 1 : 0));
		    	watcher.setObject(5, (byte) 1);
		    } else {
		    	watcher.setObject(new WrappedDataWatcherObject(3, Registry.get(Boolean.class)), json != null ? ((ComponentSerializer.parse(json)[0].toPlainText().equals("")) ? false : visible) : false);
		    	watcher.setObject(new WrappedDataWatcherObject(5, Registry.get(Boolean.class)), true);
		    }
		    
		    byte standbitmask = 0x01 | 0x10;	
	
		    if (HoloMobHealth.version.equals("1.15")) {
		    	watcher.setObject(new WrappedDataWatcherObject(14, Registry.get(Byte.class)), standbitmask);
		    } else if (HoloMobHealth.version.equals("1.14")) {
		    	watcher.setObject(new WrappedDataWatcherObject(13, Registry.get(Byte.class)), standbitmask);
		    } else if (!HoloMobHealth.version.contains("OLD")) {
				watcher.setObject(new WrappedDataWatcherObject(11, Registry.get(Byte.class)), standbitmask);
			} else {
				watcher.setObject(10, standbitmask);
			}
		} else {
			byte bitmask = 0x20;
			if (HoloMobHealth.version.contains("OLD")) {
				watcher.setObject(0, bitmask);
			} else {
				watcher.setObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), bitmask);
			}
			
			if (!HoloMobHealth.version.contains("OLD")) {
		    	watcher.setObject(new WrappedDataWatcherObject(4, Registry.get(Boolean.class)), true);
			} else {
				watcher.setObject(4, (byte) 1);
			}
	
		    if (HoloMobHealth.version.equals("1.15")) {
		    	watcher.setObject(new WrappedDataWatcherObject(15, Registry.get(Boolean.class)), true);
		    } else if (HoloMobHealth.version.equals("1.14")) {
		    	watcher.setObject(new WrappedDataWatcherObject(14, Registry.get(Boolean.class)), true);
		    } else if (!HoloMobHealth.version.contains("OLD")) {
				watcher.setObject(new WrappedDataWatcherObject(12, Registry.get(Boolean.class)), true);
			} else {
				watcher.setObject(10, (byte) 1);
			}
		}
		
	    return watcher;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskLaterAsynchronously(HoloMobHealth.plugin, () -> {
			Iterator<HoloMobArmorStand> itr = active.iterator();
			while (itr.hasNext()) {
				HoloMobArmorStand entity = itr.next();
				if (entity.getWorld().equals(player.getWorld())) {
					if (entity.getLocation().distanceSquared(player.getLocation()) <= (HoloMobHealth.range + 2) * (HoloMobHealth.range + 2)) {
						Collection<Player> players = new ArrayList<Player>();
						players.add(player);
						Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> sendArmorStandSpawn(players, entity, "", false));
						Bukkit.getConsoleSender().sendMessage(entity.getEntityId() + "->" + entity.getMountId());
						if (cache.get(entity.getEntityId()) != null) {
							Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> updateArmorStand(players, entity, cache.get(entity.getEntityId()).getCustomName(), cache.get(entity.getEntityId()).getCustomNameVisible(), true));
						}
					}
				}
			}
		}, 40);
	}

}
