package com.loohp.holomobhealth.Utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class EntityTypeUtils {
	
	private static Set<EntityType> MobTypesSet = new HashSet<EntityType>();
	
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
	}
    
    public static Set<EntityType> getMobsTypesSet() {
    	return Collections.unmodifiableSet(MobTypesSet);
    }
	
}
