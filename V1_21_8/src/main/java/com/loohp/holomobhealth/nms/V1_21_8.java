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
import com.loohp.holomobhealth.utils.ReflectionUtils;
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
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.animal.EntityTropicalFish;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftEntityType;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class V1_21_8 extends NMSWrapper {

    private final Field entityCountField;
    private final Field dataWatcherByteField;
    private final Field dataWatcherCustomNameField;
    private final Field dataWatcherCustomNameVisibleField;
    private final Field dataWatcherSilentField;
    private final Field dataWatcherNoGravityField;

    //paper
    private Method worldServerEntityLookup;

    public V1_21_8() {
        try {
            entityCountField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, AtomicInteger.class, "ENTITY_COUNTER", "c");
            dataWatcherByteField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_SHARED_FLAGS_ID", "az");
            dataWatcherCustomNameField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_CUSTOM_NAME", "bl");
            dataWatcherCustomNameVisibleField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_CUSTOM_NAME_VISIBLE", "bm");
            dataWatcherSilentField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_SILENT", "bn");
            dataWatcherNoGravityField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_NO_GRAVITY", "bo");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            //paper
            //noinspection JavaReflectionMemberAccess
            worldServerEntityLookup = WorldServer.class.getMethod("moonrise$getEntityLookup");
        } catch (NoSuchMethodException ignore) {
        }
    }

    @Override
    public PacketContainer[] createEntityDestroyPacket(int... entityIds) {
        return new PacketContainer[] {p(new PacketPlayOutEntityDestroy(entityIds))};
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createEntityMetadataPacket(int entityId, List<?> dataWatchers) {
        return p(new PacketPlayOutEntityMetadata(entityId, (List<DataWatcher.c<?>>) dataWatchers));
    }

    @Override
    public PacketContainer createEntityTeleportPacket(int entityId, Location location) {
        return p(new PacketPlayOutEntityTeleport(entityId, new PositionMoveRotation(new Vec3D(location.getX(), location.getY(), location.getZ()), Vec3D.c, location.getYaw(), location.getPitch()), Collections.emptySet(), false));
    }

    @Override
    public BoundingBox getBoundingBox(Entity entity) {
        org.bukkit.util.BoundingBox box = entity.getBoundingBox();
        return BoundingBox.of(box.getMin(), box.getMax());
    }

    @Override
    public String getEntityTranslationKey(Entity entity) {
        return CraftEntityType.bukkitToMinecraft(entity.getType()).g();
    }

    @Override
    public Component getEntityCustomName(Entity entity) {
        IChatBaseComponent customName = ((CraftEntity) entity).getHandle().aj();
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
        return fish.gS();
    }

    @SuppressWarnings("unchecked")
    @Override
    public UUID getEntityUUIDFromID(World world, int id) {
        try {
            WorldServer worldServer = ((CraftWorld) world).getHandle();
            LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter;
            if (worldServerEntityLookup == null) {
                levelEntityGetter = worldServer.P.e();
            } else {
                levelEntityGetter = (LevelEntityGetter<net.minecraft.world.entity.Entity>) worldServerEntityLookup.invoke(worldServer);
            }
            net.minecraft.world.entity.Entity entity = levelEntityGetter.a(id);
            return entity == null ? null : entity.cK();
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
        AxisAlignedBB axisAlignedBB = nmsEntity.cV();
        double minY = axisAlignedBB.b;
        double maxY = axisAlignedBB.e;
        return maxY - minY;
    }

    @Override
    public double getEntityWidth(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB axisAlignedBB = nmsEntity.cV();
        double minX = axisAlignedBB.a;
        double maxX = axisAlignedBB.d;
        return maxX - minX;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityPacket(Entity entity) {
        try {
            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            DataWatcher watcher = nmsEntity.au();

            Optional<IChatBaseComponent> name = watcher.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null));
            boolean visible = watcher.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null));

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return createEntityMetadataPacket(entity.getEntityId(), dataWatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityMetadataPacket(Entity entity, Component entityNameComponent, boolean visible) {
        try {
            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return createEntityMetadataPacket(entity.getEntityId(), dataWatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<?> buildDataWatchers(IHoloMobArmorStand entity, Component entityNameComponent, boolean visible) {
        try {
            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = 0x20;
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), true));

            byte standbitmask = 0x01 | 0x10;
            dataWatcher.add(DataWatcher.c.a(EntityArmorStand.bS, standbitmask));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer[] createArmorStandSpawnPackets(IHoloMobArmorStand entity, Component component, boolean visible) {
        EntityTypes<?> type = CraftEntityType.bukkitToMinecraft(entity.getType());
        Vec3D velocity = Vec3D.c;
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
            EntityTypes<EntityArmorStand> type = EntityTypes.g;
            Vec3D vec = new Vec3D(velocity.getX(), velocity.getY(), velocity.getZ());
            PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entityId, uuid, location.getX(), location.getY(), location.getZ(), 0, 0, type, 0, vec, 0);

            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherSilentField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = 0x20;
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), true));

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherSilentField.get(null), true));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !gravity));

            byte standBitmask = (byte) 0x01 | 0x08 | 0x10;
            dataWatcher.add(DataWatcher.c.a(EntityArmorStand.bS, standBitmask));

            PacketContainer packet2 = createEntityMetadataPacket(entityId, dataWatcher);

            return new PacketContainer[] {p(packet1), packet2};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DataWatcher.c<?>> readDataWatchersFromMetadataPacket(PacketContainer packet) {
        PacketPlayOutEntityMetadata nmsPacket = (PacketPlayOutEntityMetadata) packet.getHandle();
        return nmsPacket.e();
    }

    private <T> void addOrReplaceDataWatcher(List<DataWatcher.c<?>> dataWatcher, DataWatcher.c<T> newWatcher) {
        for (int i = 0; i < dataWatcher.size(); i++) {
            DataWatcher.c<?> watcher = dataWatcher.get(i);
            if (newWatcher.a() == watcher.a() && newWatcher.b().equals(watcher.b())) {
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
            List<DataWatcher.c<?>> dataWatcher = (List<DataWatcher.c<?>>) dataWatchers;

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            Optional<IChatBaseComponent> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            addOrReplaceDataWatcher(dataWatcher, DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            addOrReplaceDataWatcher(dataWatcher, DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer createModifiedMetadataPacket(PacketContainer packet, List<?> dataWatchers) {
        PacketPlayOutEntityMetadata nmsPacket = (PacketPlayOutEntityMetadata) packet.getHandle();
        return createEntityMetadataPacket(nmsPacket.b(), dataWatchers);
    }
}
