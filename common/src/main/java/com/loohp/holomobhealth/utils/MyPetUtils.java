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

import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class MyPetUtils {

    public static boolean isMyPet(Entity entity) {
        return entity instanceof MyPetBukkitEntity;
    }

    public static EntityType getMyPetEntityType(Entity entity) {
        if (isMyPet(entity)) {
            MyPetBukkitEntity myPetEntity = (MyPetBukkitEntity) entity;
            String typeName = myPetEntity.getPetType().getBukkitName();
            try {
                return EntityType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                return EntityType.UNKNOWN;
            }
        }
        return entity.getType();
    }

}
