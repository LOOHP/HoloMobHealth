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

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.holders.HoloMobArmorStand;
import com.loohp.holomobhealth.nms.NMS;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ArmorStandPacket implements Listener {

    private static final ProtocolManager protocolManager = HoloMobHealth.protocolManager;
    private static final Plugin plugin = HoloMobHealth.plugin;
    public static final Set<HoloMobArmorStand> active = Collections.synchronizedSet(new LinkedHashSet<>());
    public static final Map<Player, Map<HoloMobArmorStand, Boolean>> playerStatus = new ConcurrentHashMap<>();

    public static void update() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Map<HoloMobArmorStand, Boolean> activeList = playerStatus.get(player);
                if (activeList == null) {
                    continue;
                }

                List<Player> playerList = new LinkedList<>();
                playerList.add(player);

                Set<Entity> entitiesUpdated = new HashSet<>();

                for (Entry<HoloMobArmorStand, Boolean> entry : activeList.entrySet()) {
                    HoloMobArmorStand entity = entry.getKey();
                    World world = player.getWorld();
                    int range = HoloMobHealth.getUpdateRange(world);
                    range *= range;
                    if (entry.getValue()) {
                        if (!entity.getWorld().equals(world) || entity.getLocation().distanceSquared(player.getLocation()) > range + 1) {
                            removeArmorStand(playerList, entity, false, true);
                            entry.setValue(false);
                        }
                    } else {
                        if (entity.getWorld().equals(world) && entity.getLocation().distanceSquared(player.getLocation()) <= range) {
                            if (!HoloMobHealth.playersEnabled.contains(player)) {
                                continue;
                            }

                            sendArmorStandSpawn(playerList, entity, Component.empty(), false);
                            entitiesUpdated.add(entity.getHost().getEntity());
                            entry.setValue(true);
                        }
                    }
                }

                for (Entity entity : entitiesUpdated) {
                    Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(player, entity));
                }
            }
        }, 0, 20);
    }

    public static void sendArmorStandSpawnIfNotAlready(Collection<? extends Player> players, HoloMobArmorStand entity, Component component, boolean visible) {
        if (players.isEmpty()) {
            return;
        }
        List<Player> playersNotAlready = new ArrayList<>();
        for (Player player : players) {
            Map<HoloMobArmorStand, Boolean> list = playerStatus.get(player);
            if (list != null && !list.containsKey(entity)) {
                playersNotAlready.add(player);
            }
        }
        sendArmorStandSpawn(playersNotAlready, entity, component, visible);
    }

    public static void sendArmorStandSpawn(Collection<? extends Player> players, HoloMobArmorStand entity, Component component, boolean visible) {
        if (players.isEmpty()) {
            return;
        }
        active.add(entity);

        World world = entity.getWorld();
        List<Player> playersInRange = new ArrayList<>();
        for (Player each : players) {
            if ((each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))) {
                Map<HoloMobArmorStand, Boolean> list = playerStatus.get(each);
                if (list != null) {
                    list.put(entity, true);
                    playersInRange.add(each);
                }
            }
        }

        NMS.getInstance().createArmorStandSpawnPackets(entity, component, visible);

        PacketContainer[] packets = NMS.getInstance().createArmorStandSpawnPackets(entity, component, visible);

        for (Player player : playersInRange) {
            for (PacketContainer packet : packets) {
                protocolManager.sendServerPacket(player, packet);
            }
        }
    }

    public static void updateArmorStand(Collection<? extends Player> players, HoloMobArmorStand entity, Component component, boolean visible) {
        if (players.isEmpty()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = entity.getWorld();
            List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());

            PacketContainer[] packets = NMS.getInstance().createUpdateArmorStandPackets(entity, component, visible);

            for (Player player : playersInRange) {
                for (PacketContainer packet : packets) {
                    protocolManager.sendServerPacket(player, packet);
                }
            }
        });
    }

    public static void updateArmorStand(Entity host, HoloMobArmorStand entity, Component component, boolean visible) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = entity.getWorld();
            List<Player> playersInRange = Bukkit.getOnlinePlayers().stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());

            PacketContainer[] packets = NMS.getInstance().createUpdateArmorStandPackets(entity, component, visible);

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : playersInRange) {
                    for (PacketContainer packet : packets) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            });
        });
    }

    public static void updateArmorStandLocation(Collection<? extends Player> players, HoloMobArmorStand entity) {
        if (players.isEmpty()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = entity.getWorld();
            List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());

            PacketContainer[] packets = NMS.getInstance().createUpdateArmorStandLocationPackets(entity);

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : playersInRange) {
                    for (PacketContainer packet : packets) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            });
        });
    }

    public static void updateArmorStandLocation(Entity host, HoloMobArmorStand entity) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = entity.getWorld();
            List<Player> playersInRange = Bukkit.getOnlinePlayers().stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());

            PacketContainer[] packets = NMS.getInstance().createUpdateArmorStandLocationPackets(entity);

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : playersInRange) {
                    for (PacketContainer packet : packets) {
                        protocolManager.sendServerPacket(player, packet, false);
                    }
                }
            });
        });
    }

    public static void removeArmorStand(Collection<? extends Player> players, HoloMobArmorStand entity, boolean removeFromActive, boolean bypassFilter) {
        if (players.isEmpty()) {
            return;
        }
        if (removeFromActive) {
            active.remove(entity);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = entity.getWorld();
            List<Player> playersInRange;
            if (bypassFilter) {
                playersInRange = new ArrayList<>();
                for (Player each : players) {
                    if (removeFromActive) {
                        Map<HoloMobArmorStand, Boolean> list = playerStatus.get(each);
                        if (list != null) {
                            list.remove(entity);
                        }
                    }
                    playersInRange.add(each);
                }
            } else {
                playersInRange = new ArrayList<>();
                for (Player each : players) {
                    if ((each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))) {
                        if (removeFromActive) {
                            Map<HoloMobArmorStand, Boolean> list = playerStatus.get(each);
                            if (list != null) {
                                list.remove(entity);
                            }
                        }
                        playersInRange.add(each);
                    }
                }
            }

            PacketContainer[] packets = NMS.getInstance().createEntityDestroyPacket(entity.getEntityId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : playersInRange) {
                    for (PacketContainer packet : packets) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            });
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerStatus.put(event.getPlayer(), new ConcurrentHashMap<>());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        playerStatus.put(event.getPlayer(), new ConcurrentHashMap<>());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        playerStatus.remove(event.getPlayer());
    }

}
