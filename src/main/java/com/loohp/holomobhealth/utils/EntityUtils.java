package com.loohp.holomobhealth.utils;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Entity;

public class EntityUtils {
	
	private static boolean multiplePassenger;
	
	static {
		try {
			Entity.class.getMethod("getPassengers");
			multiplePassenger = true;
		} catch (NoSuchMethodException | SecurityException e) {
			multiplePassenger = false;
		}
	}
	
	public static List<Entity> getPassenger(Entity entity) {
		if (multiplePassenger) {
			return entity.getPassengers();
		} else {
			@SuppressWarnings("deprecation")
			Entity passenger = entity.getPassenger();
			return passenger == null ? Collections.emptyList() : Collections.singletonList(passenger);
		}
	}

}
