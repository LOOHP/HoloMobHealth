/*
 * This file is part of ImageFrame.
 *
 * Copyright (C) 2024. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2024. Contributors
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

package com.loohp.holomobhealth.nms;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.holders.IHoloMobArmorStand;
import com.loohp.holomobhealth.utils.BoundingBox;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public abstract class NMSWrapper {

    private static Plugin plugin;
    private static NMSWrapper instance;

    @Deprecated
    public static Plugin getPlugin() {
        return plugin;
    }

    @Deprecated
    public static NMSWrapper getInstance() {
        return instance;
    }

    @Deprecated
    public static void setup(NMSWrapper instance, Plugin plugin) {
        NMSWrapper.instance = instance;
        NMSWrapper.plugin = plugin;
    }

    static PacketContainer p(Object packet) {
        return PacketContainer.fromPacket(packet);
    }

    public abstract PacketContainer[] createEntityDestroyPacket(int... entityIds);

    public abstract PacketContainer createEntityMetadataPacket(int entityId, List<?> dataWatchers);

    public abstract PacketContainer createEntityTeleportPacket(int entityId, Location location);

    public abstract BoundingBox getBoundingBox(Entity entity);

    public abstract String getEntityTranslationKey(Entity entity);

    public abstract Component getEntityCustomName(Entity entity);

    public abstract Future<Integer> getNextEntityId();

    public abstract List<Entity> getPassengers(Entity entity);

    public abstract int getTropicalFishVariant(Entity entity);

    public abstract UUID getEntityUUIDFromID(World world, int id);

    public abstract Entity getEntityFromUUID(UUID uuid);

    public abstract double getEntityHeight(Entity entity);

    public abstract double getEntityWidth(Entity entity);

    public abstract PacketContainer createUpdateEntityPacket(Entity entity);

    public abstract PacketContainer createUpdateEntityMetadataPacket(Entity entity, Component entityNameComponent, boolean visible);

    public abstract List<?> buildDataWatchers(IHoloMobArmorStand entity, Component entityNameComponent, boolean visible);

    public abstract PacketContainer[] createArmorStandSpawnPackets(IHoloMobArmorStand entity, Component component, boolean visible);

    public abstract PacketContainer[] createUpdateArmorStandPackets(IHoloMobArmorStand entity, Component component, boolean visible);

    public abstract PacketContainer[] createUpdateArmorStandLocationPackets(IHoloMobArmorStand entity);

    public abstract PacketContainer[] createSpawnDamageIndicatorPackets(int entityId, UUID uuid, Component entityNameComponent, Location location, Vector velocity, boolean gravity);

    public abstract List<?> readDataWatchersFromMetadataPacket(PacketContainer packet);

    public abstract void modifyDataWatchers(List<?> dataWatchers, Component entityNameComponent, boolean visible);

    public abstract PacketContainer createModifiedMetadataPacket(PacketContainer packet, List<?> dataWatchers);

}
