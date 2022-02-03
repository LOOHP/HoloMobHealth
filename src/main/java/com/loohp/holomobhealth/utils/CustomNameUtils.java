package com.loohp.holomobhealth.utils;

import com.loohp.holomobhealth.HoloMobHealth;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.Optional;

public class CustomNameUtils {

    public static String getMobCustomName(Entity entity) {
        if (HoloMobHealth.mythicHook) {
            Optional<ActiveMob> optmob = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId());
            if (optmob.isPresent()) {
                try {
                    return optmob.get().getDisplayName();
                } catch (Throwable e) {
                    try {
                        Object type = optmob.get().getType();
                        Method method = type.getClass().getMethod("getDisplayName");
                        return method.invoke(type).toString();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        if (HoloMobHealth.citizensHook) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            if (npc != null) {
                try {
                    return npc.getFullName();
                } catch (Exception ignore) {
                }
            }
        }
        if (HoloMobHealth.shopkeepersHook) {
            Shopkeeper keeper = ShopkeepersAPI.getShopkeeperRegistry().getShopkeeperByEntity(entity);
            if (keeper != null) {
                return keeper.getName();
            }
        }
        if (HoloMobHealth.myPetHook) {
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
