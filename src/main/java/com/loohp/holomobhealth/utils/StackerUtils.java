package com.loohp.holomobhealth.utils;

import com.loohp.holomobhealth.HoloMobHealth;
import org.bukkit.entity.Entity;

import java.util.UUID;

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
