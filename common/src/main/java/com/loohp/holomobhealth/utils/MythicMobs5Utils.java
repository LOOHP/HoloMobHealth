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

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;

import java.util.Optional;

@SuppressWarnings("resource")
public class MythicMobs5Utils {

    public static boolean isMythicMob(Entity entity) {
        return MythicBukkit.inst().getMobManager().isActiveMob(entity.getUniqueId());
    }

    public static String getMobCustomName(Entity entity) {
        Optional<ActiveMob> optionalActiveMob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
        return optionalActiveMob.map(mob -> mob.getName()).orElse(null);
    }

}
