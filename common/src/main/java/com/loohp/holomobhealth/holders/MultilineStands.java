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

package com.loohp.holomobhealth.holders;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.protocol.ArmorStandPacket;
import com.loohp.holomobhealth.utils.EntityTypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class MultilineStands implements IMultilineStands {

    private static final Vector SPACING = new Vector(0, 0.27, 0);

    private final List<HoloMobArmorStand> stands;
    private final Location location;
    private final Entity entity;
    private final UUID uuid;
    private final int id;
    private int offset;
    private int gctask;

    public MultilineStands(Entity entity) {
        Location location = entity.getLocation().clone();
        offset = HoloMobHealth.armorStandYOffset;
        if (entity.getCustomName() != null) {
            for (Entry<String, Integer> entry : HoloMobHealth.specialNameOffset.entrySet()) {
                if (entity.getCustomName().matches(entry.getKey())) {
                    offset = offset + entry.getValue();
                    break;
                }
            }
        }
        for (Entry<EntityType, Integer> entry : HoloMobHealth.specialTypeOffset.entrySet()) {
            if (EntityTypeUtils.getEntityType(entity).equals(entry.getKey())) {
                offset = offset + entry.getValue();
                break;
            }
        }
        this.location = location.clone();
        Location standloc = location.clone();
        stands = new ArrayList<>();
        for (int i = 0; i < HoloMobHealth.displayText.size(); i++) {
            HoloMobArmorStand stand = new HoloMobArmorStand(standloc, EntityType.ARMOR_STAND, this);
            stands.add(stand);
        }
        Location base = entity.getLocation().add(0, NMS.getInstance().getEntityHeight(entity), 0);
        for (int i = 0; i < stands.size(); i++) {
            HoloMobArmorStand stand = stands.get(i);
            stand.setLocation(base.clone());
            base.add(SPACING);
        }
        Collections.reverse(stands);
        this.entity = entity;
        this.uuid = entity.getUniqueId();
        this.id = entity.getEntityId();
        gc();
    }

    private void gc() {
        gctask = Bukkit.getScheduler().runTaskTimerAsynchronously(HoloMobHealth.plugin, () -> {
            if (!entity.isValid() || !entity.getUniqueId().equals(uuid)) {
                remove();
            }
        }, 0, 15).getTaskId();
    }

    @Override
    public HoloMobArmorStand getStand(int line) {
        return stands.get(line);
    }

    @Override
    public List<HoloMobArmorStand> getStands() {
        return stands;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location location) {
        Location base = entity.getLocation().add(0, NMS.getInstance().getEntityHeight(entity), 0);
        for (int i = stands.size() - 1; i >= 0; i--) {
            HoloMobArmorStand stand = stands.get(i);
            stand.setLocation(base.clone());
            base.add(SPACING);
        }
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public int getEntityId() {
        return id;
    }

    @Override
    public void remove() {
        for (HoloMobArmorStand stand : stands) {
            Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> ArmorStandPacket.removeArmorStand(Bukkit.getOnlinePlayers(), stand, true, false));
        }
        Bukkit.getScheduler().cancelTask(gctask);
    }

}
