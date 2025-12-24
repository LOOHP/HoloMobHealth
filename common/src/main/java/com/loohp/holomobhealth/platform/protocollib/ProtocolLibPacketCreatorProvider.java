package com.loohp.holomobhealth.platform.protocollib;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.holders.IHoloMobArmorStand;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.platform.PlatformPacketCreatorProvider;
import com.loohp.holomobhealth.platform.packets.PlatformPacket;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProtocolLibPacketCreatorProvider implements PlatformPacketCreatorProvider<PacketContainer> {

    private static List<PlatformPacket<PacketContainer>> c(PacketContainer[] packets) {
        return Arrays.stream(packets).map(p -> new ProtocolLibGenericCreatorPacket(p)).collect(Collectors.toList());
    }

    private static List<PlatformPacket<PacketContainer>> c(PacketContainer packet) {
        return Collections.singletonList(new ProtocolLibGenericCreatorPacket(packet));
    }

    private static class ProtocolLibGenericCreatorPacket extends PlatformPacket<PacketContainer> {
        public ProtocolLibGenericCreatorPacket(PacketContainer handle) {
            super(handle);
        }
        @Override
        public ProtocolLibGenericCreatorPacket shallowClone() {
            return new ProtocolLibGenericCreatorPacket(handle.shallowClone());
        }
    }

    @Override
    public List<PlatformPacket<PacketContainer>> createEntityDestroyPackets(int... entityIds) {
        return c(NMS.getInstance().createEntityDestroyPacket(entityIds));
    }

    @Override
    public List<PlatformPacket<PacketContainer>> createEntityTeleportPackets(int entityId, Location location) {
        return c(NMS.getInstance().createEntityTeleportPacket(entityId, location));
    }

    @Override
    public List<PlatformPacket<PacketContainer>> createUpdateEntityPackets(Entity entity) {
        return c(NMS.getInstance().createUpdateEntityPacket(entity));
    }

    @Override
    public List<PlatformPacket<PacketContainer>> createUpdateEntityMetadataPackets(Entity entity, Component entityNameComponent, boolean visible) {
        return c(NMS.getInstance().createUpdateEntityMetadataPacket(entity, entityNameComponent, visible));
    }

    @Override
    public List<PlatformPacket<PacketContainer>> createArmorStandSpawnPackets(IHoloMobArmorStand entity, Component component, boolean visible) {
        return c(NMS.getInstance().createArmorStandSpawnPackets(entity, component, visible));
    }

    @Override
    public List<PlatformPacket<PacketContainer>> createUpdateArmorStandPackets(IHoloMobArmorStand entity, Component component, boolean visible) {
        return c(NMS.getInstance().createUpdateArmorStandPackets(entity, component, visible));
    }

    @Override
    public List<PlatformPacket<PacketContainer>> createUpdateArmorStandLocationPackets(IHoloMobArmorStand entity) {
        return c(NMS.getInstance().createUpdateArmorStandLocationPackets(entity));
    }

    @Override
    public List<PlatformPacket<PacketContainer>> createSpawnDamageIndicatorPackets(int entityId, UUID uuid, Component entityNameComponent, Location location, Vector velocity, boolean gravity) {
        return c(NMS.getInstance().createSpawnDamageIndicatorPackets(entityId, uuid, entityNameComponent, location, velocity, gravity));
    }

    @Override
    public void modifyDataWatchers(List<?> dataWatchers, Component entityNameComponent, boolean visible) {
        NMS.getInstance().modifyDataWatchers(dataWatchers, entityNameComponent, visible);
    }
}
