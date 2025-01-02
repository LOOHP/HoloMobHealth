/*
 * This file is part of HoloMobHealth.
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

package com.loohp.holomobhealth.protocol;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.utils.EntityTypeUtils;
import com.loohp.holomobhealth.utils.PacketSender;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        PacketContainer packet = NMS.getInstance().createUpdateEntityPacket(entity);

        Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> {
            for (Player player : players) {
                if (player.getWorld().equals(entity.getWorld())) {
                    try {
                        PacketSender.sendServerPacket(player, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void sendMetadataPacket(Entity entity, Component entityNameComponent, boolean visible, List<Player> players, boolean quiet) {
        Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> {

            PacketContainer packet = NMS.getInstance().createUpdateEntityMetadataPacket(entity, entityNameComponent, visible);

            for (Player player : players) {
                if (player.hasPermission("holomobhealth.use")) {
                    PacketSender.sendServerPacket(player, packet, !quiet);
                }
            }
        });
    }

}
