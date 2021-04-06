package com.loohp.holomobhealth.utils;

import org.bukkit.entity.Entity;

import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;

public class MyPetUtils {
	
	public static boolean isMyPet(Entity entity) {
		return entity instanceof MyPetBukkitEntity;
	}

}
