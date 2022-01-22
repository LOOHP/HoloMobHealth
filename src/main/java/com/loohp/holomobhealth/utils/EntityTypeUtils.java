package com.loohp.holomobhealth.utils;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EntityTypeUtils {

    private static final Set<EntityType> mobTypesSet = new HashSet<EntityType>();

    static {
        mobTypesSet.clear();
        for (EntityType each : EntityType.values()) {
            if (each.equals(EntityType.PLAYER) || each.equals(EntityType.ARMOR_STAND) || each.equals(EntityType.UNKNOWN)) {
                continue;
            }
            Set<Class<?>> clazzList = ClassUtils.getAllExtendedOrImplementedTypesRecursively(each.getEntityClass());
            if (clazzList.contains(LivingEntity.class)) {
                mobTypesSet.add(each);
            }
        }
        mobTypesSet.add(EntityType.UNKNOWN);
    }

    public static Set<EntityType> getMobsTypesSet() {
        return Collections.unmodifiableSet(mobTypesSet);
    }

}
