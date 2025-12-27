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

package com.loohp.holomobhealth.modules;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.platform.PlatformPacketListenerPriority;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityMetadataPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityLivingPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityPacket;
import com.loohp.holomobhealth.protocol.EntityMetadata;
import com.loohp.holomobhealth.utils.ChatColorUtils;
import com.loohp.holomobhealth.utils.CitizensUtils;
import com.loohp.holomobhealth.utils.CustomNameUtils;
import com.loohp.holomobhealth.utils.EntityTypeUtils;
import com.loohp.holomobhealth.utils.MCVersion;
import com.loohp.holomobhealth.utils.MyPetUtils;
import com.loohp.holomobhealth.utils.MythicMobsUtils;
import com.loohp.holomobhealth.utils.ParsePlaceholders;
import com.loohp.holomobhealth.utils.ShopkeepersUtils;
import com.loohp.holomobhealth.utils.WorldGuardUtils;
import com.loohp.platformscheduler.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class NameTagDisplay {

    public static void entityMetadataPacketListener() {
        HoloMobHealth.protocolPlatform.getPlatformPacketListenerProvider().listenToPlayClientEntityMeta(HoloMobHealth.plugin, PlatformPacketListenerPriority.MONITOR, event -> {
            try {
                Player player = event.getPlayer();
                if (!player.hasPermission("holomobhealth.use") || !HoloMobHealth.playersEnabled.contains(player)) {
                    return;
                }
                PlatformPlayClientEntityMetadataPacket<?> packet = event.getPacket();
                World world = player.getWorld();
                int entityId = packet.getEntityId();
                Entity entity = NMS.getInstance().getEntityFromID(world, entityId);
                if (entity == null) {
                    return;
                }
                List<?> watcher = getWatcher(player, entity, world, packet);
                if (watcher != null) {
                    boolean readOnly = event.isReadOnly();
                    event.setReadOnly(false);
                    packet.setEntityDataWatchers(watcher);
                    event.setReadOnly(readOnly);
                }
            } catch (UnsupportedOperationException ignored) {
            }
        });
        if (HoloMobHealth.version.isOlderThan(MCVersion.V1_19)) {
            HoloMobHealth.protocolPlatform.getPlatformPacketListenerProvider().listenToPlayClientSpawnEntityLiving(HoloMobHealth.plugin, PlatformPacketListenerPriority.HIGHEST, event -> {
                try {
                    PlatformPlayClientSpawnEntityLivingPacket<?> packet = event.getPacket();
                    Player player = event.getPlayer();
                    World world = player.getWorld();
                    int entityId = packet.getEntityId();
                    Entity entity = NMS.getInstance().getEntityFromID(world, entityId);
                    if (entity == null) {
                        return;
                    }
                    Scheduler.runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(player, entity), 5);
                } catch (UnsupportedOperationException ignored) {
                }
            });
        }
        HoloMobHealth.protocolPlatform.getPlatformPacketListenerProvider().listenToPlayClientSpawnEntity(HoloMobHealth.plugin, PlatformPacketListenerPriority.HIGHEST, event -> {
            try {
                PlatformPlayClientSpawnEntityPacket<?> packet = event.getPacket();
                Player player = event.getPlayer();
                World world = player.getWorld();
                int entityId = packet.getEntityId();
                Entity entity = NMS.getInstance().getEntityFromID(world, entityId);
                if (entity == null) {
                    return;
                }
                Scheduler.runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(player, entity), 5);
            } catch (UnsupportedOperationException ignored) {
            }
        });
    }

    public static List<?> getWatcher(Player player, Entity entity, World world, PlatformPlayClientEntityMetadataPacket<?> packet) {
        if (entity == null || !EntityTypeUtils.getMobsTypesSet().contains(EntityTypeUtils.getEntityType(entity))) {
            return null;
        }

        if (HoloMobHealth.disabledMobTypes.contains(EntityTypeUtils.getEntityType(entity))) {
            return null;
        }

        Component customName = CustomNameUtils.getMobCustomName(entity);

        if (HoloMobHealth.rangeEnabled && !RangeModule.isEntityInRangeOfPlayer(player, entity)) {
            return null;
        }

        boolean useIdle = false;
        if (HoloMobHealth.useAlterHealth && !HoloMobHealth.altShowHealth.containsKey(entity.getUniqueId())) {
            if (!HoloMobHealth.idleUse) {
                return null;
            }
            useIdle = true;
        }

        if (!HoloMobHealth.disabledWorlds.contains(world.getName())) {

            if (HoloMobHealth.worldGuardHook) {
                if (!WorldGuardUtils.checkStateFlag(entity.getLocation(), player, WorldGuardUtils.getHealthDisplayFlag())) {
                    return null;
                }
            }
            if (!HoloMobHealth.showCitizens && HoloMobHealth.citizensHook) {
                if (CitizensUtils.isNPC(entity)) {
                    return null;
                }
            }
            if (!HoloMobHealth.showMythicMobs && HoloMobHealth.mythicHook) {
                if (MythicMobsUtils.isMythicMob(entity)) {
                    return null;
                }
            }
            if (!HoloMobHealth.showShopkeepers && HoloMobHealth.shopkeepersHook) {
                if (ShopkeepersUtils.isShopkeeper(entity)) {
                    return null;
                }
            }
            if (!HoloMobHealth.showMyPet && HoloMobHealth.myPetHook) {
                if (MyPetUtils.isMyPet(entity)) {
                    return null;
                }
            }

            if (customName != null) {
                for (String each : HoloMobHealth.disabledMobNamesAbsolute) {
                    if (LegacyComponentSerializer.legacySection().serialize(customName).equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
                        return null;
                    }
                }
                for (String each : HoloMobHealth.disabledMobNamesContains) {
                    if (PlainTextComponentSerializer.plainText().serialize(customName).toLowerCase().contains(ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
                        return null;
                    }
                }
            }

            if (!HoloMobHealth.applyToNamed) {
                if (customName != null) {
                    return null;
                }
            }

            List<?> watcher = packet.getEntityDataWatchers();

            Component component = ParsePlaceholders.parse(player, (LivingEntity) entity, useIdle ? HoloMobHealth.idleDisplayText.get(0) : HoloMobHealth.displayText.get(0));
            boolean visible = HoloMobHealth.alwaysShow;

            HoloMobHealth.protocolPlatform.getPlatformPacketCreatorProvider().modifyDataWatchers(watcher, component, visible);

            return watcher;
        }
        return null;
    }

}
