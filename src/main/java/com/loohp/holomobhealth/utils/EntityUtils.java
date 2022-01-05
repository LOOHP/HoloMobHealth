package com.loohp.holomobhealth.utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import com.loohp.holomobhealth.HoloMobHealth;

public class EntityUtils {
	
	private static boolean multiplePassenger;
	private static Field entityCountField;
	
	static {
		try {
			Entity.class.getMethod("getPassengers");
			multiplePassenger = true;
		} catch (NoSuchMethodException | SecurityException e) {
			multiplePassenger = false;
		}
		try {
			Class<?> nmsEntityClass = NMSUtils.getNMSClass("net.minecraft.server.%s.Entity", "net.minecraft.world.entity.Entity");
			try {
				entityCountField = nmsEntityClass.getDeclaredField("entityCount");
			} catch (NoSuchFieldException | SecurityException e) {
				entityCountField = Stream.of(nmsEntityClass.getDeclaredFields()).filter(each -> each.getType().equals(AtomicInteger.class)).findFirst().get();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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
	
	public static Future<Integer> getNextEntityId() {
		try {
			entityCountField.setAccessible(true);
			Object entityCountObject = entityCountField.get(null);
			if (entityCountObject instanceof AtomicInteger) {
				return CompletableFuture.completedFuture(((AtomicInteger) entityCountObject).incrementAndGet());
			} else if (entityCountObject instanceof Integer) {
				if (Bukkit.isPrimaryThread()) {
					int value = (Integer) entityCountObject;
					try {
						entityCountField.set(null, value + 1);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
					return CompletableFuture.completedFuture(value);
				} else {
					return Bukkit.getScheduler().callSyncMethod(HoloMobHealth.plugin, () -> getNextEntityId().get());
				}
			} else {
				System.out.println(entityCountObject.getClass());
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return CompletableFuture.completedFuture(-1);
	}

}
