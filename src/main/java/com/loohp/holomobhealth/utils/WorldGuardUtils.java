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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardUtils {

    private static StateFlag damageIndicatorFlag;
    private static StateFlag regenIndicatorFlag;

    public static void registerFlag() {
        damageIndicatorFlag = new StateFlag("hmh-damage-indicator", true);
        regenIndicatorFlag = new StateFlag("hmh-regen-indicator", true);
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(damageIndicatorFlag);
        } catch (FlagConflictException | IllegalStateException e) {
            Flag<?> existingDamageFlag = registry.get("hmh-damage-indicator");
            if (existingDamageFlag instanceof StateFlag) {
                damageIndicatorFlag = (StateFlag) existingDamageFlag;
            }
        }
        try {
            registry.register(regenIndicatorFlag);
        } catch (FlagConflictException | IllegalStateException e) {
            Flag<?> existingFlag = registry.get("hmh-regen-indicator");
            if (existingFlag instanceof StateFlag) {
                regenIndicatorFlag = (StateFlag) existingFlag;
            }
        }
    }

    public static StateFlag getDamageIndicatorFlag() {
        return damageIndicatorFlag;
    }

    public static StateFlag getRegenIndicatorFlag() {
        return regenIndicatorFlag;
    }

    public static boolean checkStateFlag(Location location, Player player, StateFlag... stateFlags) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.testState(BukkitAdapter.adapt(location), player == null ? null : WorldGuardPlugin.inst().wrapPlayer(player), stateFlags);
    }

}
