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

package com.loohp.holomobhealth.modules;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.protocol.EntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

public class RangeModule {

    private static final Queue<Player> updateQueue = new LinkedList<>();
    protected static int playersPerTick = 1;
    protected static int rate = 20;
    protected static double distance = 15;
    private static Map<Player, List<Entity>> upcomming = new HashMap<>();
    private static Map<Player, List<Entity>> current = new HashMap<>();

    public static void reloadNumbers() {
        rate = HoloMobHealth.getConfiguration().getInt("Options.Range.UpdateRate");
        distance = HoloMobHealth.getConfiguration().getDouble("Options.Range.Distance");
    }

    public static boolean isEntityInRangeOfPlayer(Player player, Entity entity) {
        List<Entity> nearby = current.get(player);
        return nearby != null && nearby.contains(entity);
    }

    public static void run() {
        for (World world : Bukkit.getWorlds()) {
            if (!HoloMobHealth.disabledWorlds.contains(world.getName())) {
                updateQueue.addAll(world.getPlayers());
            }
        }
        playersPerTick = updateQueue.size() / rate;
        Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> getEntitiesInRange(), 1);
    }

    private static void getEntitiesInRange() {
        int count = 0;
        while (!updateQueue.isEmpty()) {
            Player player = updateQueue.poll();
            upcomming.put(player, player.getNearbyEntities(distance, distance, distance));
            count++;
            if (count >= playersPerTick) {
                break;
            }
        }
        if (updateQueue.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> compareEntities());
        } else {
            Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> getEntitiesInRange(), 1);
        }
    }

    private static void compareEntities() {
        for (Entry<Player, List<Entity>> entry : upcomming.entrySet()) {
            Player player = entry.getKey();
            List<Entity> last = current.get(player);
            if (last == null) {
                List<Entity> now1 = new ArrayList<>(entry.getValue());
                Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
                    for (Entity entity : now1) {
                        EntityMetadata.updateEntity(player, entity);
                    }
                }, 1);
            } else {
                List<Entity> last1 = new ArrayList<>(last);
                List<Entity> now1 = new ArrayList<>(entry.getValue());
                List<Entity> now2 = new ArrayList<>(entry.getValue());
                now1.removeAll(last1);
                Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
                    for (Entity entity : now1) {
                        EntityMetadata.updateEntity(player, entity);
                    }
                }, 1);
                last1.removeAll(now2);
                Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
                    for (Entity entity : last1) {
                        EntityMetadata.updateEntity(player, entity);
                    }
                }, 1);
            }
        }
        Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
            current = upcomming;
            upcomming = new HashMap<>();
            Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> run(), 1);
        }, 1);
    }

}
