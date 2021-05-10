package com.loohp.holomobhealth.utils;

import java.util.UUID;

import org.bukkit.entity.Entity;

import com.loohp.holomobhealth.HoloMobHealth;

public class StackerUtils {
	
	public static Entity getEntityFromStack(UUID uuid) {
		if (HoloMobHealth.roseStackerHook) {
			dev.rosewood.rosestacker.stack.StackedEntity stack = dev.rosewood.rosestacker.api.RoseStackerAPI.getInstance().getStackedEntities().get(uuid);
			if (stack != null) {
				return stack.getEntity();
			}
		}
		if (HoloMobHealth.ultimateStackerHook) {
			com.songoda.ultimatestacker.stackable.entity.EntityStack stack = com.songoda.ultimatestacker.UltimateStacker.getInstance().getEntityStackManager().getStack(uuid);
			if (stack != null) {
				return stack.getHostEntity();
			}
		}
		return null;
	}

}
