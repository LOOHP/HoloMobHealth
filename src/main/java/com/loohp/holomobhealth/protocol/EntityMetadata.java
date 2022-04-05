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

package com.loohp.holomobhealth.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.utils.EntityTypeUtils;
import com.loohp.holomobhealth.utils.LanguageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EntityMetadata {

    public static void updatePlayer(Player player) {
        List<Player> players = new ArrayList<>();
        players.add(player);
        int range = HoloMobHealth.getUpdateRange(player.getWorld());
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            updateEntity(players, entity);
        }
    }

    public static void updateEntity(Player player, Entity entity) {
        List<Player> players = new ArrayList<>();
        players.add(player);
        updateEntity(players, entity);
    }

    public static void updateEntity(Collection<? extends Player> players, Entity entity) {
        if (!EntityTypeUtils.getMobsTypesSet().contains(EntityTypeUtils.getEntityType(entity))) {
            return;
        }
        if (!entity.isValid()) {
            return;
        }
        PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher watcher = new WrappedDataWatcher(entity);
        WrappedDataWatcher entityWatcher = WrappedDataWatcher.getEntityWatcher(entity);
        if (!HoloMobHealth.version.isOld()) {
            WrappedWatchableObject watchableObject2 = entityWatcher.getWatchableObject(2);
            watcher.setObject(watchableObject2.getWatcherObject(), watchableObject2.getValue());
            WrappedWatchableObject watchableObject3 = entityWatcher.getWatchableObject(3);
            watcher.setObject(watchableObject3.getWatcherObject(), watchableObject3.getValue());
        } else if (entity instanceof LivingEntity) {
            String watchableObject2 = entityWatcher.getString(2);
            watcher.setObject(2, watchableObject2);
            byte watchableObject3 = entityWatcher.getByte(3);
            watcher.setObject(3, watchableObject3);
        }
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> {
            for (Player player : players) {
                if (player.getWorld().equals(entity.getWorld())) {
                    try {
                        HoloMobHealth.protocolManager.sendServerPacket(player, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void sendMetadataPacket(Entity entity, Component entityNameComponent, boolean visible, List<Player> players, boolean quiet) {
        Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> {

            PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);

            packet.getIntegers().write(0, entity.getEntityId()); //Set packet's entity id
            WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this

            if (entityNameComponent != null && !entityNameComponent.equals(Component.empty())) {
                if (HoloMobHealth.version.isOld()) {
                    watcher.setObject(2, LegacyComponentSerializer.legacySection().serialize(LanguageUtils.convert(entityNameComponent, HoloMobHealth.language)));
                } else if (HoloMobHealth.version.isLegacy()) {
                    Serializer serializer = Registry.get(String.class);
                    WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
                    watcher.setObject(object, LegacyComponentSerializer.legacySection().serialize(LanguageUtils.convert(entityNameComponent, HoloMobHealth.language)));
                } else {
                    Optional<?> opt = Optional.of(WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(entityNameComponent)).getHandle());
                    watcher.setObject(new WrappedDataWatcherObject(2, Registry.getChatComponentSerializer(true)), opt);
                }
            } else {
                if (HoloMobHealth.version.isOld()) {
                    watcher.setObject(2, "");
                } else if (HoloMobHealth.version.isLegacy()) {
                    Serializer serializer = Registry.get(String.class);
                    WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, serializer);
                    watcher.setObject(object, "");
                } else {
                    Optional<?> opt = Optional.empty();
                    watcher.setObject(new WrappedDataWatcherObject(2, Registry.getChatComponentSerializer(true)), opt);
                }
            }

            if (HoloMobHealth.version.isOld()) {
                watcher.setObject(3, (byte) (visible ? 1 : 0));
            } else {
                watcher.setObject(new WrappedDataWatcherObject(3, Registry.get(Boolean.class)), visible);
            }
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            try {
                for (Player player : players) {
                    if (player.hasPermission("holomobhealth.use")) {
                        HoloMobHealth.protocolManager.sendServerPacket(player, packet, !quiet);
                    }
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

}
