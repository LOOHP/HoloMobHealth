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

package com.loohp.holomobhealth.nms;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.holders.IHoloMobArmorStand;
import com.loohp.holomobhealth.utils.BoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.v1_16_R2.AxisAlignedBB;
import net.minecraft.server.v1_16_R2.DataWatcher;
import net.minecraft.server.v1_16_R2.DataWatcherObject;
import net.minecraft.server.v1_16_R2.EntityArmorStand;
import net.minecraft.server.v1_16_R2.EntityTropicalFish;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import net.minecraft.server.v1_16_R2.IRegistry;
import net.minecraft.server.v1_16_R2.MathHelper;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_16_R2.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_16_R2.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class V1_16_2 extends NMSWrapper {

    private final Field[] entityMetadataPacketFields;
    private final Field entityCountField;
    private final Field dataWatcherByteField;
    private final Field dataWatcherCustomNameField;
    private final Field dataWatcherCustomNameVisibleField;
    private final Field dataWatcherSilentField;
    private final Field dataWatcherNoGravityField;
    private final Field[] spawnEntityLivingPacketFields;
    private final Field[] entityTeleportPacketFields;

    public V1_16_2() {
        try {
            entityMetadataPacketFields = PacketPlayOutEntityMetadata.class.getDeclaredFields();
            entityCountField = net.minecraft.server.v1_16_R2.Entity.class.getDeclaredField("entityCount");
            dataWatcherByteField = net.minecraft.server.v1_16_R2.Entity.class.getDeclaredField("S");
            dataWatcherCustomNameField = net.minecraft.server.v1_16_R2.Entity.class.getDeclaredField("aq");
            dataWatcherCustomNameVisibleField = net.minecraft.server.v1_16_R2.Entity.class.getDeclaredField("ar");
            dataWatcherSilentField = net.minecraft.server.v1_16_R2.Entity.class.getDeclaredField("as");
            dataWatcherNoGravityField = net.minecraft.server.v1_16_R2.Entity.class.getDeclaredField("at");
            spawnEntityLivingPacketFields = PacketPlayOutSpawnEntityLiving.class.getDeclaredFields();
            entityTeleportPacketFields = PacketPlayOutEntityTeleport.class.getDeclaredFields();
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
        try {
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
            entityTeleportPacketFields[0].setAccessible(true);
            entityTeleportPacketFields[1].setAccessible(true);
            entityTeleportPacketFields[2].setAccessible(true);
            entityTeleportPacketFields[3].setAccessible(true);
            entityTeleportPacketFields[4].setAccessible(true);
            entityTeleportPacketFields[5].setAccessible(true);
            entityTeleportPacketFields[6].setAccessible(true);

            entityTeleportPacketFields[0].setInt(packet, entityId);
            entityTeleportPacketFields[1].setDouble(packet, location.getX());
            entityTeleportPacketFields[2].setDouble(packet, location.getY());
            entityTeleportPacketFields[3].setDouble(packet, location.getZ());
            entityTeleportPacketFields[4].setByte(packet, (byte) (int) (location.getYaw() * 256.0F / 360.0F));
            entityTeleportPacketFields[5].setByte(packet, (byte) (int) (location.getPitch() * 256.0F / 360.0F));
            entityTeleportPacketFields[6].setBoolean(packet, false);

            return p(packet);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BoundingBox getBoundingBox(Entity entity) {
        org.bukkit.util.BoundingBox box = entity.getBoundingBox();
        return BoundingBox.of(box.getMin(), box.getMax());
    }

    @Override
    public Component getEntityCustomName(Entity entity) {
        IChatBaseComponent customName = ((CraftEntity) entity).getHandle().getCustomName();
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(customName));
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getEntityTranslationKey(Entity entity) {
        EntityTypes<?> type = EntityTypes.a(entity.getType().getName()).orElseThrow(() -> new RuntimeException());
        return type.f();
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
        return entity.getPassengers();
    }

    @Override
    public int getTropicalFishVariant(Entity entity) {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        EntityTropicalFish fish = (EntityTropicalFish) nmsEntity;
        return fish.getVariant();
    }

    @Override
    public UUID getEntityUUIDFromID(World world, int id) {
        try {
            WorldServer worldServer = ((CraftWorld) world).getHandle();
            net.minecraft.server.v1_16_R2.Entity entity = worldServer.entitiesById.get(id);
            return entity == null ? null : entity.getUniqueID();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Entity getEntityFromUUID(UUID uuid) {
        return Bukkit.getEntity(uuid);
    }

    @Override
    public double getEntityHeight(Entity entity) {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB axisAlignedBB = nmsEntity.getBoundingBox();
        double minY = axisAlignedBB.minY;
        double maxY = axisAlignedBB.maxY;
        return maxY - minY;
    }

    @Override
    public double getEntityWidth(Entity entity) {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB axisAlignedBB = nmsEntity.getBoundingBox();
        double minX = axisAlignedBB.minX;
        double maxX = axisAlignedBB.maxX;
        return maxX - minX;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityPacket(Entity entity) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            DataWatcher watcher = nmsEntity.getDataWatcher();

            Optional<IChatBaseComponent> name = watcher.get((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null));
            boolean visible = watcher.get((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return createEntityMetadataPacket(entity.getEntityId(), dataWatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityMetadataPacket(Entity entity, Component entityNameComponent, boolean visible) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.ofNullable(IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return createEntityMetadataPacket(entity.getEntityId(), dataWatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<?> buildDataWatchers(IHoloMobArmorStand entity, Component entityNameComponent, boolean visible) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = 0x20;
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.ofNullable(IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), true));

            byte standbitmask = 0x01 | 0x10;
            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.b, standbitmask));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public PacketContainer[] createArmorStandSpawnPackets(IHoloMobArmorStand entity, Component component, boolean visible) {
        try {
            EntityTypes<?> type = EntityTypes.a(entity.getType().getName()).orElseThrow(() -> new RuntimeException());
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

            spawnEntityLivingPacketFields[0].setInt(packet1, entity.getEntityId());
            spawnEntityLivingPacketFields[1].set(packet1, entity.getUniqueId());
            spawnEntityLivingPacketFields[2].setInt(packet1, IRegistry.ENTITY_TYPE.a(type));
            spawnEntityLivingPacketFields[3].setDouble(packet1, entity.getLocation().getX());
            spawnEntityLivingPacketFields[4].setDouble(packet1, entity.getLocation().getY());
            spawnEntityLivingPacketFields[5].setDouble(packet1, entity.getLocation().getZ());
            spawnEntityLivingPacketFields[6].setInt(packet1, 0);
            spawnEntityLivingPacketFields[7].setInt(packet1, 0);
            spawnEntityLivingPacketFields[8].setInt(packet1, 0);
            spawnEntityLivingPacketFields[9].setByte(packet1, (byte) ((int) (entity.getLocation().getYaw() * 256.0F / 360.0F)));
            spawnEntityLivingPacketFields[10].setByte(packet1, (byte) ((int) (entity.getLocation().getPitch() * 256.0F / 360.0F)));
            spawnEntityLivingPacketFields[11].setByte(packet1, (byte) ((int) (entity.getLocation().getYaw() * 256.0F / 360.0F)));

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

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer[] createSpawnDamageIndicatorPackets(int entityId, UUID uuid, Component entityNameComponent, Location location, Vector velocity, boolean gravity) {
        try {
            EntityTypes<EntityArmorStand> type = EntityTypes.ARMOR_STAND;

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

            spawnEntityLivingPacketFields[0].setInt(packet1, entityId);
            spawnEntityLivingPacketFields[1].set(packet1, uuid);
            spawnEntityLivingPacketFields[2].setInt(packet1, IRegistry.ENTITY_TYPE.a(type));
            spawnEntityLivingPacketFields[3].setDouble(packet1, location.getX());
            spawnEntityLivingPacketFields[4].setDouble(packet1, location.getY());
            spawnEntityLivingPacketFields[5].setDouble(packet1, location.getZ());
            spawnEntityLivingPacketFields[6].setInt(packet1, (int) (MathHelper.a(velocity.getX(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[7].setInt(packet1, (int) (MathHelper.a(velocity.getY(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[8].setInt(packet1, (int) (MathHelper.a(velocity.getZ(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[9].setByte(packet1, (byte) 0);
            spawnEntityLivingPacketFields[10].setByte(packet1, (byte) 0);
            spawnEntityLivingPacketFields[11].setByte(packet1, (byte) 0);

            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherSilentField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = 0x20;
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.ofNullable(IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), true));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherSilentField.get(null), true));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !gravity));

            byte standBitmask = (byte) 0x01 | 0x08 | 0x10;
            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.b, standBitmask));

            PacketContainer packet2 = createEntityMetadataPacket(entityId, dataWatcher);

            return new PacketContainer[] {p(packet1), packet2};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.Item<?>> readDataWatchersFromMetadataPacket(PacketContainer packet) {
        try {
            PacketPlayOutEntityMetadata nmsPacket = (PacketPlayOutEntityMetadata) packet.getHandle();
            entityMetadataPacketFields[1].setAccessible(true);
            return (List<DataWatcher.Item<?>>) entityMetadataPacketFields[1].get(nmsPacket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void addOrReplaceDataWatcher(List<DataWatcher.Item<?>> dataWatcher, DataWatcher.Item<T> newWatcher) {
        for (int i = 0; i < dataWatcher.size(); i++) {
            DataWatcher.Item<?> watcher = dataWatcher.get(i);
            if (newWatcher.a().a() == watcher.a().a() && newWatcher.b().equals(watcher.b())) {
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
            List<DataWatcher.Item<?>> dataWatcher = (List<DataWatcher.Item<?>>) dataWatchers;

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.ofNullable(IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            addOrReplaceDataWatcher(dataWatcher, new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            addOrReplaceDataWatcher(dataWatcher, new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));
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
