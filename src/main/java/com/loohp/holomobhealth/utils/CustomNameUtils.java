/*
 * This file is part of HoloMobHealth.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
