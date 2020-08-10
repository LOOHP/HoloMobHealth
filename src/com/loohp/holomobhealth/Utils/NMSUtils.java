package com.loohp.holomobhealth.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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
	private static MethodHandle craftWorldGetHandleMethod;
	private static MethodHandle nmsWorldServerGetEntityByIDMethod;
	private static MethodHandle nmsWorldServerGetEntityByUUIDMethod;
	private static MethodHandle nmsEntityGetBukkitEntityMethod;
	private static MethodHandle nmsEntityGetUniqueIDMethod;
	
	static {
		try {craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");} catch (Exception e) {}
		try {craftEntityClass = getNMSClass("org.bukkit.craftbukkit.", "entity.CraftEntity");} catch (Exception e) {}
		try {nmsEntityClass = getNMSClass("net.minecraft.server.", "Entity");} catch (Exception e) {}
		try {nmsWorldServerClass = getNMSClass("net.minecraft.server.", "WorldServer");} catch (Exception e) {}
		try {craftWorldGetHandleMethod = MethodHandles.lookup().findVirtual(craftWorldClass, "getHandle", MethodType.methodType(nmsWorldServerClass));} catch (Exception e) {}
		try {nmsWorldServerGetEntityByIDMethod = MethodHandles.lookup().findVirtual(nmsWorldServerClass, "getEntity", MethodType.methodType(nmsEntityClass, int.class));} catch (Exception e) {}
		try {nmsWorldServerGetEntityByUUIDMethod = MethodHandles.lookup().findVirtual(nmsWorldServerClass, "getEntity", MethodType.methodType(nmsEntityClass, UUID.class));} catch (Exception e) {}
		try {nmsEntityGetBukkitEntityMethod = MethodHandles.lookup().findVirtual(nmsEntityClass, "getBukkitEntity", MethodType.methodType(craftEntityClass));} catch (Exception e) {}
		try {nmsEntityGetUniqueIDMethod = MethodHandles.lookup().findVirtual(nmsEntityClass, "getUniqueID", MethodType.methodType(UUID.class));} catch (Exception e) {}
	}
	
	public static UUID getEntityUUIDFromID(World world, int id) {
		if (HoloMobHealth.version.isOld()) {
			for (Entity entity : world.getEntities()) {
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
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }

}
