package com.loohp.holomobhealth.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish.Pattern;

import com.loohp.holomobhealth.HoloMobHealth;

public class TropicalFishUtils {
	
	private static Class<?> craftEntityClass;
	private static Method getNmsEntityMethod;
	private static Class<?> nmsEntityTropicalFishClass;
	private static Method getTropicalFishVarianceMethod;
	private static Class<?> craftTropicalFishClass;
	private static Method getTropicalFishPatternMethod;
	
	private static Map<Integer, Integer> predefined = new HashMap<>();
	
	static {
		if (!HoloMobHealth.version.isLegacy()) {
			try {
				craftEntityClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
				getNmsEntityMethod = craftEntityClass.getMethod("getHandle");
				nmsEntityTropicalFishClass = NMSUtils.getNMSClass("net.minecraft.server.%s.EntityTropicalFish", "net.minecraft.world.entity.animal.EntityTropicalFish");
				getTropicalFishVarianceMethod = nmsEntityTropicalFishClass.getMethod("getVariant");
				craftTropicalFishClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftTropicalFish");
				getTropicalFishPatternMethod = craftTropicalFishClass.getMethod("getPattern", int.class);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			
			predefined.put(117506305, 0);
			predefined.put(117899265, 1);
			predefined.put(185008129, 2);
			predefined.put(117441793, 3);
			predefined.put(118161664, 4);
			predefined.put(65536, 5);
			predefined.put(50726144, 6);
			predefined.put(67764993, 7);
			predefined.put(234882305, 8);
			predefined.put(67110144, 9);
			predefined.put(117441025, 10);
			predefined.put(16778497, 11);
			predefined.put(101253888, 12);
			predefined.put(50660352, 13);
			predefined.put(918529, 14);
			predefined.put(235340288, 15);
			predefined.put(918273, 16);
			predefined.put(67108865, 17);
			predefined.put(917504, 18);
			predefined.put(459008, 19);
			predefined.put(67699456, 20);
			predefined.put(67371009, 21);
		}
	}

	public static String addTropicalFishType(Entity entity, String toAppend) {
		String path = toAppend;
		EntityType type = entity.getType();
		if (type.equals(EntityType.TROPICAL_FISH)) {
			try {
				Object craftEntityObject = craftEntityClass.cast(entity);
				Object nmsTropicalFishObject = nmsEntityTropicalFishClass.cast(getNmsEntityMethod.invoke(craftEntityObject));
				int variance = (int) getTropicalFishVarianceMethod.invoke(nmsTropicalFishObject);
				int prefefinedType = predefined.getOrDefault(variance, -1);
				if (prefefinedType != -1) {
					path += ".predefined." + prefefinedType;
				} else {
					variance = validateAndFixTropicalFishVariant(variance);
					Pattern pattern = (Pattern) getTropicalFishPatternMethod.invoke(null, variance);
					path += ".type." + pattern.toString().toLowerCase();
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return path;
	}
	
	private static int validateAndFixTropicalFishVariant(int data) {
		byte[] bytes = new byte[] {(byte) (data >> 24), (byte) (data >> 16), (byte) (data >> 8), (byte) data};
		if (bytes.length != 4) {
			return 0;
		}
		if (bytes[3] < 0 || bytes[3] > 1) {
			bytes[3] = 1;
		}
		if (bytes[2] < 0 || bytes[2] > 5) {
			bytes[2] = 5;
		}
		if (bytes[1] < 0 || bytes[1] > 15) {
			bytes[1] = 0;
		}
		if (bytes[0] < 0 || bytes[0] > 15) {
			bytes[0] = 0;
		}
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
	}
	
}
