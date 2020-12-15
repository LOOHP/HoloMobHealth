package com.loohp.holomobhealth.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.bukkit.entity.Entity;

import com.loohp.holomobhealth.HoloMobHealth;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;

import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class CustomNameUtils {
	
	private static boolean legacyMythicMobs = false;
	private static Method legacyMythicMobsDisplayNameMethod;
	
	static {
		if (HoloMobHealth.MythicHook) {
			try {
				Class<?> clazz = Class.forName("io.lumine.xikage.mythicmobs.mobs.ActiveMob");
				clazz.getMethod("getDisplayName");
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
				legacyMythicMobs = true;
				try {
					Class<?> clazz = Class.forName("io.lumine.xikage.mythicmobs.mobs.MythicMob");
					legacyMythicMobsDisplayNameMethod = clazz.getMethod("getDisplayName");
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static String getMobCustomName(Entity entity) {
		if (HoloMobHealth.MythicHook) {
			Optional<ActiveMob> optmob = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId());
			if (optmob.isPresent()) {
				if (legacyMythicMobs) {
					try {
						return legacyMythicMobsDisplayNameMethod.invoke(optmob.get().getType()).toString();
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						e1.printStackTrace();
					}
				} else {
					return optmob.get().getDisplayName();
				}
			}
		}
		if (HoloMobHealth.CitizensHook) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
			if (npc != null) {
				try {
					return npc.getFullName();
				} catch (Exception ignore) {}
			}
		}
		if (HoloMobHealth.ShopkeepersHook) {
			Shopkeeper keeper = ShopkeepersAPI.getShopkeeperRegistry().getShopkeeperByEntity(entity);
			if (keeper != null) {
				return keeper.getName();
			}
		}
		if (HoloMobHealth.MyPetHook) {
			if (entity instanceof MyPetBukkitEntity) {
				MyPet mypet = ((MyPetBukkitEntity) entity).getMyPet();
				return mypet.getPetName();
			}
		}
		
		String bukkitcustomname = entity.getCustomName();
		if (bukkitcustomname == null || bukkitcustomname.equals("")) {
			return null;
		}
		return bukkitcustomname;
	}

}
