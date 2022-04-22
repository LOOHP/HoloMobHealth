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
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardUtils {

    private static StateFlag HEALTH_DISPLAY_FLAG;
    private static StateFlag DAMAGE_INDICATOR_FLAG;
    private static StateFlag REGEN_INDICATOR_FLAG;

    public static void registerFlag() {
        HEALTH_DISPLAY_FLAG = new StateFlag("hmh-health-display", true);
        DAMAGE_INDICATOR_FLAG = new StateFlag("hmh-damage-indicator", true);
        REGEN_INDICATOR_FLAG = new StateFlag("hmh-regen-indicator", true);
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(HEALTH_DISPLAY_FLAG);
        } catch (FlagConflictException | IllegalStateException e) {
            Flag<?> existingDamageFlag = registry.get("hmh-health-display");
            if (existingDamageFlag instanceof StateFlag) {
                HEALTH_DISPLAY_FLAG = (StateFlag) existingDamageFlag;
            }
        }
        try {
            registry.register(DAMAGE_INDICATOR_FLAG);
        } catch (FlagConflictException | IllegalStateException e) {
            Flag<?> existingDamageFlag = registry.get("hmh-damage-indicator");
            if (existingDamageFlag instanceof StateFlag) {
                DAMAGE_INDICATOR_FLAG = (StateFlag) existingDamageFlag;
            }
        }
        try {
            registry.register(REGEN_INDICATOR_FLAG);
        } catch (FlagConflictException | IllegalStateException e) {
            Flag<?> existingFlag = registry.get("hmh-regen-indicator");
            if (existingFlag instanceof StateFlag) {
                REGEN_INDICATOR_FLAG = (StateFlag) existingFlag;
            }
        }
    }

    public static StateFlag getHealthDisplayFlag() {
        return HEALTH_DISPLAY_FLAG;
    }

    public static StateFlag getDamageIndicatorFlag() {
        return DAMAGE_INDICATOR_FLAG;
    }

    public static StateFlag getRegenIndicatorFlag() {
        return REGEN_INDICATOR_FLAG;
    }

    public static boolean checkStateFlag(Location location, Player player, StateFlag... stateFlags) {
        return checkStateFlag(location, player, true, stateFlags);
    }

    public static boolean checkStateFlag(Location location, Player player, boolean checkBypass, StateFlag... stateFlags) {
        WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
        LocalPlayer localPlayer = player == null ? null : WorldGuardPlugin.inst().wrapPlayer(player);
        RegionQuery query = platform.getRegionContainer().createQuery();
        if (checkBypass && localPlayer != null && platform.getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return true;
        }
        return query.testState(BukkitAdapter.adapt(location), localPlayer, stateFlags);
    }

}
