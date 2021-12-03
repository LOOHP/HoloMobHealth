package com.loohp.holomobhealth.utils;

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
	
	private static Field nmsWorldEntityManagerField;
	private static Method nmsEntityManagerGetEntityGetterMethod;
	private static Class<?> nmsLevelEntityGetterClass;
	private static Method nmsLevelEntityGetterGetEntityByIDMethod;
	private static Method nmsLevelEntityGetterGetEntityByUUIDMethod;
	
	static {
		try {
			craftWorldClass = getNMSClass("org.bukkit.craftbukkit.%s.CraftWorld");
			craftEntityClass = getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
			nmsEntityClass = getNMSClass("net.minecraft.server.%s.Entity", "net.minecraft.world.entity.Entity");
			nmsWorldServerClass = getNMSClass("net.minecraft.server.%s.WorldServer", "net.minecraft.server.level.WorldServer");
			craftWorldGetHandleMethod = craftWorldClass.getMethod("getHandle");
			try {
				nmsWorldServerGetEntityByIDMethod = nmsWorldServerClass.getMethod("getEntity", int.class);
			} catch (NoSuchMethodException e) {
				nmsWorldServerGetEntityByIDMethod = nmsWorldServerClass.getMethod("a", int.class);
			}
			try {
				nmsWorldServerGetEntityByUUIDMethod = nmsWorldServerClass.getMethod("getEntity", UUID.class);
			} catch (NoSuchMethodException e) {
				nmsWorldServerGetEntityByUUIDMethod = nmsWorldServerClass.getMethod("a", UUID.class);
			}
			nmsEntityGetBukkitEntityMethod = nmsEntityClass.getMethod("getBukkitEntity");
			try {
				nmsEntityGetUniqueIDMethod = nmsEntityClass.getMethod("getUniqueID");
			} catch (NoSuchMethodException e) {
				nmsEntityGetUniqueIDMethod = nmsEntityClass.getMethod("cm");
			}
			try {
				nmsEntityGetBoundingBox = nmsEntityClass.getMethod("getBoundingBox");
			} catch (NoSuchMethodException e) {
				nmsEntityGetBoundingBox = nmsEntityClass.getMethod("cw");
			}
			nmsEntityGetHandle = craftEntityClass.getMethod("getHandle");
			nmsAxisAlignedBBClass = getNMSClass("net.minecraft.server.%s.AxisAlignedBB", "net.minecraft.world.phys.AxisAlignedBB");
			nmsAxisAlignedBBFields = nmsAxisAlignedBBClass.getFields();
			if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
				if (HoloMobHealth.version.equals(MCVersion.V1_17)) {
					nmsWorldEntityManagerField = nmsWorldServerClass.getDeclaredField("G");
				} else {
					nmsWorldEntityManagerField = nmsWorldServerClass.getDeclaredField("P");
				}
				nmsEntityManagerGetEntityGetterMethod = nmsWorldEntityManagerField.getType().getMethod("d");//
				nmsLevelEntityGetterClass = Class.forName("net.minecraft.world.level.entity.LevelEntityGetterAdapter");
				nmsLevelEntityGetterGetEntityByIDMethod = nmsLevelEntityGetterClass.getMethod("a", int.class);//
				nmsLevelEntityGetterGetEntityByUUIDMethod = nmsLevelEntityGetterClass.getMethod("a", UUID.class);//
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static UUID getEntityUUIDFromID(World world, int id) {
		if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
			try {
				Object craftWorldObject = craftWorldClass.cast(world);
				Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
				nmsWorldEntityManagerField.setAccessible(true);
				Object nmsEntityManagerObject = nmsWorldEntityManagerField.get(nmsWorldServerObject);
				Object nmsLevelEntityGetterObject = nmsEntityManagerGetEntityGetterMethod.invoke(nmsEntityManagerObject);
				Object nmsEntityObject = nmsLevelEntityGetterGetEntityByIDMethod.invoke(nmsLevelEntityGetterObject, id);
				if (nmsEntityObject == null) {
					return null;
				}
				return (UUID) nmsEntityGetUniqueIDMethod.invoke(nmsEntityObject);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		} else if (HoloMobHealth.version.isOld()) {
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
		Entity entity = null;
		if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
			for (World world : Bukkit.getWorlds()) {
				try {
					Object craftWorldObject = craftWorldClass.cast(world);
					Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
					nmsWorldEntityManagerField.setAccessible(true);
					Object nmsEntityManagerObject = nmsWorldEntityManagerField.get(nmsWorldServerObject);
					Object nmsLevelEntityGetterObject = nmsEntityManagerGetEntityGetterMethod.invoke(nmsEntityManagerObject);
					Object nmsEntityObject = nmsLevelEntityGetterGetEntityByUUIDMethod.invoke(nmsLevelEntityGetterObject, uuid);
					if (nmsEntityObject == null) {
						continue;
					}
					return (Entity) nmsEntityGetBukkitEntityMethod.invoke(nmsEntityObject);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} else if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_12)) {
			entity = Bukkit.getEntity(uuid);
		} else {
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
		}
		if (entity == null) {
			entity = StackerUtils.getEntityFromStack(uuid);
		}
		return entity;
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
	
	public static Class<?> getNMSClass(String path, String... paths) throws ClassNotFoundException {	
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        ClassNotFoundException error = null;
        try {
    		return Class.forName(path.replace("%s", version));
    	} catch (ClassNotFoundException e) {
    		error = e;
    	}
        for (String classpath : paths) {
        	try {
        		return Class.forName(classpath.replace("%s", version));
        	} catch (ClassNotFoundException e) {
        		error = e;
        	}
        }
        throw error;
    }

}
