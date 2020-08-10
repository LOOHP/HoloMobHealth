package com.loohp.holomobhealth.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class NMSUtils {
	
	private static Class<?> craftWorldClass;
	private static Class<?> nmsEntityClass;
	private static Class<?> nmsWorldServerClass;
	private static MethodHandle craftWorldGetHandleMethod;
	private static MethodHandle nmsWorldServerGetEntityMethod;
	private static MethodHandle nmsEntityGetUniqueIDMethod;
	
	static {
		try {
		craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");
		nmsEntityClass = getNMSClass("net.minecraft.server.", "Entity");
		nmsWorldServerClass = getNMSClass("net.minecraft.server.", "WorldServer");
		craftWorldGetHandleMethod = MethodHandles.lookup().findVirtual(craftWorldClass, "getHandle", MethodType.methodType(nmsWorldServerClass));
		nmsWorldServerGetEntityMethod = MethodHandles.lookup().findVirtual(nmsWorldServerClass, "getEntity", MethodType.methodType(nmsEntityClass, int.class));
		nmsEntityGetUniqueIDMethod = MethodHandles.lookup().findVirtual(nmsEntityClass, "getUniqueID", MethodType.methodType(UUID.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static UUID getEntityUUIDFromID(World world, int id) {
		try {
			Object craftWorldObject = craftWorldClass.cast(world);
			Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
			Object nmsEntityObject = nmsWorldServerGetEntityMethod.invoke(nmsWorldServerObject, id);
			if (nmsEntityObject == null) {
				return null;
			}
			return (UUID) nmsEntityGetUniqueIDMethod.invoke(nmsEntityObject);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }

}
