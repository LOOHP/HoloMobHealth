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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.holders.HoloMobArmorStand;
import com.loohp.holomobhealth.holders.MultilineStands;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.protocol.ArmorStandPacket;
import com.loohp.holomobhealth.protocol.EntityMetadata;
import com.loohp.holomobhealth.utils.ChatColorUtils;
import com.loohp.holomobhealth.utils.CitizensUtils;
import com.loohp.holomobhealth.utils.CustomNameUtils;
import com.loohp.holomobhealth.utils.DataWatcherUtils;
import com.loohp.holomobhealth.utils.EntityTypeUtils;
import com.loohp.holomobhealth.utils.MCVersion;
import com.loohp.holomobhealth.utils.MyPetUtils;
import com.loohp.holomobhealth.utils.MythicMobsUtils;
import com.loohp.holomobhealth.utils.ParsePlaceholders;
import com.loohp.holomobhealth.utils.RayTrace;
import com.loohp.holomobhealth.utils.ShopkeepersUtils;
import com.loohp.holomobhealth.utils.WorldGuardUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ArmorstandDisplay implements Listener {

    private static final UUID EMPTY_UUID = new UUID(0, 0);

    private static final Map<UUID, MultilineStands> mapping = new HashMap<>();
    private static final Map<Player, UUID> focusingEntities = new HashMap<>();

    public static void run() {
        Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
            if (HoloMobHealth.alwaysShow || !HoloMobHealth.armorStandMode) {
                return;
            }
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            int perTick = (int) Math.ceil((double) players.size() / 5);
            int current = 0;
            int count = 0;
            for (Player each : players) {
                UUID playerUUID = each.getUniqueId();
                if (count >= perTick) {
                    count = 0;
                    current++;
                }
                count++;
                Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player == null) {
                        return;
                    }
                    if (HoloMobHealth.disabledWorlds.contains(player.getWorld().getName())) {
                        return;
                    }
                    Entity entity = RayTrace.getLookingEntity(player, 6);
                    UUID last = focusingEntities.get(player);
                    if (entity != null) {
                        UUID now = entity.getUniqueId();
                        if (!now.equals(last)) {
                            focusingEntities.put(player, now);
                            EntityMetadata.updateEntity(player, entity);
                        }
                    } else {
                        if (last != null) {
                            focusingEntities.remove(player);
                            Entity lastEntity = NMS.getInstance().getEntityFromUUID(last);
                            if (lastEntity != null) {
                                EntityMetadata.updateEntity(player, lastEntity);
                            }
                        }
                    }
                }, current);
            }
        }, 0, 5);
    }

    public static void entityMetadataPacketListener() {
        Bukkit.getPluginManager().registerEvents(new ArmorstandDisplay(), HoloMobHealth.plugin);
        HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.ENTITY_METADATA) {
            @SuppressWarnings("deprecation")
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    if (!event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
                        return;
                    }

                    Player player = event.getPlayer();

                    PacketContainer packet = event.getPacket();

                    World world = player.getWorld();
                    int entityId = packet.getIntegers().read(0);

                    UUID entityUUID = NMS.getInstance().getEntityUUIDFromID(world, entityId);

                    if (entityUUID == null) {
                        return;
                    }

                    if (!player.hasPermission("holomobhealth.use") || !HoloMobHealth.playersEnabled.contains(player)) {
                        MultilineStands multi = mapping.get(entityUUID);
                        if (multi == null) {
                            return;
                        }
                        List<Player> players = new ArrayList<>();
                        players.add(player);
                        Map<HoloMobArmorStand, Boolean> list = ArmorStandPacket.playerStatus.get(player);
                        multi.getStands().forEach(each -> {
                            ArmorStandPacket.removeArmorStand(players, each, false, false);
                            if (list != null) {
                                list.remove(each);
                            }
                        });
                        return;
                    }

                    ArmorStandDisplayData data = getData(player, entityUUID, world, packet);

                    if (data != null) {
                        if (data.use()) {
                            DataWatcherUtils.writeMetadataPacket(packet, data.getWatcher());

                            Entity entity = data.getEntity();
                            String customName = data.getCustomName();

                            if (EntityTypeUtils.getMobsTypesSet().contains(EntityTypeUtils.getEntityType(entity))) {
                                if (entity.getPassenger() != null || isInvisible(entity) || (!HoloMobHealth.applyToNamed && customName != null) || (HoloMobHealth.useAlterHealth && !HoloMobHealth.idleUse && !HoloMobHealth.altShowHealth.containsKey(entity.getUniqueId())) || (HoloMobHealth.rangeEnabled && !RangeModule.isEntityInRangeOfPlayer(player, entity))) {
                                    Component name = customName != null && !customName.equals("") ? LegacyComponentSerializer.legacySection().deserialize(customName) : Component.empty();
                                    boolean visible = entity.isCustomNameVisible();
                                    EntityMetadata.sendMetadataPacket(entity, name, visible, Collections.singletonList(player), true);
                                    MultilineStands multi = mapping.remove(entity.getUniqueId());
                                    if (multi == null) {
                                        return;
                                    }
                                    multi.getStands().forEach(each -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
                                    multi.remove();
                                    return;
                                } else if (entity.isValid()) {
                                    MultilineStands multi = mapping.get(entity.getUniqueId());
                                    if (multi == null) {
                                        multi = new MultilineStands(entity);
                                        mapping.put(entity.getUniqueId(), multi);
                                        List<HoloMobArmorStand> stands = new ArrayList<>(multi.getStands());
                                        Collections.reverse(stands);
                                        for (HoloMobArmorStand stand : stands) {
                                            ArmorStandPacket.sendArmorStandSpawn(HoloMobHealth.playersEnabled, stand, Component.empty(), HoloMobHealth.alwaysShow);
                                        }
                                    } else {
                                        List<Player> players = new ArrayList<>();
                                        players.add(player);
                                        for (HoloMobArmorStand stand : multi.getStands()) {
                                            ArmorStandPacket.sendArmorStandSpawnIfNotAlready(players, stand, Component.empty(), HoloMobHealth.alwaysShow);
                                        }
                                    }
                                    UUID focusing = focusingEntities.getOrDefault(player, EMPTY_UUID);
                                    multi.setLocation(entity.getLocation());
                                    for (int i = 0; i < data.getComponents().size(); i++) {
                                        Component display = data.getComponents().get(i);
                                        ArmorStandPacket.updateArmorStand(entity, multi.getStand(i), display, HoloMobHealth.alwaysShow || focusing.equals(entityUUID));
                                    }
                                }
                            }
                        } else {
                            Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
                                MultilineStands multi = mapping.remove(entityUUID);
                                if (multi == null) {
                                    return;
                                }
                                Entity entity = data.getEntity();
                                Component name = NMS.getInstance().getEntityCustomName(entity);
                                boolean visible = entity.isCustomNameVisible();
                                EntityMetadata.sendMetadataPacket(entity, name, visible, Collections.singletonList(player), true);
                                multi.getStands().forEach(each -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
                                multi.remove();
                                EntityMetadata.sendMetadataPacket(entity, name, entity.isCustomNameVisible(), entity.getWorld().getPlayers(), true);
                            }, 1);
                        }
                    }
                } catch (UnsupportedOperationException e) {
                }
            }
        });
        if (HoloMobHealth.version.isOlderThan(MCVersion.V1_19)) {
            HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    try {
                        if (!event.getPacketType().equals(PacketType.Play.Server.SPAWN_ENTITY_LIVING)) {
                            return;
                        }
                        PacketContainer packet = event.getPacket();
                        Player player = event.getPlayer();
                        World world = player.getWorld();
                        int entityId = packet.getIntegers().read(0);
                        UUID entityUUID = NMS.getInstance().getEntityUUIDFromID(world, entityId);
                        if (entityUUID == null) {
                            return;
                        }
                        Entity entity = NMS.getInstance().getEntityFromUUID(entityUUID);
                        if (entity == null) {
                            return;
                        }
                        Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(player, entity), 5);
                    } catch (UnsupportedOperationException e) {
                    }
                }
            });
        }
        HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    if (!event.getPacketType().equals(PacketType.Play.Server.SPAWN_ENTITY)) {
                        return;
                    }
                    PacketContainer packet = event.getPacket();
                    Player player = event.getPlayer();
                    World world = player.getWorld();
                    int entityId = packet.getIntegers().read(0);
                    UUID entityUUID = NMS.getInstance().getEntityUUIDFromID(world, entityId);
                    if (entityUUID == null) {
                        return;
                    }
                    Entity entity = NMS.getInstance().getEntityFromUUID(entityUUID);
                    if (entity == null) {
                        return;
                    }
                    Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(player, entity), 5);
                } catch (UnsupportedOperationException e) {
                }
            }
        });
        HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.ENTITY_TELEPORT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    if (!event.getPacketType().equals(PacketType.Play.Server.ENTITY_TELEPORT)) {
                        return;
                    }
                    PacketContainer packet = event.getPacket();
                    Player player = event.getPlayer();
                    World world = player.getWorld();
                    int entityId = packet.getIntegers().read(0);
                    UUID entityUUID = NMS.getInstance().getEntityUUIDFromID(world, entityId);
                    if (entityUUID == null) {
                        return;
                    }
                    Entity entity = NMS.getInstance().getEntityFromUUID(entityUUID);
                    if (entity == null) {
                        return;
                    }
                    MultilineStands multi = mapping.get(entityUUID);
                    if (multi == null) {
                        return;
                    }
                    multi.setLocation(entity.getLocation());
                    multi.getStands().forEach(each -> ArmorStandPacket.updateArmorStandLocation(entity, each));
                } catch (UnsupportedOperationException e) {
                }
            }
        });
        HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.REL_ENTITY_MOVE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    if (!event.getPacketType().equals(PacketType.Play.Server.REL_ENTITY_MOVE)) {
                        return;
                    }
                    PacketContainer packet = event.getPacket();
                    Player player = event.getPlayer();
                    World world = player.getWorld();
                    int entityId = packet.getIntegers().read(0);
                    UUID entityUUID = NMS.getInstance().getEntityUUIDFromID(world, entityId);
                    if (entityUUID == null) {
                        return;
                    }
                    Entity entity = NMS.getInstance().getEntityFromUUID(entityUUID);
                    if (entity == null) {
                        return;
                    }
                    MultilineStands multi = mapping.get(entityUUID);
                    if (multi == null) {
                        return;
                    }
                    multi.setLocation(entity.getLocation());
                    multi.getStands().forEach(each -> ArmorStandPacket.updateArmorStandLocation(entity, each));
                } catch (UnsupportedOperationException e) {
                }
            }
        });
        HoloMobHealth.protocolManager.addPacketListener(new PacketAdapter(HoloMobHealth.plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    if (!event.getPacketType().equals(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK)) {
                        return;
                    }
                    PacketContainer packet = event.getPacket();
                    Player player = event.getPlayer();
                    World world = player.getWorld();
                    int entityId = packet.getIntegers().read(0);
                    UUID entityUUID = NMS.getInstance().getEntityUUIDFromID(world, entityId);
                    if (entityUUID == null) {
                        return;
                    }
                    Entity entity = NMS.getInstance().getEntityFromUUID(entityUUID);
                    if (entity == null) {
                        return;
                    }
                    MultilineStands multi = mapping.get(entityUUID);
                    if (multi == null) {
                        return;
                    }
                    multi.setLocation(entity.getLocation());
                    multi.getStands().forEach(each -> ArmorStandPacket.updateArmorStandLocation(entity, each));
                } catch (UnsupportedOperationException e) {
                }
            }
        });
    }

    public static ArmorStandDisplayData getData(Player player, UUID entityUUID, World world, PacketContainer packet) {
        //ArmorStandDisplayCache cahcedData = cache.get(entityUUID);
        //if (cahcedData != null) {
        //	return cahcedData;
        //}

        Entity entity = NMS.getInstance().getEntityFromUUID(entityUUID);

        if (entity == null || !EntityTypeUtils.getMobsTypesSet().contains(EntityTypeUtils.getEntityType(entity))) {
            return null;
        }

        if (HoloMobHealth.disabledMobTypes.contains(EntityTypeUtils.getEntityType(entity))) {
            return new ArmorStandDisplayData();
        }

        String customName = CustomNameUtils.getMobCustomName(entity);

        if (!HoloMobHealth.disabledWorlds.contains(world.getName())) {

            if (HoloMobHealth.worldGuardHook) {
                if (!WorldGuardUtils.checkStateFlag(entity.getLocation(), player, WorldGuardUtils.getHealthDisplayFlag())) {
                    return null;
                }
            }
            if (!HoloMobHealth.showCitizens && HoloMobHealth.citizensHook) {
                if (CitizensUtils.isNPC(entity)) {
                    return null;
                }
            }
            if (!HoloMobHealth.showMythicMobs && HoloMobHealth.mythicHook) {
                if (MythicMobsUtils.isMythicMob(entity)) {
                    return null;
                }
            }
            if (!HoloMobHealth.showShopkeepers && HoloMobHealth.shopkeepersHook) {
                if (ShopkeepersUtils.isShopkeeper(entity)) {
                    return null;
                }
            }
            if (!HoloMobHealth.showMyPet && HoloMobHealth.myPetHook) {
                if (MyPetUtils.isMyPet(entity)) {
                    return null;
                }
            }

            if (customName != null) {
                for (String each : HoloMobHealth.disabledMobNamesAbsolute) {
                    if (customName.equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
                        return new ArmorStandDisplayData();
                    }
                }
                for (String each : HoloMobHealth.disabledMobNamesContains) {
                    if (ChatColorUtils.stripColor(customName.toLowerCase()).contains(ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
                        return new ArmorStandDisplayData();
                    }
                }
            }

            if (!HoloMobHealth.applyToNamed) {
                if (customName != null) {
                    return null;
                }
            }

            boolean useIdle = false;
            if (HoloMobHealth.useAlterHealth && !HoloMobHealth.altShowHealth.containsKey(entityUUID)) {
                if (HoloMobHealth.idleUse) {
                    useIdle = true;
                }
            }

            WrappedDataWatcher watcher = DataWatcherUtils.fromMetadataPacket(packet);

            List<Component> components;
            if (useIdle) {
                components = HoloMobHealth.idleDisplayText.stream().map(each -> ParsePlaceholders.parse(player, (LivingEntity) entity, each)).collect(Collectors.toList());
            } else {
                components = HoloMobHealth.displayText.stream().map(each -> ParsePlaceholders.parse(player, (LivingEntity) entity, each)).collect(Collectors.toList());
            }

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

            if (HoloMobHealth.version.isOld()) {
                watcher.setObject(3, (byte) 0);
            } else {
                watcher.setObject(new WrappedDataWatcherObject(3, Registry.get(Boolean.class)), false);
            }

            ArmorStandDisplayData newData = new ArmorStandDisplayData(watcher, components, customName, entity);

            //cache.put(entityUUID, newData);
            //Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> cache.remove(entityUUID), 1);

            return newData;
        }
        return null;
    }

    public static boolean isInvisible(Entity entity) {
        if (entity instanceof LivingEntity) {
            return ((LivingEntity) entity).getPotionEffect(PotionEffectType.INVISIBILITY) != null;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getFrom().distanceSquared(event.getTo()) > 0) {
            Entity vehicle = event.getVehicle();
            List<Entity> passengers = NMS.getInstance().getPassengers(vehicle);
            if (!passengers.isEmpty()) {
                int range = HoloMobHealth.getUpdateRange(vehicle.getWorld());
                List<Player> nearby = vehicle.getNearbyEntities(range, range, range).stream().filter(each -> each instanceof Player).map(each -> (Player) each).collect(Collectors.toList());
                for (Entity passenger : passengers) {
                    Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> EntityMetadata.updateEntity(nearby, passenger));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangeDimension(EntityPortalEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        MultilineStands multi = mapping.remove(entity.getUniqueId());
        if (multi == null) {
            return;
        }
        multi.getStands().forEach(each -> ArmorStandPacket.removeArmorStand(HoloMobHealth.playersEnabled, each, true, false));
        multi.remove();

        Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
            EntityMetadata.updateEntity(HoloMobHealth.playersEnabled, entity);
        }, 2);
    }

    private static class ArmorStandDisplayData {

        private final boolean use;
        private WrappedDataWatcher watcher;
        private List<Component> components;
        private String customName;
        private Entity entity;

        public ArmorStandDisplayData(WrappedDataWatcher watcher, List<Component> components, String customName, Entity entity) {
            this.watcher = watcher;
            this.components = components;
            this.customName = customName;
            this.use = true;
            this.entity = entity;
        }

        public ArmorStandDisplayData() {
            this.use = false;
        }

        public WrappedDataWatcher getWatcher() {
            return watcher;
        }

        public List<Component> getComponents() {
            return components;
        }

        public String getCustomName() {
            return customName;
        }

        public boolean use() {
            return use;
        }

        public Entity getEntity() {
            return entity;
        }

    }

}
