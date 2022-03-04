/*
 * This file is part of HoloMobHealth.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class BoundingBoxUtils {

    private static Class<?> craftEntityClass;
    private static Class<?> nmsEntityClass;
    private static Method craftEntityGetHandlerMethod;
    private static Class<?> nmsAxisAlignedBBClass;
    private static Method nmsEntityGetBoundingBoxMethod;
    private static Field[] nmsAxisAlignedBBFields;

    static {
        try {
            craftEntityClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
            nmsEntityClass = NMSUtils.getNMSClass("net.minecraft.server.%s.Entity", "net.minecraft.world.entity.Entity");
            craftEntityGetHandlerMethod = craftEntityClass.getMethod("getHandle");
            nmsAxisAlignedBBClass = NMSUtils.getNMSClass("net.minecraft.server.%s.AxisAlignedBB", "net.minecraft.world.phys.AxisAlignedBB");
            nmsEntityGetBoundingBoxMethod = nmsEntityClass.getMethod("getBoundingBox");
            nmsEntityGetBoundingBoxMethod = NMSUtils.reflectiveLookup(Method.class, () -> {
                return nmsEntityClass.getMethod("getBoundingBox");
            }, () -> {
                return nmsEntityClass.getMethod("cx");
            });
            nmsAxisAlignedBBFields = nmsAxisAlignedBBClass.getFields();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static BoundingBox getBoundingBox(Entity entity) {
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_14)) {
            org.bukkit.util.BoundingBox bukkitBox = entity.getBoundingBox();
            return BoundingBox.of(bukkitBox.getMin(), bukkitBox.getMax());
        } else {
            try {
                Object craftEntityObject = craftEntityClass.cast(entity);
                Object nmsEntityObject = craftEntityGetHandlerMethod.invoke(craftEntityObject);
                Object axisAlignedBBObject = nmsEntityGetBoundingBoxMethod.invoke(nmsEntityObject);
                double[] values = Arrays.stream(nmsAxisAlignedBBFields).mapToDouble(field -> {
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
