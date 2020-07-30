package com.loohp.holomobhealth.Utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import com.loohp.holomobhealth.HoloMobHealth;

public class EntityTypeUtils {
	
	private static Set<EntityType> MobTypesSet = new HashSet<EntityType>();
    
    public static Set<EntityType> getMobsTypesSet() {
    	return MobTypesSet;
    }
    
    public static void setUpList() {
    	MobTypesSet.clear();
    	for (EntityType each : EntityType.values()) {
    		if (each.equals(EntityType.PLAYER) || each.equals(EntityType.ARMOR_STAND) || each.equals(EntityType.UNKNOWN)) {
    			continue;
    		}
    		Set<Class<?>> clazzList = ClassUtils.getAllExtendedOrImplementedTypesRecursively(each.getEntityClass());
    		if (clazzList.contains(org.bukkit.entity.LivingEntity.class)) {
    			MobTypesSet.add(each);
    		}
    	}
    	MobTypesSet.add(EntityType.UNKNOWN);
    }
	
	public static String getMinecraftLangName(Entity entity) {	
		if (HoloMobHealth.version.isLegacy()) {
			return LegacyEntityTypeUtils.getLegacyMinecraftName(entity);
		}
		
		EntityType type = entity.getType();
		String path = "";
		if (HoloMobHealth.version.equals(MCVersion.V1_13) || HoloMobHealth.version.equals(MCVersion.V1_13_1)) {
			path = new StringBuilder().append("entity.minecraft.").append(type.name().toLowerCase()).toString();
			if (type.name().equalsIgnoreCase("PIG_ZOMBIE")) {
				path = new StringBuilder().append("entity.minecraft.zombie_pigman").toString();
			}
		} else {
			path = new StringBuilder().append("entity.").append(type.getKey().getNamespace()).append('.').append(type.getKey().getKey()).toString();
		}
		
		path = TropicalFishUtils.addTropicalFishType(entity, path);
		
		if (type.equals(EntityType.VILLAGER)) {
			Villager villager = (Villager) entity;
			if (!(HoloMobHealth.version.equals(MCVersion.V1_13) || HoloMobHealth.version.equals(MCVersion.V1_13_1))) {
				path = new StringBuilder().append(path).append(".").append(villager.getProfession().toString().toLowerCase()).toString();
			}
		}
		return path;
	}
	
	public static void reloadLang() {
		if (HoloMobHealth.version.isLegacy()) {
			LegacyEntityTypeUtils.reloadLegacyLang();
		}
	}
	
	public static void setupLang() {		
		if (HoloMobHealth.version.isLegacy()) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Translatable Components are not supported on this version");
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "HoloMobHealth will use legacy EntityType names method instead!");
	    	LegacyEntityTypeUtils.setupLegacyLang();
	    	return;
	    }
	}
	
}
