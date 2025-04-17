/*
 * This file is part of HoloMobHealth2.
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

import org.bukkit.entity.Entity;

public class MythicMobsUtils {

    private static boolean mythicMobs5;

    static {
        try {
            Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            mythicMobs5 = true;
        } catch (Throwable e) {
            mythicMobs5 = false;
        }
    }

    public static boolean isMythicMob(Entity entity) {
        return mythicMobs5 ? MythicMobs5Utils.isMythicMob(entity) : MythicMobs4Utils.isMythicMob(entity);
    }

    public static String getMobCustomName(Entity entity) {
        return mythicMobs5 ? MythicMobs5Utils.getMobCustomName(entity) : MythicMobs4Utils.getMobCustomName(entity);
    }

}
