package com.loohp.holomobhealth.Utils;

import org.bukkit.entity.Entity;

import io.lumine.xikage.mythicmobs.MythicMobs;

public class MythicMobsUtils {
	public static boolean isMythicMob(Entity entity) {
		return MythicMobs.inst().getMobManager().isActiveMob(entity.getUniqueId());
	}
}
