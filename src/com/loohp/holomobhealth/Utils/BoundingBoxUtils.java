package com.loohp.holomobhealth.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import com.loohp.holomobhealth.HoloMobHealth;

public class BoundingBoxUtils {
	
	private static Class<?> craftEntityClass;
	private static Class<?> nmsEntityClass;
	private static MethodHandle craftEntityGetHandlerMethod;
	private static Class<?> nmsAxisAlignedBBClass;
	private static MethodHandle nmsEntityGetBoundingBoxMethod;
	
	public static void setup() {
		try {
			craftEntityClass = getNMSClass("org.bukkit.craftbukkit.", "entity.CraftEntity");
			nmsEntityClass = getNMSClass("net.minecraft.server.", "Entity");
			craftEntityGetHandlerMethod = MethodHandles.lookup().findVirtual(craftEntityClass, "getHandle", MethodType.methodType(nmsEntityClass));
			nmsAxisAlignedBBClass = getNMSClass("net.minecraft.server.", "AxisAlignedBB");
			nmsEntityGetBoundingBoxMethod = MethodHandles.lookup().findVirtual(nmsEntityClass, "getBoundingBox", MethodType.methodType(nmsAxisAlignedBBClass));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static double getBoundingYHeight(Entity entity) {
		if (HoloMobHealth.version.equals(MCVersion.V1_14) || HoloMobHealth.version.equals(MCVersion.V1_15) || HoloMobHealth.version.equals(MCVersion.V1_16)) {
			return entity.getBoundingBox().getMaxY() - entity.getBoundingBox().getMinY();
		}
		try {
			Object craftEntity = craftEntityClass.cast(entity);
			Object nmsEntity = craftEntityGetHandlerMethod.invoke(craftEntity);
			Object nmsAxisAlignedBB = nmsEntityGetBoundingBoxMethod.invoke(nmsEntity);
			Field maxY = nmsAxisAlignedBB.getClass().getField("maxY");
			Field minY = nmsAxisAlignedBB.getClass().getField("minY");
			return maxY.getDouble(nmsAxisAlignedBB) - minY.getDouble(nmsAxisAlignedBB);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return 0.0; 
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
	    String name = prefix + version + nmsClassString;
	    return Class.forName(name);
	}

}
