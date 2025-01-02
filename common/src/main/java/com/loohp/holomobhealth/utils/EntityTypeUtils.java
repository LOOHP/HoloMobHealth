/*
 * This file is part of HoloMobHealth.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.holomobhealth.utils;

import com.loohp.holomobhealth.HoloMobHealth;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EntityTypeUtils {

    private static final Set<EntityType> mobTypesSet = new HashSet<>();

    static {
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

    public static EntityType getEntityType(Entity entity) {
        if (HoloMobHealth.myPetHook) {
            return MyPetUtils.getMyPetEntityType(entity);
        }
        return entity.getType();
    }

}
