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
import com.loohp.holomobhealth.holders.DataWatcherField;
import com.loohp.holomobhealth.holders.DataWatcherFieldType;
import com.loohp.holomobhealth.holders.DataWatcherFields;
import com.loohp.holomobhealth.holders.IHoloMobArmorStand;
import com.loohp.holomobhealth.utils.BoundingBox;
import com.loohp.holomobhealth.utils.ReflectionUtils;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

@SuppressWarnings("unused")
public class V26_1 extends NMSWrapper {

    private final Field entityCountField;
    private final Field dataWatcherByteField;
    private final Field dataWatcherCustomNameField;
    private final Field dataWatcherCustomNameVisibleField;
    private final Field dataWatcherSilentField;
    private final Field dataWatcherNoGravityField;

    //paper
    private Method worldServerEntityLookup;

    public V26_1() {
        try {
            entityCountField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, AtomicInteger.class, "ENTITY_COUNTER");
            dataWatcherByteField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_SHARED_FLAGS_ID");
            dataWatcherCustomNameField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_CUSTOM_NAME");
            dataWatcherCustomNameVisibleField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_CUSTOM_NAME_VISIBLE");
            dataWatcherSilentField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_SILENT");
            dataWatcherNoGravityField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_NO_GRAVITY");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        try {
            //paper
            //noinspection JavaReflectionMemberAccess
            worldServerEntityLookup = ServerLevel.class.getMethod("moonrise$getEntityLookup");
        } catch (NoSuchMethodException ignore) {
        }
    }

    @Override
    public PacketContainer[] createEntityDestroyPacket(int... entityIds) {
        return new PacketContainer[] {p(new ClientboundRemoveEntitiesPacket(entityIds))};
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createEntityMetadataPacket(int entityId, List<?> dataWatchers) {
        return p(new ClientboundSetEntityDataPacket(entityId, (List<SynchedEntityData.DataValue<?>>) dataWatchers));
    }

    @Override
    public PacketContainer createEntityTeleportPacket(int entityId, Location location) {
        return p(new ClientboundTeleportEntityPacket(entityId, new PositionMoveRotation(new Vec3(location.getX(), location.getY(), location.getZ()), Vec3.ZERO, location.getYaw(), location.getPitch()), Collections.emptySet(), false));
    }

    @Override
    public BoundingBox getBoundingBox(Entity entity) {
        org.bukkit.util.BoundingBox box = entity.getBoundingBox();
        return BoundingBox.of(box.getMin(), box.getMax());
    }

    @Override
    public String getEntityTranslationKey(Entity entity) {
        return CraftEntityType.bukkitToMinecraft(entity.getType()).getDescriptionId();
    }

    @Override
    public Component getEntityName(Entity entity) {
        net.minecraft.network.chat.Component customName = ((CraftEntity) entity).getHandle().getName();
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(customName));
    }

    @Override
    public Component getEntityCustomName(Entity entity) {
        net.minecraft.network.chat.Component customName = ((CraftEntity) entity).getHandle().getCustomName();
        return customName == null ? null : GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(customName));
    }

    @Override
    public Component getEntityDisplayName(Entity entity) {
        net.minecraft.network.chat.Component customName = ((CraftEntity) entity).getHandle().getDisplayName();
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
        TropicalFish fish = (TropicalFish) nmsEntity;
        return fish.getPackedVariant();
    }

    @SuppressWarnings("unchecked")
    private LevelEntityGetter<net.minecraft.world.entity.Entity> getLevelEntityGetter(World world) {
        try {
            ServerLevel worldServer = ((CraftWorld) world).getHandle();
            if (worldServerEntityLookup == null) {
                return worldServer.entityManager.getEntityGetter();
            } else {
                return (LevelEntityGetter<net.minecraft.world.entity.Entity>) worldServerEntityLookup.invoke(worldServer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Entity getEntityFromID(World world, int id) {
        net.minecraft.world.entity.Entity entity = getLevelEntityGetter(world).get(id);
        return entity == null ? null : entity.getBukkitEntity();
    }

    @Override
    public Entity getEntityFromUUID(UUID uuid) {
        for (World world : Bukkit.getWorlds()) {
            net.minecraft.world.entity.Entity entity = getLevelEntityGetter(world).get(uuid);
            if (entity != null) {
                return entity.getBukkitEntity();
            }
        }
        return null;
    }

    @Override
    public double getEntityHeight(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AABB axisAlignedBB = nmsEntity.getBoundingBox();
        double minY = axisAlignedBB.minY;
        double maxY = axisAlignedBB.maxY;
        return maxY - minY;
    }

    @Override
    public double getEntityWidth(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        AABB axisAlignedBB = nmsEntity.getBoundingBox();
        double minX = axisAlignedBB.minX;
        double maxX = axisAlignedBB.maxX;
        return maxX - minX;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityPacket(Entity entity) {
        try {
            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            SynchedEntityData watcher = nmsEntity.getEntityData();

            Optional<net.minecraft.network.chat.Component> name = watcher.get((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null));
            boolean visible = watcher.get((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null));

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return createEntityMetadataPacket(entity.getEntityId(), dataWatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketContainer createUpdateEntityMetadataPacket(Entity entity, Component entityNameComponent, boolean visible) {
        try {
            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            Optional<net.minecraft.network.chat.Component> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return createEntityMetadataPacket(entity.getEntityId(), dataWatcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<?> buildDataWatchers(IHoloMobArmorStand entity, Component entityNameComponent, boolean visible) {
        try {
            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = 0x20;
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Byte>) dataWatcherByteField.get(null), bitmask));

            Optional<net.minecraft.network.chat.Component> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherNoGravityField.get(null), true));

            byte standbitmask = 0x01 | 0x10;
            dataWatcher.add(SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, standbitmask));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer[] createArmorStandSpawnPackets(IHoloMobArmorStand entity, Component component, boolean visible) {
        EntityType<?> type = CraftEntityType.bukkitToMinecraft(entity.getType());
        Vec3 velocity = Vec3.ZERO;
        ClientboundAddEntityPacket packet1 = new ClientboundAddEntityPacket(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, 0, velocity, entity.getLocation().getYaw());

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
            EntityType<ArmorStand> type = EntityType.ARMOR_STAND;
            Vec3 vec = new Vec3(velocity.getX(), velocity.getY(), velocity.getZ());
            ClientboundAddEntityPacket packet1 = new ClientboundAddEntityPacket(entityId, uuid, location.getX(), location.getY(), location.getZ(), 0, 0, type, 0, vec, 0);

            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherSilentField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = 0x20;
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Byte>) dataWatcherByteField.get(null), bitmask));

            Optional<net.minecraft.network.chat.Component> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), true));

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherSilentField.get(null), true));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherNoGravityField.get(null), !gravity));

            byte standBitmask = (byte) 0x01 | 0x08 | 0x10;
            dataWatcher.add(SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, standBitmask));

            PacketContainer packet2 = createEntityMetadataPacket(entityId, dataWatcher);

            return new PacketContainer[] {p(packet1), packet2};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SynchedEntityData.DataValue<?>> readDataWatchersFromMetadataPacket(PacketContainer packet) {
        ClientboundSetEntityDataPacket nmsPacket = (ClientboundSetEntityDataPacket) packet.getHandle();
        return nmsPacket.packedItems();
    }

    private <T> void addOrReplaceDataWatcher(List<SynchedEntityData.DataValue<?>> dataWatcher, SynchedEntityData.DataValue<T> newWatcher) {
        for (int i = 0; i < dataWatcher.size(); i++) {
            SynchedEntityData.DataValue<?> watcher = dataWatcher.get(i);
            if (newWatcher.id() == watcher.id() && newWatcher.value().equals(watcher.value())) {
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
            List<SynchedEntityData.DataValue<?>> dataWatcher = (List<SynchedEntityData.DataValue<?>>) dataWatchers;

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            Optional<net.minecraft.network.chat.Component> name = entityNameComponent == null ? Optional.empty() : Optional.of(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(entityNameComponent)));
            addOrReplaceDataWatcher(dataWatcher, SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), name));
            addOrReplaceDataWatcher(dataWatcher, SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacketContainer createModifiedMetadataPacket(PacketContainer packet, List<?> dataWatchers) {
        ClientboundSetEntityDataPacket nmsPacket = (ClientboundSetEntityDataPacket) packet.getHandle();
        return createEntityMetadataPacket(nmsPacket.id(), dataWatchers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataWatcherFields getDataWatcherFields() {
        try {
            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherSilentField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);
            return new DataWatcherFields(
                    new DataWatcherField(((EntityDataAccessor<Byte>) dataWatcherByteField.get(null)).id(), DataWatcherFieldType.BYTE),
                    new DataWatcherField(((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null)).id(), DataWatcherFieldType.OPTIONAL_CHAT),
                    new DataWatcherField(((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null)).id(), DataWatcherFieldType.BOOLEAN),
                    new DataWatcherField(((EntityDataAccessor<Boolean>) dataWatcherSilentField.get(null)).id(), DataWatcherFieldType.BOOLEAN),
                    new DataWatcherField(((EntityDataAccessor<Boolean>) dataWatcherNoGravityField.get(null)).id(), DataWatcherFieldType.BOOLEAN),
                    new DataWatcherField(ArmorStand.DATA_CLIENT_FLAGS.id(), DataWatcherFieldType.BYTE)
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
