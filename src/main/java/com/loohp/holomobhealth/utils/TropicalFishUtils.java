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
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TropicalFishUtils {

    private static final Map<Integer, Integer> PREDEFINED_TROPICAL_FISH = new HashMap<>();

    private static Class<?> craftEntityClass;
    private static Method getNmsEntityMethod;
    private static Class<?> nmsEntityTropicalFishClass;
    private static Method getTropicalFishVarianceMethod;
    private static Class<?> craftTropicalFishClass;
    private static Method getTropicalFishPatternMethod;

    static {
        if (!HoloMobHealth.version.isLegacy()) {
            try {
                craftEntityClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
                getNmsEntityMethod = craftEntityClass.getMethod("getHandle");
                nmsEntityTropicalFishClass = NMSUtils.getNMSClass("net.minecraft.server.%s.EntityTropicalFish", "net.minecraft.world.entity.animal.EntityTropicalFish");
                getTropicalFishVarianceMethod = NMSUtils.reflectiveLookup(Method.class, () -> {
                    return nmsEntityTropicalFishClass.getMethod("getVariant");
                }, () -> {
                    Method method = nmsEntityTropicalFishClass.getMethod("fH");
                    if (!method.getReturnType().equals(int.class)) {
                        throw new NoSuchMethodException("Incorrect return type");
                    }
                    return method;
                }, () -> {
                    return nmsEntityTropicalFishClass.getMethod("fI");
                });
                craftTropicalFishClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftTropicalFish");
                getTropicalFishPatternMethod = craftTropicalFishClass.getMethod("getPattern", int.class);
            } catch (SecurityException | ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), 0);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), 1);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), 2);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), 3);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), 4);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE), 5);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), 6);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), 7);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED), 8);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), 9);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY), 10);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), 11);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK), 12);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), 13);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE), 14);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED), 15);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), 16);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), 17);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.KOB, DyeColor.RED, DyeColor.WHITE), 18);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), 19);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW), 20);
        PREDEFINED_TROPICAL_FISH.put(calculateTropicalFishVariant(TropicalFish.Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW), 21);
    }

    public static String addTropicalFishType(Entity entity, String toAppend) {
        String path = toAppend;
        EntityType type = EntityTypeUtils.getEntityType(entity);
        if (type.equals(EntityType.TROPICAL_FISH)) {
            try {
                Object craftEntityObject = craftEntityClass.cast(entity);
                Object nmsTropicalFishObject = nmsEntityTropicalFishClass.cast(getNmsEntityMethod.invoke(craftEntityObject));
                int variance = (int) getTropicalFishVarianceMethod.invoke(nmsTropicalFishObject);
                int predefinedType = getPredefinedType(variance);
                if (predefinedType >= 0) {
                    path += ".predefined." + predefinedType;
                } else {
                    path += ".type." + getTropicalFishTypeName(variance);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    public static int getPredefinedType(int variance) {
        return PREDEFINED_TROPICAL_FISH.getOrDefault(variance, -1);
    }

    public static int calculateTropicalFishVariant(Pattern bukkitPattern, DyeColor bodyColor, DyeColor patternColor) {
        TropicalFishPattern pattern = TropicalFishPattern.fromPattern(bukkitPattern);
        return pattern.getBase() & 255 | (pattern.getIndex() & 255) << 8 | ((int) bodyColor.getWoolData() & 255) << 16 | ((int) patternColor.getWoolData() & 255) << 24;
    }

    public static int getTropicalFishBaseColorIdx(int variance) {
        return (variance & 16711680) >> 16;
    }

    public static int getTropicalFishPatternColorIdx(int variance) {
        return (variance & -16777216) >> 24;
    }

    public static int getTropicalFishBaseVariant(int variance) {
        return Math.min(variance & 255, 1);
    }

    public static int getTropicalFishPatternVariant(int variance) {
        return Math.min((variance & '\uff00') >> 8, 5);
    }

    public static DyeColor getTropicalFishBaseColor(int variance) {
        return DyeColor.getByWoolData((byte) getTropicalFishBaseColorIdx(variance));
    }

    public static DyeColor getTropicalFishPatternColor(int variance) {
        return DyeColor.getByWoolData((byte) getTropicalFishPatternColorIdx(variance));
    }

    public static String getTropicalFishTypeName(int variance) {
        int base = getTropicalFishBaseVariant(variance);
        int index = getTropicalFishPatternVariant(variance);
        return TropicalFishPattern.getPatternName(base, index);
    }

    public static enum TropicalFishPattern {

        KOB(0, 0),
        SUNSTREAK(0, 1),
        SNOOPER(0, 2),
        DASHER(0, 3),
        BRINELY(0, 4),
        SPOTTY(0, 5),
        FLOPPER(1, 0),
        STRIPEY(1, 1),
        GLITTER(1, 2),
        BLOCKFISH(1, 3),
        BETTY(1, 4),
        CLAYFISH(1, 5);

        private static final TropicalFishPattern[] VALUES = values();

        public static String getPatternName(int base, int index) {
            return VALUES[index + 6 * base].getName();
        }

        public static TropicalFishPattern fromPattern(Pattern pattern) {
            for (TropicalFishPattern wrapper : VALUES) {
                if (wrapper.name().equals(pattern.name())) {
                    return wrapper;
                }
            }
            return null;
        }

        private final int base;
        private final int index;

        TropicalFishPattern(int base, int index) {
            this.base = base;
            this.index = index;
        }

        public int getBase() {
            return this.base;
        }

        public int getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

    }

}
