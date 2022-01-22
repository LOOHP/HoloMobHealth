package com.loohp.holomobhealth.utils;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.Entity;

public class MythicMobsUtils {

    public static boolean isMythicMob(Entity entity) {
        return MythicMobs.inst().getMobManager().isActiveMob(entity.getUniqueId());
    }

}
