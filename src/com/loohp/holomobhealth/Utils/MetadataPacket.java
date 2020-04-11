package com.loohp.holomobhealth.Utils;

import java.lang.reflect.InvocationTargetException;
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

import net.md_5.bungee.api.ChatColor;

public class MetadataPacket {
	
	public static void sendMetadataPacket(Player player, Entity entity, String name, boolean visible) {
		
		PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		
		packet.getIntegers().write(0, entity.getEntityId()); //Set packet's entity id
	    WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
	    watcher.setEntity(player); //Set the new data watcher's target
	    
	    if (name != null) {
		    if (HoloMobHealth.version.contains("legacy")) {
		    	Serializer serializer = Registry.get(String.class);
		    	WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
		    	watcher.setObject(object, name);
		    } else {
		    	Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(name)[0].getHandle());
		    	watcher.setObject(new WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), opt);
		    }
	    } else {
	    	if (HoloMobHealth.version.contains("legacy")) {
		    	Serializer serializer = Registry.get(String.class);
		    	WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
		    	watcher.setObject(object, "");
		    } else {
		    	Optional<?> opt = Optional.empty();
		    	watcher.setObject(new WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), opt);
		    }
	    }
	    
	    watcher.setObject(new WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), visible);
	    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
	    try {
	    	HoloMobHealth.protocolManager.sendServerPacket(player, packet);
	    } catch (InvocationTargetException e) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "HoloMobHealth failed to send update packet to " + player.getName());
	        e.printStackTrace();
	    }
	}

}