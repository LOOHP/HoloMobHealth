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

package com.loohp.holomobhealth.holders;

import com.loohp.holomobhealth.utils.EntityUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HoloMobArmorStand {

    private final UUID uuid;
    private final EntityType type;
    private final transient Future<Integer> entityIdFuture;
    private int id;
    private Location location;

    public HoloMobArmorStand(Location location, EntityType type) {
        this.entityIdFuture = EntityUtils.getNextEntityId();
        this.id = Integer.MIN_VALUE;
        this.uuid = UUID.randomUUID();
        this.location = location.clone();
        this.type = type;
    }

    public EntityType getType() {
        return type;
    }

    public void setRotation(float yaw, float pitch) {
        teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
    }

    public World getWorld() {
        return location.getWorld();
    }

    public void teleport(Location location) {
        this.location = location.clone();
    }

    public void teleport(World world, double x, double y, double z) {
        this.location = new Location(world, x, y, z, location.getYaw(), location.getPitch());
    }

    public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
        this.location = new Location(world, x, y, z, yaw, pitch);
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        this.location = location.clone();
    }

    public UUID getUniqueId() {
        return uuid;
    }

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
