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
import com.loohp.holomobhealth.utils.UnsafeAccessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.EntityTropicalFish;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class V1_19 extends NMSWrapper {

    private final Field[] entityMetadataPacketFields;
    private final Field entityCountField;
    private final Field dataWatcherByteField;
    private final Field dataWatcherCustomNameField;
    private final Field dataWatcherCustomNameVisibleField;
    private final Field dataWatcherSilentField;
    private final Field dataWatcherNoGravityField;
    private final Field[] entityTeleportPacketFields;

    //paper
    private Method worldServerEntityLookup;

    public V1_19() {
        try {
            entityMetadataPacketFields = PacketPlayOutEntityMetadata.class.getDeclaredFields();
            entityCountField = net.minecraft.world.entity.Entity.class.getDeclaredField("c");
            dataWatcherByteField = net.minecraft.world.entity.Entity.class.getDeclaredField("Z");
            dataWatcherCustomNameField = net.minecraft.world.entity.Entity.class.getDeclaredField("aM");
            dataWatcherCustomNameVisibleField = net.minecraft.world.entity.Entity.class.getDeclaredField("aN");
            dataWatcherSilentField = net.minecraft.world.entity.Entity.class.getDeclaredField("aO");
            dataWatcherNoGravityField = net.minecraft.world.entity.Entity.class.getDeclaredField("aP");
            entityTeleportPacketFields = PacketPlayOutEntityTeleport.class.getDeclaredFields();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            //paper
            //noinspection JavaReflectionMemberAccess
            worldServerEntityLookup = WorldServer.class.getMethod("getEntityLookup");
        } catch (NoSuchMethodException ignore) {
        }
    }

    @Override
    public PacketContainer[] createEntityDestroyPacket(int... entityIds) {
        return new PacketContainer[] {p(new PacketPlayOutEntityDestroy(entityIds))};
    }
    
    @Override
    public PacketContainer createEntityMetadataPacket(int entityId, List<?> dataWatchers) {
        try {
            PacketPlayOutEntityMetadata packet = (PacketPlayOutEntityMetadata) UnsafeAccessor.getUnsafe().allocateInstance(PacketPlayOutEntityMetadata.class);
            entityMetadataPacketFields[0].setAccessible(true);
            entityMetadataPacketFields[0].setInt(packet, entityId);
            entityMetadataPacketFields[1].setAccessible(true);
            entityMetadataPacketFields[1].set(packet, dataWatchers);
            return p(packet);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer createEntityTeleportPacket(int entityId, Location location) {
        try {
            PacketPlayOutEntityTeleport packet = (PacketPlayOutEntityTeleport) UnsafeAccessor.getUnsafe().allocateInstance(PacketPlayOutEntityTeleport.class);
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
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BoundingBox getBoundingBox(Entity entity) {
        org.bukkit.util.BoundingBox box = entity.getBoundingBox();
        return BoundingBox.of(box.getMin(), box.getMax());
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getEntityTranslationKey(Entity entity) {
        EntityTypes<?> type = EntityTypes.a(entity.getType().getName()).orElseThrow(() -> new RuntimeException());
        return type.g();
    }

    @Override
    public Component getEntityCustomName(Entity entity) {
        IChatBaseComponent customName = ((CraftEntity) entity).getHandle().Z();
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(customName));
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
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        EntityTropicalFish fish = (EntityTropicalFish) nmsEntity;
        return fish.fS();
    }

    @SuppressWarnings("unchecked")
    @Override
    public UUID getEntityUUIDFromID(World world, int id) {
        try {
            WorldServer worldServer = ((CraftWorld) world).getHandle();
            LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter;
            if (worldServerEntityLookup == null) {
                levelEntityGetter = worldServer.P.d();
            } else {
                levelEntityGetter = (LevelEntityGetter<net.minecraft.world.entity.Entity>) worldServerEntityLookup.invoke(worldServer);
            }
            net.minecraft.world.entity.Entity entity = levelEntityGetter.a(id);
            return entity == null ? null : entity.co();
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
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB axisAlignedBB = nmsEntity.cz();
        double minY = axisAlignedBB.b;
        double maxY = axisAlignedBB.e;
        return maxY - minY;
    }

    @Override
    public double getEntityWidth(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB axisAlignedBB = nmsEntity.cz();
        double minX = axisAlignedBB.a;
        double maxX = axisAlignedBB.d;
        return maxX - minX;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityPacket(Entity entity) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            DataWatcher watcher = nmsEntity.ai();

            Optional<IChatBaseComponent> name = watcher.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null));
            boolean visible = watcher.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null));

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

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
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

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), true));

            byte standbitmask = 0x01 | 0x10;
            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.bG, standbitmask));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public PacketContainer[] createArmorStandSpawnPackets(IHoloMobArmorStand entity, Component component, boolean visible) {
        EntityTypes<?> type = EntityTypes.a(entity.getType().getName()).orElseThrow(() -> new RuntimeException());
        Vec3D velocity = Vec3D.b;
        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, 0, velocity, entity.getLocation().getYaw());

        List<?> dataWatchers = buildDataWatchers(entity, component, visible);
        PacketContainer packet2 = createEntityMetadataPacket(entity.getEntityId(), dataWatchers);

        return new PacketContainer[] {p(packet1), packet2};
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
            EntityTypes<EntityArmorStand> type = EntityTypes.d;
            Vec3D vec = new Vec3D(velocity.getX(), velocity.getY(), velocity.getZ());
            PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entityId, uuid, location.getX(), location.getY(), location.getZ(), 0, 0, type, 0, vec, 0);

            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherSilentField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = 0x20;
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), true));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherSilentField.get(null), true));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !gravity));

            byte standBitmask = (byte) 0x01 | 0x08 | 0x10;
            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.bG, standBitmask));

            PacketContainer packet2 = createEntityMetadataPacket(entityId, dataWatcher);

            return new PacketContainer[] {p(packet1), packet2};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DataWatcher.Item<?>> readDataWatchersFromMetadataPacket(PacketContainer packet) {
        PacketPlayOutEntityMetadata nmsPacket = (PacketPlayOutEntityMetadata) packet.getHandle();
        return nmsPacket.b();
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

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            addOrReplaceDataWatcher(dataWatcher, new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            addOrReplaceDataWatcher(dataWatcher, new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer createModifiedMetadataPacket(PacketContainer packet, List<?> dataWatchers) {
        PacketPlayOutEntityMetadata nmsPacket = (PacketPlayOutEntityMetadata) packet.getHandle();
        return createEntityMetadataPacket(nmsPacket.c(), dataWatchers);
    }
}
