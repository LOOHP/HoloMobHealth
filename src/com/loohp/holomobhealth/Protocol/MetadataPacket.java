package com.loohp.holomobhealth.Protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
import com.loohp.holomobhealth.Holders.HoloMobCache;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;

public class MetadataPacket {
	
	private static HashMap<Integer, HoloMobCache> cache = new HashMap<Integer, HoloMobCache>();
	
	public static void sendMetadataPacket(Entity entity, String entityNameJson, boolean visible) {
		String json = (entityNameJson == null || entityNameJson.equals("")) ? null : (ComponentSerializer.parse(entityNameJson)[0].toPlainText().equals("") ? null : entityNameJson);
		Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> {
			
			HoloMobCache hmc = cache.get(entity.getEntityId());
			if (hmc != null) {
				if (hmc.getCustomName().equals(json == null ? "" : json) && hmc.getCustomNameVisible() == visible) {
					return;
				}
			}
			
			PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
			
			packet.getIntegers().write(0, entity.getEntityId()); //Set packet's entity id
		    WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
		    
		    if (json != null) {
		    	if (HoloMobHealth.version.contains("OLD")) {
			    	watcher.setObject(2, ComponentSerializer.parse(json)[0].toLegacyText());
		    	} else if (HoloMobHealth.version.contains("legacy")) {
			    	Serializer serializer = Registry.get(String.class);
			    	WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
			    	watcher.setObject(object, ComponentSerializer.parse(json)[0].toLegacyText());
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
		    } else {
		    	watcher.setObject(new WrappedDataWatcherObject(3, Registry.get(Boolean.class)), visible);
		    }
		    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
		    try {
		    	for (Player player : HoloMobHealth.playersEnabled) {
		    		if (player.hasPermission("holomobhealth.use")) {
		    			HoloMobHealth.protocolManager.sendServerPacket(player, packet);
					}
		    	}
		    } catch (InvocationTargetException e) {
		    	Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "HoloMobHealth failed to send update packet");
		        e.printStackTrace();
		    }
		    
		    cache.put(entity.getEntityId(), new HoloMobCache(json == null ? "" : json, visible));
		});
	}

}
