package com.loohp.holomobhealth.utils;

import org.bukkit.entity.Entity;

import net.citizensnpcs.api.CitizensAPI;

public class CitizensUtils {
	
	public static boolean isNPC(Entity entity) {
		return CitizensAPI.getNPCRegistry().isNPC(entity);
	}
	
}
