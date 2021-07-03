package com.loohp.holomobhealth.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class WorldGuardUtils {
	
	public static final StateFlag DAMAGE_INDICATOR_FLAG = new StateFlag("hmh-damage-indicator", true);
	public static final StateFlag REGEN_INDICATOR_FLAG = new StateFlag("hmh-regen-indicator", true);

	public static void registerFlag() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		try {
			registry.register(DAMAGE_INDICATOR_FLAG);
			registry.register(REGEN_INDICATOR_FLAG);
		} catch (FlagConflictException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean checkStateFlag(Location location, Player player, StateFlag... stateFlags) {
		RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
		return query.testState(BukkitAdapter.adapt(location), player == null ? null : WorldGuardPlugin.inst().wrapPlayer(player), stateFlags);
	}

}
