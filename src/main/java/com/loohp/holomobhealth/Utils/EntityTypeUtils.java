package com.loohp.holomobhealth.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;

import com.loohp.holomobhealth.HoloMobHealth;

public class EntityTypeUtils {
	
	private static Set<EntityType> MobTypesSet = new HashSet<EntityType>();
	
	private static Class<?> craftEntityClass;
	private static Class<?> nmsEntityClass;
	private static Class<?> nmsEntityTypesClass;
	private static Method getNmsEntityMethod;
	private static Method getEntityKeyMethod;
	
	static {
		MobTypesSet.clear();
    	for (EntityType each : EntityType.values()) {
    		if (each.equals(EntityType.PLAYER) || each.equals(EntityType.ARMOR_STAND) || each.equals(EntityType.UNKNOWN)) {
    			continue;
    		}
    		Set<Class<?>> clazzList = ClassUtils.getAllExtendedOrImplementedTypesRecursively(each.getEntityClass());
    		if (clazzList.contains(LivingEntity.class)) {
    			MobTypesSet.add(each);
    		}
    	}
    	MobTypesSet.add(EntityType.UNKNOWN);
    	
		if (HoloMobHealth.version.isLegacy()) {
			try {
				craftEntityClass = getNMSClass("org.bukkit.craftbukkit.", "entity.CraftEntity");
				nmsEntityClass = getNMSClass("net.minecraft.server.", "Entity");
				nmsEntityTypesClass = getNMSClass("net.minecraft.server.", "EntityTypes");
				getNmsEntityMethod = craftEntityClass.getMethod("getHandle");
				getEntityKeyMethod = nmsEntityTypesClass.getMethod("b", nmsEntityClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {	
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }
    
    public static Set<EntityType> getMobsTypesSet() {
    	return Collections.unmodifiableSet(MobTypesSet);
    }
    
    public static String getTranslationKey(Entity entity) {
    	try {
			if (HoloMobHealth.version.isLegacy()) {
				return getLegacyTranslationKey(entity);
			} else {
				return getModernTranslationKey(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    private static String getLegacyTranslationKey(Entity entity) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	String str = getEntityKeyMethod.invoke(null, getNmsEntityMethod.invoke(craftEntityClass.cast(entity))).toString();
    	if (str == null) {
    		return "";
    	} else {
    		EntityType type = entity.getType();
    		if (type.equals(EntityType.VILLAGER)) {
    			Villager villager = (Villager) entity;
    			str += "." + villager.getProfession().toString().toLowerCase();
    		} else {
    			str += ".name";
    		}
    		return "entity." + str;
    	}
    }
	
	private static String getModernTranslationKey(Entity entity) {	
		EntityType type = entity.getType();
		String path = "";
		if (HoloMobHealth.version.equals(MCVersion.V1_13) || HoloMobHealth.version.equals(MCVersion.V1_13_1)) {
			path = "entity.minecraft." + type.name().toLowerCase();
			if (type.name().equalsIgnoreCase("PIG_ZOMBIE")) {
				path = "entity.minecraft.zombie_pigman";
			}
		} else {
			path = "entity." + type.getKey().getNamespace() + "." + type.getKey().getKey();
		}
		
		path = TropicalFishUtils.addTropicalFishType(entity, path);
		
		if (type.equals(EntityType.VILLAGER)) {
			Villager villager = (Villager) entity;
			if (HoloMobHealth.version.equals(MCVersion.V1_13) || HoloMobHealth.version.equals(MCVersion.V1_13_1)) {
				switch (villager.getProfession()) {
				case TOOLSMITH:
					path += ".tool_smith";
					break;
				case WEAPONSMITH:
					path += ".weapon_smith";
					break;
				default:
					path += "." + villager.getProfession().toString().toLowerCase();
					break;
				}
			} else {
				path += "." + villager.getProfession().toString().toLowerCase();
			}
		}
		return path;
	}
	
}
