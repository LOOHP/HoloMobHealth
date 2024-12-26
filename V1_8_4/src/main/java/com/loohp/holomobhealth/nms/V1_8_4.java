/*
 * This file is part of HoloMobHealth.
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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class V1_8_4 extends NMSWrapper {

    private final Field[] entityMetadataPacketFields;
    private final Field entityCountField;
    private final Field dataWatcherTypeMapField;
    private final int dataWatcherByteIndex;
    private final int dataWatcherCustomNameIndex;
    private final int dataWatcherCustomNameVisibleIndex;
    private final int dataWatcherArmorStandByteIndex;
    private final Field[] spawnEntityLivingPacketFields;

    public V1_8_4() {
        try {
            entityMetadataPacketFields = PacketPlayOutEntityMetadata.class.getDeclaredFields();
            entityCountField = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("entityCount");
            dataWatcherTypeMapField = DataWatcher.class.getDeclaredField("c");
            dataWatcherByteIndex = 0;
            dataWatcherCustomNameIndex = 2;
            dataWatcherCustomNameVisibleIndex = 3;
            dataWatcherArmorStandByteIndex = 10;
            spawnEntityLivingPacketFields = PacketPlayOutSpawnEntityLiving.class.getDeclaredFields();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer[] createEntityDestroyPacket(int... entityIds) {
        return new PacketContainer[] {p(new PacketPlayOutEntityDestroy(entityIds))};
    }

    @Override
    public PacketContainer createEntityMetadataPacket(int entityId, List<?> dataWatchers) {
        try {
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
            entityMetadataPacketFields[0].setAccessible(true);
            entityMetadataPacketFields[0].setInt(packet, entityId);
            entityMetadataPacketFields[1].setAccessible(true);
            entityMetadataPacketFields[1].set(packet, dataWatchers);
            return p(packet);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer createEntityTeleportPacket(int entityId, Location location) {
        return p(new PacketPlayOutEntityTeleport(entityId, MathHelper.floor(location.getX() * 32.0), MathHelper.floor(location.getY() * 32.0), MathHelper.floor(location.getZ() * 32.0), (byte) (int) (location.getYaw() * 256.0F / 360.0F), (byte) (int) (location.getPitch() * 256.0F / 360.0F), false));
    }

    @Override
    public BoundingBox getBoundingBox(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB axisAlignedBB = nmsEntity.getBoundingBox();
        return new BoundingBox(axisAlignedBB.a, axisAlignedBB.b, axisAlignedBB.c, axisAlignedBB.d, axisAlignedBB.e, axisAlignedBB.f);
    }

    @Override
    public String getEntityTranslationKey(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        return "entity." + EntityTypes.b(nmsEntity) + ".name";
    }

    @Override
    public Component getEntityCustomName(Entity entity) {
        String customName = ((CraftEntity) entity).getHandle().getCustomName();
        return LegacyComponentSerializer.legacySection().deserialize(customName);
    }

    @Override
    public Future<Integer> getNextEntityId() {
        try {
            entityCountField.setAccessible(true);
            AtomicInteger counter = (AtomicInteger) entityCountField.get(null);
            return CompletableFuture.completedFuture(counter.incrementAndGet());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Entity> getPassengers(Entity entity) {
        Entity passenger = entity.getPassenger();
        return passenger == null ? Collections.emptyList() : Collections.singletonList(passenger);
    }

    @Override
    public int getTropicalFishVariant(Entity entity) {
        return -1;
    }

    @Override
    public UUID getEntityUUIDFromID(World world, int id) {
        try {
            WorldServer worldServer = ((CraftWorld) world).getHandle();
            net.minecraft.server.v1_8_R3.Entity entity = worldServer.a(id);
            return entity == null ? null : entity.getUniqueID();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Entity getEntityFromUUID(UUID uuid) {
        try {
            for (World world : Bukkit.getWorlds()) {
                WorldServer worldServer = ((CraftWorld) world).getHandle();
                net.minecraft.server.v1_8_R3.Entity entity = worldServer.getEntity(uuid);
                if (entity != null) {
                    return entity.getBukkitEntity();
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getEntityHeight(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB axisAlignedBB = nmsEntity.getBoundingBox();
        double minY = axisAlignedBB.b;
        double maxY = axisAlignedBB.e;
        return maxY - minY;
    }

    @Override
    public double getEntityWidth(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB axisAlignedBB = nmsEntity.getBoundingBox();
        double minX = axisAlignedBB.a;
        double maxX = axisAlignedBB.d;
        return maxX - minX;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityPacket(Entity entity) {
        try {
            List<DataWatcher.WatchableObject> dataWatcher = new ArrayList<>();

            dataWatcherTypeMapField.setAccessible(true);
            Map<Class<?>, Integer> typeMap = (Map<Class<?>, Integer>) dataWatcherTypeMapField.get(null);

            net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            DataWatcher watcher = nmsEntity.getDataWatcher();
            
            String name = watcher.getString(dataWatcherCustomNameIndex);
            Byte visible = watcher.getByte(dataWatcherCustomNameVisibleIndex);

            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(String.class), dataWatcherCustomNameIndex, name));
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherCustomNameVisibleIndex, visible));

            return createEntityMetadataPacket(entity.getEntityId(), dataWatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityMetadataPacket(Entity entity, Component entityNameComponent, boolean visible) {
        try {
            List<DataWatcher.WatchableObject> dataWatcher = new ArrayList<>();

            dataWatcherTypeMapField.setAccessible(true);
            Map<Class<?>, Integer> typeMap = (Map<Class<?>, Integer>) dataWatcherTypeMapField.get(null);

            String name = entityNameComponent == null ? "" : LegacyComponentSerializer.legacySection().serialize(entityNameComponent);
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(String.class), dataWatcherCustomNameIndex, name));
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherCustomNameVisibleIndex, visible));

            return createEntityMetadataPacket(entity.getEntityId(), dataWatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<?> buildDataWatchers(IHoloMobArmorStand entity, Component entityNameComponent, boolean visible) {
        try {
            List<DataWatcher.WatchableObject> dataWatcher = new ArrayList<>();

            dataWatcherTypeMapField.setAccessible(true);
            Map<Class<?>, Integer> typeMap = (Map<Class<?>, Integer>) dataWatcherTypeMapField.get(null);

            byte bitmask = 0x20;
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherByteIndex, bitmask));

            String name = entityNameComponent == null ? "" : LegacyComponentSerializer.legacySection().serialize(entityNameComponent);
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(String.class), dataWatcherCustomNameIndex, name));
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherCustomNameVisibleIndex, visible));

            byte standbitmask = 0x01 | 0x10;
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherArmorStandByteIndex, standbitmask));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public PacketContainer[] createArmorStandSpawnPackets(IHoloMobArmorStand entity, Component component, boolean visible) {
        try {
            PacketPlayOutSpawnEntityLiving packet1 = new PacketPlayOutSpawnEntityLiving();
            spawnEntityLivingPacketFields[0].setAccessible(true);
            spawnEntityLivingPacketFields[1].setAccessible(true);
            spawnEntityLivingPacketFields[2].setAccessible(true);
            spawnEntityLivingPacketFields[3].setAccessible(true);
            spawnEntityLivingPacketFields[4].setAccessible(true);
            spawnEntityLivingPacketFields[5].setAccessible(true);
            spawnEntityLivingPacketFields[6].setAccessible(true);
            spawnEntityLivingPacketFields[7].setAccessible(true);
            spawnEntityLivingPacketFields[8].setAccessible(true);
            spawnEntityLivingPacketFields[9].setAccessible(true);
            spawnEntityLivingPacketFields[10].setAccessible(true);
            spawnEntityLivingPacketFields[11].setAccessible(true);
            spawnEntityLivingPacketFields[12].setAccessible(true);

            spawnEntityLivingPacketFields[0].setInt(packet1, entity.getEntityId());
            spawnEntityLivingPacketFields[1].setInt(packet1, entity.getType().getTypeId());
            spawnEntityLivingPacketFields[2].setDouble(packet1, entity.getLocation().getX());
            spawnEntityLivingPacketFields[3].setDouble(packet1, entity.getLocation().getY());
            spawnEntityLivingPacketFields[4].setDouble(packet1, entity.getLocation().getZ());
            spawnEntityLivingPacketFields[5].setInt(packet1, 0);
            spawnEntityLivingPacketFields[6].setInt(packet1, 0);
            spawnEntityLivingPacketFields[7].setInt(packet1, 0);
            spawnEntityLivingPacketFields[8].setByte(packet1, (byte) ((int) (entity.getLocation().getYaw() * 256.0F / 360.0F)));
            spawnEntityLivingPacketFields[9].setByte(packet1, (byte) ((int) (entity.getLocation().getPitch() * 256.0F / 360.0F)));
            spawnEntityLivingPacketFields[10].setByte(packet1, (byte) ((int) (entity.getLocation().getYaw() * 256.0F / 360.0F)));
            spawnEntityLivingPacketFields[11].set(packet1, new DataWatcher(null));
            spawnEntityLivingPacketFields[12].set(packet1, Collections.emptyList());

            List<?> dataWatchers = buildDataWatchers(entity, component, visible);
            PacketContainer packet2 = createEntityMetadataPacket(entity.getEntityId(), dataWatchers);

            return new PacketContainer[] {p(packet1), packet2};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer[] createUpdateArmorStandPackets(IHoloMobArmorStand entity, Component component, boolean visible) {
        List<?> dataWatchers = buildDataWatchers(entity, component, visible);
        PacketContainer packet1 = createEntityMetadataPacket(entity.getEntityId(), dataWatchers);
        PacketContainer packet2 = createEntityTeleportPacket(entity.getEntityId(), entity.getLocation());
        return new PacketContainer[] {packet1, packet2};
    }

    @Override
    public PacketContainer[] createUpdateArmorStandLocationPackets(IHoloMobArmorStand entity) {
        return new PacketContainer[] {createEntityTeleportPacket(entity.getEntityId(), entity.getLocation())};
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public PacketContainer[] createSpawnDamageIndicatorPackets(int entityId, UUID uuid, Component entityNameComponent, Location location, Vector velocity, boolean gravity) {
        try {
            PacketPlayOutSpawnEntityLiving packet1 = new PacketPlayOutSpawnEntityLiving();
            spawnEntityLivingPacketFields[0].setAccessible(true);
            spawnEntityLivingPacketFields[1].setAccessible(true);
            spawnEntityLivingPacketFields[2].setAccessible(true);
            spawnEntityLivingPacketFields[3].setAccessible(true);
            spawnEntityLivingPacketFields[4].setAccessible(true);
            spawnEntityLivingPacketFields[5].setAccessible(true);
            spawnEntityLivingPacketFields[6].setAccessible(true);
            spawnEntityLivingPacketFields[7].setAccessible(true);
            spawnEntityLivingPacketFields[8].setAccessible(true);
            spawnEntityLivingPacketFields[9].setAccessible(true);
            spawnEntityLivingPacketFields[10].setAccessible(true);
            spawnEntityLivingPacketFields[11].setAccessible(true);
            spawnEntityLivingPacketFields[12].setAccessible(true);

            spawnEntityLivingPacketFields[0].setInt(packet1, entityId);
            spawnEntityLivingPacketFields[1].setInt(packet1, EntityType.ARMOR_STAND.getTypeId());
            spawnEntityLivingPacketFields[2].setDouble(packet1, location.getX());
            spawnEntityLivingPacketFields[3].setDouble(packet1, location.getY());
            spawnEntityLivingPacketFields[4].setDouble(packet1, location.getZ());
            spawnEntityLivingPacketFields[5].setInt(packet1, (int) (MathHelper.a(velocity.getX(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[6].setInt(packet1, (int) (MathHelper.a(velocity.getY(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[7].setInt(packet1, (int) (MathHelper.a(velocity.getZ(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[8].setByte(packet1, (byte) 0);
            spawnEntityLivingPacketFields[9].setByte(packet1, (byte) 0);
            spawnEntityLivingPacketFields[10].setByte(packet1, (byte) 0);
            spawnEntityLivingPacketFields[11].set(packet1, new DataWatcher(null));
            spawnEntityLivingPacketFields[12].set(packet1, Collections.emptyList());

            List<DataWatcher.WatchableObject> dataWatcher = new ArrayList<>();

            dataWatcherTypeMapField.setAccessible(true);
            Map<Class<?>, Integer> typeMap = (Map<Class<?>, Integer>) dataWatcherTypeMapField.get(null);

            byte bitmask = 0x20;
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherByteIndex, bitmask));

            String name = entityNameComponent == null ? "" : LegacyComponentSerializer.legacySection().serialize(entityNameComponent);
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(String.class), dataWatcherCustomNameIndex, name));
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherCustomNameVisibleIndex, (byte) 1));

            byte standBitmask = (byte) 0x01 | 0x08 | 0x10;
            dataWatcher.add(new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherArmorStandByteIndex, standBitmask));

            PacketContainer packet2 = createEntityMetadataPacket(entityId, dataWatcher);

            return new PacketContainer[] {p(packet1), packet2};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.WatchableObject> readDataWatchersFromMetadataPacket(PacketContainer packet) {
        try {
            PacketPlayOutEntityMetadata nmsPacket = (PacketPlayOutEntityMetadata) packet.getHandle();
            entityMetadataPacketFields[1].setAccessible(true);
            return (List<DataWatcher.WatchableObject>) entityMetadataPacketFields[1].get(nmsPacket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void addOrReplaceDataWatcher(List<DataWatcher.WatchableObject> dataWatcher, DataWatcher.WatchableObject newWatcher) {
        for (int i = 0; i < dataWatcher.size(); i++) {
            DataWatcher.WatchableObject watcher = dataWatcher.get(i);
            if (newWatcher.a() == watcher.a() && newWatcher.c() == watcher.c()) {
                dataWatcher.set(i, newWatcher);
                return;
            }
        }
        dataWatcher.add(newWatcher);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void modifyDataWatchers(List<?> dataWatchers, Component entityNameComponent, boolean visible) {
        try {
            List<DataWatcher.WatchableObject> dataWatcher = (List<DataWatcher.WatchableObject>) dataWatchers;

            dataWatcherTypeMapField.setAccessible(true);
            Map<Class<?>, Integer> typeMap = (Map<Class<?>, Integer>) dataWatcherTypeMapField.get(null);

            String name = entityNameComponent == null ? "" : LegacyComponentSerializer.legacySection().serialize(entityNameComponent);
            addOrReplaceDataWatcher(dataWatcher, new DataWatcher.WatchableObject(typeMap.get(String.class), dataWatcherCustomNameIndex, name));
            addOrReplaceDataWatcher(dataWatcher, new DataWatcher.WatchableObject(typeMap.get(Byte.class), dataWatcherCustomNameVisibleIndex, (byte) 1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer createModifiedMetadataPacket(PacketContainer packet, List<?> dataWatchers) {
        try {
            PacketPlayOutEntityMetadata nmsPacket = (PacketPlayOutEntityMetadata) packet.getHandle();
            entityMetadataPacketFields[0].setAccessible(true);
            int id = entityMetadataPacketFields[0].getInt(nmsPacket);
            return createEntityMetadataPacket(id, dataWatchers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
