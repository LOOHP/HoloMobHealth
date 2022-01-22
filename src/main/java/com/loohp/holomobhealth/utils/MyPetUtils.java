package com.loohp.holomobhealth.utils;

import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import org.bukkit.entity.Entity;

public class MyPetUtils {

    public static boolean isMyPet(Entity entity) {
        return entity instanceof MyPetBukkitEntity;
    }

}
