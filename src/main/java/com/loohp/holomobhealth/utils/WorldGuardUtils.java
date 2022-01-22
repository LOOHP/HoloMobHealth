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
