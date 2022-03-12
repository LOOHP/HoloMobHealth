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
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

public class CustomNameUtils {

    public static String getMobCustomName(Entity entity) {
        if (HoloMobHealth.mythicHook) {
            return MythicMobsUtils.getMobCustomName(entity);
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

        String bukkitCustomName = entity.getCustomName();
        if (bukkitCustomName == null || bukkitCustomName.isEmpty()) {
            return null;
        }
        return bukkitCustomName;
    }

}
