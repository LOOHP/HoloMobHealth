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

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.Optional;

@SuppressWarnings("resource")
public class MythicMobs4Utils {

    public static boolean isMythicMob(Entity entity) {
        return MythicMobs.inst().getMobManager().isActiveMob(entity.getUniqueId());
    }

    public static String getMobCustomName(Entity entity) {
        Optional<ActiveMob> optionalActiveMob = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId());
        if (optionalActiveMob.isPresent()) {
            try {
                ActiveMob activeMob = optionalActiveMob.get();
                return activeMob.getEntity() == null ? "Unknown" : activeMob.getEntity().getName();
            } catch (Throwable e1) {
                try {
                    Object type = optionalActiveMob.get().getType();
                    Method method = type.getClass().getMethod("getDisplayName");
                    return method.invoke(type).toString();
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

}
