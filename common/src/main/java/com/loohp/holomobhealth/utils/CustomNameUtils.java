/*
 * This file is part of HoloMobHealth2.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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
import com.loohp.holomobhealth.nms.NMS;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;

public class CustomNameUtils {

    public static Component getMobCustomName(Entity entity) {
        if (HoloMobHealth.mythicHook) {
            String customName = MythicMobsUtils.getMobCustomName(entity);
            Component vanillaCustomName = getMinecraftCustomName(entity);
            if (HoloMobHealth.useMythicMobCustomNamesFirst) {
                return customName == null || customName.isEmpty() ? vanillaCustomName : LegacyComponentSerializer.legacySection().deserialize(customName);
            } else {
                return vanillaCustomName == null || PlainTextComponentSerializer.plainText().serialize(vanillaCustomName).isEmpty() ? LegacyComponentSerializer.legacySection().deserialize(customName) : vanillaCustomName;
            }
        }
        if (HoloMobHealth.citizensHook) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            if (npc != null) {
                try {
                    return LegacyComponentSerializer.legacySection().deserialize(npc.getFullName());
                } catch (Exception ignore) {
                }
            }
        }
        if (HoloMobHealth.shopkeepersHook) {
            Shopkeeper keeper = ShopkeepersAPI.getShopkeeperRegistry().getShopkeeperByEntity(entity);
            if (keeper != null) {
                return LegacyComponentSerializer.legacySection().deserialize(keeper.getName());
            }
        }
        if (HoloMobHealth.myPetHook) {
            if (entity instanceof MyPetBukkitEntity) {
                MyPet mypet = ((MyPetBukkitEntity) entity).getMyPet();
                return LegacyComponentSerializer.legacySection().deserialize(mypet.getPetName());
            }
        }

        Component bukkitCustomName = getMinecraftCustomName(entity);
        if (bukkitCustomName == null || PlainTextComponentSerializer.plainText().serialize(bukkitCustomName).isEmpty()) {
            return null;
        }
        return bukkitCustomName;
    }

    private static Component getMinecraftCustomName(Entity entity) {
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_13)) {
            return NMS.getInstance().getEntityName(entity);
        } else {
            return NMS.getInstance().getEntityCustomName(entity);
        }
    }

}
