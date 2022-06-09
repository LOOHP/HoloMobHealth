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
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.holders.HoloMobArmorStand;
import com.loohp.holomobhealth.utils.LanguageUtils;
import com.loohp.holomobhealth.utils.MCVersion;
import com.loohp.holomobhealth.utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import java.util.Optional;
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

        PacketContainer packet1 = protocolManager.createPacket(HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_19) ? PacketType.Play.Server.SPAWN_ENTITY : PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packet1.getIntegers().write(0, entity.getEntityId());
        if (packet1.getUUIDs().size() > 0) {
            packet1.getUUIDs().write(0, entity.getUniqueId());
        }
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_19)) {
            packet1.getEntityTypeModifier().write(0, entity.getType());
            packet1.getIntegers().write(1, 0);
            packet1.getIntegers().write(2, 0);
            packet1.getIntegers().write(3, 0);
        } else {
            switch (HoloMobHealth.version) {
                case V1_18_2:
                case V1_18:
                case V1_17:
                    packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 71);
                    break;
                case V1_16_4:
                case V1_16_2:
                    packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 66);
                    break;
                case V1_16:
                    packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 65);
                    break;
                case V1_15:
                    packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 60);
                    break;
                case V1_14:
                    packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 59);
                    break;
                case V1_13_1:
                case V1_13:
                    packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 1 : 56);
                    break;
                default:
                    packet1.getIntegers().write(1, entity.getType().equals(EntityType.ARMOR_STAND) ? 30 : 101);
                    break;
            }
            packet1.getIntegers().write(2, 0);
            packet1.getIntegers().write(3, 0);
            packet1.getIntegers().write(4, 0);
        }
        packet1.getDoubles().write(0, entity.getLocation().getX());
        packet1.getDoubles().write(1, entity.getLocation().getY());
        packet1.getDoubles().write(2, entity.getLocation().getZ());
        packet1.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
        packet1.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
        packet1.getBytes().write(2, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));

        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet2.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = buildWrappedDataWatcher(entity, component, visible);
        packet2.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        for (Player player : playersInRange) {
            protocolManager.sendServerPacket(player, packet1);
            protocolManager.sendServerPacket(player, packet2);
        }
    }

    public static void updateArmorStand(Collection<? extends Player> players, HoloMobArmorStand entity, Component component, boolean visible) {
        if (players.isEmpty()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = entity.getWorld();
            List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());
            PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet1.getIntegers().write(0, entity.getEntityId());
            WrappedDataWatcher wpw = buildWrappedDataWatcher(entity, component, visible);
            packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

            PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            packet2.getIntegers().write(0, entity.getEntityId());
            packet2.getDoubles().write(0, entity.getLocation().getX());
            packet2.getDoubles().write(1, entity.getLocation().getY());
            packet2.getDoubles().write(2, entity.getLocation().getZ());
            packet2.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
            packet2.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));

            for (Player player : playersInRange) {
                protocolManager.sendServerPacket(player, packet1);
                protocolManager.sendServerPacket(player, packet2);
            }
        });
    }

    public static void updateArmorStand(Entity host, HoloMobArmorStand entity, Component component, boolean visible) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = entity.getWorld();
            List<Player> playersInRange = Bukkit.getOnlinePlayers().stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());

            PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet1.getIntegers().write(0, entity.getEntityId());
            WrappedDataWatcher wpw = buildWrappedDataWatcher(entity, component, visible);
            packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

            PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            packet2.getIntegers().write(0, entity.getEntityId());
            packet2.getDoubles().write(0, entity.getLocation().getX());
            packet2.getDoubles().write(1, entity.getLocation().getY());
            packet2.getDoubles().write(2, entity.getLocation().getZ());
            packet2.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
            packet2.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : playersInRange) {
                    protocolManager.sendServerPacket(player, packet1, false);
                    protocolManager.sendServerPacket(player, packet2, false);
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
            PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            packet1.getIntegers().write(0, entity.getEntityId());
            packet1.getDoubles().write(0, entity.getLocation().getX());
            packet1.getDoubles().write(1, entity.getLocation().getY());
            packet1.getDoubles().write(2, entity.getLocation().getZ());
            packet1.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
            packet1.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : playersInRange) {
                    protocolManager.sendServerPacket(player, packet1);
                }
            });
        });
    }

    public static void updateArmorStandLocation(Entity host, HoloMobArmorStand entity) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = entity.getWorld();
            List<Player> playersInRange = Bukkit.getOnlinePlayers().stream().filter(each -> (each.getWorld().equals(world)) && (each.getLocation().distanceSquared(entity.getLocation()) <= (HoloMobHealth.getUpdateRange(world) * HoloMobHealth.getUpdateRange(world)))).collect(Collectors.toList());

            PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            packet1.getIntegers().write(0, entity.getEntityId());
            packet1.getDoubles().write(0, entity.getLocation().getX());
            packet1.getDoubles().write(1, entity.getLocation().getY());
            packet1.getDoubles().write(2, entity.getLocation().getZ());
            packet1.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
            packet1.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : playersInRange) {
                    protocolManager.sendServerPacket(player, packet1, false);
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

            PacketContainer[] packets = PacketUtils.createEntityDestroyPacket(entity.getEntityId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : playersInRange) {
                    for (PacketContainer packet : packets) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            });
        });
    }

    private static WrappedDataWatcher buildWrappedDataWatcher(HoloMobArmorStand entity, Component entityNameComponent, boolean visible) {
        WrappedDataWatcher watcher = new WrappedDataWatcher();

        if (entity.getType().equals(EntityType.ARMOR_STAND)) {
            byte bitmask = 0x20;
            if (HoloMobHealth.version.isOld()) {
                watcher.setObject(0, bitmask);
            } else {
                watcher.setObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), bitmask);
            }

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
                watcher.setObject(5, (byte) 1);
            } else {
                watcher.setObject(new WrappedDataWatcherObject(3, Registry.get(Boolean.class)), entityNameComponent != null && (!entityNameComponent.equals(Component.empty()) && visible));
                watcher.setObject(new WrappedDataWatcherObject(5, Registry.get(Boolean.class)), true);
            }

            byte standbitmask = 0x01 | 0x10;

            if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
                watcher.setObject(new WrappedDataWatcherObject(15, Registry.get(Byte.class)), standbitmask);
            } else if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_15)) {
                watcher.setObject(new WrappedDataWatcherObject(14, Registry.get(Byte.class)), standbitmask);
            } else if (HoloMobHealth.version.equals(MCVersion.V1_14)) {
                watcher.setObject(new WrappedDataWatcherObject(13, Registry.get(Byte.class)), standbitmask);
            } else if (!HoloMobHealth.version.isOld()) {
                watcher.setObject(new WrappedDataWatcherObject(11, Registry.get(Byte.class)), standbitmask);
            } else {
                watcher.setObject(10, standbitmask);
            }
        } else {
            byte bitmask = 0x20;
            if (HoloMobHealth.version.isOld()) {
                watcher.setObject(0, bitmask);
            } else {
                watcher.setObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), bitmask);
            }

            if (!HoloMobHealth.version.isOld()) {
                watcher.setObject(new WrappedDataWatcherObject(4, Registry.get(Boolean.class)), true);
            } else {
                watcher.setObject(4, (byte) 1);
            }

            if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
                watcher.setObject(new WrappedDataWatcherObject(16, Registry.get(Boolean.class)), true);
            } else if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_15)) {
                watcher.setObject(new WrappedDataWatcherObject(15, Registry.get(Boolean.class)), true);
            } else if (HoloMobHealth.version.equals(MCVersion.V1_14)) {
                watcher.setObject(new WrappedDataWatcherObject(14, Registry.get(Boolean.class)), true);
            } else if (!HoloMobHealth.version.isOld()) {
                watcher.setObject(new WrappedDataWatcherObject(12, Registry.get(Boolean.class)), true);
            } else {
                watcher.setObject(10, (byte) 1);
            }
        }

        return watcher;
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
