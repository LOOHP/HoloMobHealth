package com.loohp.holomobhealth.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.loohp.holomobhealth.HoloMobHealth;

public class NMSUtils {
	
	private static Class<?> craftWorldClass;
	private static Class<?> craftEntityClass;
	private static Class<?> nmsEntityClass;
	private static Class<?> nmsWorldServerClass;
	private static Method craftWorldGetHandleMethod;
	private static Method nmsWorldServerGetEntityByIDMethod;
	private static Method nmsWorldServerGetEntityByUUIDMethod;
	private static Method nmsEntityGetBukkitEntityMethod;
	private static Method nmsEntityGetUniqueIDMethod;
	private static Method nmsEntityGetBoundingBox;
	private static Method nmsEntityGetHandle;
	private static Class<?> nmsAxisAlignedBBClass;
	private static Field[] nmsAxisAlignedBBFields;
	
	static {
		try {craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");} catch (Exception e) {}
		try {craftEntityClass = getNMSClass("org.bukkit.craftbukkit.", "entity.CraftEntity");} catch (Exception e) {}
		try {nmsEntityClass = getNMSClass("net.minecraft.server.", "Entity");} catch (Exception e) {}
		try {nmsWorldServerClass = getNMSClass("net.minecraft.server.", "WorldServer");} catch (Exception e) {}
		try {craftWorldGetHandleMethod = craftWorldClass.getMethod("getHandle");} catch (Exception e) {}
		try {nmsWorldServerGetEntityByIDMethod = nmsWorldServerClass.getMethod("getEntity", int.class);} catch (Exception e) {}
		try {nmsWorldServerGetEntityByUUIDMethod = nmsWorldServerClass.getMethod("getEntity", UUID.class);} catch (Exception e) {}
		try {nmsEntityGetBukkitEntityMethod = nmsEntityClass.getMethod("getBukkitEntity");} catch (Exception e) {}
		try {nmsEntityGetUniqueIDMethod = nmsEntityClass.getMethod("getUniqueID");} catch (Exception e) {}		
		try {nmsEntityGetBoundingBox = nmsEntityClass.getMethod("getBoundingBox");} catch (Exception e) {}
		try {nmsEntityGetHandle = craftEntityClass.getMethod("getHandle");} catch (Exception e) {}
		try {nmsAxisAlignedBBClass = getNMSClass("net.minecraft.server.", "AxisAlignedBB");} catch (Exception e) {}
		try {nmsAxisAlignedBBFields = nmsAxisAlignedBBClass.getFields();} catch (Exception e) {}
	}
	
	public static UUID getEntityUUIDFromID(World world, int id) {
		if (HoloMobHealth.version.isOld()) {
			List<Entity> entities = world.getEntities();
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				if (entity.getEntityId() == id) {
					return entity.getUniqueId();
				}
			}
			return null;
		} else {
			try {
				Object craftWorldObject = craftWorldClass.cast(world);
				Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
				Object nmsEntityObject = nmsWorldServerGetEntityByIDMethod.invoke(nmsWorldServerObject, id);
				if (nmsEntityObject == null) {
					return null;
				}
				return (UUID) nmsEntityGetUniqueIDMethod.invoke(nmsEntityObject);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static Entity getEntityFromUUID(UUID uuid) {
		for (World world : Bukkit.getWorlds()) {
			try {
				Object craftWorldObject = craftWorldClass.cast(world);
				Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
				Object nmsEntityObject = nmsWorldServerGetEntityByUUIDMethod.invoke(nmsWorldServerObject, uuid);
				if (nmsEntityObject == null) {
					continue;
				}
				return (Entity) nmsEntityGetBukkitEntityMethod.invoke(nmsEntityObject);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static double getEntityHeight(Entity entity) {
		try {
			Object craftEntityObject = craftEntityClass.cast(entity);
			Object nmsEntityObject = nmsEntityGetHandle.invoke(craftEntityObject);
			Object aabbObj = nmsEntityGetBoundingBox.invoke(nmsEntityObject);
			double minY = nmsAxisAlignedBBFields[1].getDouble(aabbObj);
			double maxY = nmsAxisAlignedBBFields[4].getDouble(aabbObj);
			return maxY - minY;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static double getEntityWidth(Entity entity) {
		try {
			Object craftEntityObject = craftEntityClass.cast(entity);
			Object nmsEntityObject = nmsEntityGetHandle.invoke(craftEntityObject);
			Object aabbObj = nmsEntityGetBoundingBox.invoke(nmsEntityObject);
			double minX = nmsAxisAlignedBBFields[0].getDouble(aabbObj);
			double maxX = nmsAxisAlignedBBFields[3].getDouble(aabbObj);
			return maxX - minX;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }

}
