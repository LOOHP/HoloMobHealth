package com.loohp.holomobhealth.Utils;

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
	
	public static String getMobCustomName(Entity entity) {
		if (HoloMobHealth.MythicHook) {
			Optional<ActiveMob> optmob = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId());
			if (optmob.isPresent()) {
				return optmob.get().getDisplayName();
			}
		}
		if (HoloMobHealth.CitizensHook) {
			NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
			if (npc != null) {
				return npc.getFullName();
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
