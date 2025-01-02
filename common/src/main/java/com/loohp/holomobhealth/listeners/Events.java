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

package com.loohp.holomobhealth.listeners;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.database.Database;
import com.loohp.holomobhealth.protocol.EntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> {
            if (!Database.playerExists(player)) {
                Database.createPlayer(player);
            }
            Database.loadPlayer(player);
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        HoloMobHealth.playersEnabled.remove(player);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity), 5);
    }

    @EventHandler
    public void onAttack(EntityDamageEvent event) {
        if (!HoloMobHealth.useAlterHealth) {
            return;
        }

        if (HoloMobHealth.disabledWorlds.contains(event.getEntity().getWorld().getName())) {
            return;
        }

        if (HoloMobHealth.altOnlyPlayer) {
            return;
        }

        Entity entity = event.getEntity();
        UUID uuid = entity.getUniqueId();
        HoloMobHealth.altShowHealth.put(uuid, System.currentTimeMillis() + HoloMobHealth.altHealthDisplayTime * 1000);
        EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity);
        Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
            Long timeout = HoloMobHealth.altShowHealth.get(uuid);
            if (timeout != null && System.currentTimeMillis() > timeout) {
                HoloMobHealth.altShowHealth.remove(uuid);
                EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity);
            }
        }, HoloMobHealth.altHealthDisplayTime * 20 + 5);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!HoloMobHealth.useAlterHealth) {
            return;
        }

        if (HoloMobHealth.disabledWorlds.contains(event.getEntity().getWorld().getName())) {
            return;
        }

        if (!HoloMobHealth.altOnlyPlayer) {
            return;
        }

        if (!event.getDamager().getType().equals(EntityType.PLAYER)) {
            if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() == null) {
                    return;
                } else {
                    if (!(projectile.getShooter() instanceof Player)) {
                        return;
                    }
                }
            } else {
                return;
            }
        }

        Entity entity = event.getEntity();
        UUID uuid = entity.getUniqueId();
        HoloMobHealth.altShowHealth.put(uuid, System.currentTimeMillis() + HoloMobHealth.altHealthDisplayTime * 1000);
        EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity);
        Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
            Long timeout = HoloMobHealth.altShowHealth.get(uuid);
            if (timeout != null && System.currentTimeMillis() > timeout) {
                HoloMobHealth.altShowHealth.remove(uuid);
                EntityMetadata.updateEntity(entity.getWorld().getPlayers(), entity);
            }
        }, HoloMobHealth.altHealthDisplayTime * 20 + 5);
    }

}
