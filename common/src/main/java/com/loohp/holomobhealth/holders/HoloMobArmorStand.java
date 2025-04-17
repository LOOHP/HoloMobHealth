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

package com.loohp.holomobhealth.holders;

import com.loohp.holomobhealth.nms.NMSWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HoloMobArmorStand implements IHoloMobArmorStand {

    private final UUID uuid;
    private final EntityType type;
    private final transient Future<Integer> entityIdFuture;
    private final MultilineStands host;
    private int id;
    private Location location;

    @SuppressWarnings("deprecation")
    public HoloMobArmorStand(Location location, EntityType type, MultilineStands host) {
        this.entityIdFuture = NMSWrapper.getInstance().getNextEntityId();
        this.id = Integer.MIN_VALUE;
        this.uuid = UUID.randomUUID();
        this.location = location.clone();
        this.type = type;
        this.host = host;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public MultilineStands getHost() {
        return host;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public void teleport(Location location) {
        this.location = location.clone();
    }

    @Override
    public void teleport(World world, double x, double y, double z) {
        this.location = new Location(world, x, y, z, location.getYaw(), location.getPitch());
    }

    @Override
    public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
        this.location = new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public final int getEntityId() {
        if (id != Integer.MIN_VALUE) {
            return id;
        }
        try {
            return id = entityIdFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
