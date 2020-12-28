package com.loohp.holomobhealth.Protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;

import net.md_5.bungee.chat.ComponentSerializer;

public class EntityMetadata {
	
	public static void updatePlayer(Player player) {
		List<Player> players = new ArrayList<>();
		players.add(player);
		int range = HoloMobHealth.getUpdateRange(player.getWorld());
		for (Entity entity : player.getNearbyEntities(range, range, range)) {
			updateEntity(players, entity);
		}
	}
	
	public static void updateEntity(Player player, Entity entity) {
		List<Player> players = new ArrayList<>();
		players.add(player);
		updateEntity(players, entity);
	}
	
	public static void updateEntity(Collection<? extends Player> players, Entity entity) {
		if (!EntityTypeUtils.getMobsTypesSet().contains(entity.getType())) {
			return;
		}
		if (!entity.isValid()) {
			return;
		}
		PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entity.getEntityId());
		packet.getWatchableCollectionModifier().write(0, WrappedDataWatcher.getEntityWatcher(entity).getWatchableObjects());
		
		for (Player player : players) {
			if (player.getWorld().equals(entity.getWorld())) {
				try {
					HoloMobHealth.protocolManager.sendServerPacket(player, packet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void sendMetadataPacket(Entity entity, String entityNameJson, boolean visible, List<Player> players, boolean quiet) {
		String json = (entityNameJson == null || entityNameJson.equals("")) ? null : entityNameJson;
		Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> {

			PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
			
			packet.getIntegers().write(0, entity.getEntityId()); //Set packet's entity id
		    WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
		    
		    if (json != null) {
		    	if (HoloMobHealth.version.isOld()) {
			    	watcher.setObject(2, ComponentSerializer.parse(json)[0].toLegacyText());
		    	} else if (HoloMobHealth.version.isLegacy()) {
			    	Serializer serializer = Registry.get(String.class);
			    	WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
			    	watcher.setObject(object, ComponentSerializer.parse(json)[0].toLegacyText());
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
		    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
    
    		try {
		    	for (Player player : players) {
		    		if (player.hasPermission("holomobhealth.use")) {
		    			HoloMobHealth.protocolManager.sendServerPacket(player, packet, !quiet);
					}
		    	}
		    } catch (InvocationTargetException e) {
		        e.printStackTrace();
		    }
    	});
	}

}
