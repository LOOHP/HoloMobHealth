package com.loohp.holomobhealth.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.loohp.holomobhealth.HoloMobHealth;

public class BoundingBoxUtils {
	
	private static Class<?> craftEntityClass;
	private static Class<?> nmsEntityClass;
	private static Method craftEntityGetHandlerMethod;
	private static Class<?> nmsAxisAlignedBBClass;
	private static Method nmsEntityGetBoundingBoxMethod;
	private static List<Field> nmsAxisAlignedBBFields;
	
	private static void _init_() {
		try {
			craftEntityClass = getNMSClass("org.bukkit.craftbukkit.", "entity.CraftEntity");
			nmsEntityClass = getNMSClass("net.minecraft.server.", "Entity");
			craftEntityGetHandlerMethod = craftEntityClass.getMethod("getHandle");
			nmsAxisAlignedBBClass = getNMSClass("net.minecraft.server.", "AxisAlignedBB");
			nmsEntityGetBoundingBoxMethod = nmsEntityClass.getMethod("getBoundingBox");
			nmsAxisAlignedBBFields = Arrays.asList(nmsAxisAlignedBBClass.getFields());
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
	    String name = prefix + version + nmsClassString;
	    return Class.forName(name);
	}
	
	public static BoundingBox getBoundingBox(Entity entity) {
		if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_14)) {
			org.bukkit.util.BoundingBox bukkitBox = entity.getBoundingBox();
			return BoundingBox.of(bukkitBox.getMin(), bukkitBox.getMax());
		} else {
			if (craftEntityClass == null) {
				_init_();
			}
			try {
				Object craftEntityObject = craftEntityClass.cast(entity);
				Object nmsEntityObject = craftEntityGetHandlerMethod.invoke(craftEntityObject);
				Object axisAlignedBBObject = nmsEntityGetBoundingBoxMethod.invoke(nmsEntityObject);
				double[] values = nmsAxisAlignedBBFields.stream().mapToDouble(field -> {
					try {
						return field.getDouble(axisAlignedBBObject);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
					return 0;
				}).toArray();
				Location min = new Location(entity.getWorld(), values[0], values[1], values[2]);
				Location max = new Location(entity.getWorld(), values[3], values[4], values[5]);
				return BoundingBox.of(min, max);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}
