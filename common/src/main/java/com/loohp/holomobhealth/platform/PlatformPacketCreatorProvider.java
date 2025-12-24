package com.loohp.holomobhealth.platform;

import com.loohp.holomobhealth.holders.IHoloMobArmorStand;
import com.loohp.holomobhealth.platform.packets.PlatformPacket;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public interface PlatformPacketCreatorProvider<Packet> {

    List<PlatformPacket<Packet>> createEntityDestroyPackets(int... entityIds);

    List<PlatformPacket<Packet>> createEntityTeleportPackets(int entityId, Location location);

    List<PlatformPacket<Packet>> createUpdateEntityPackets(Entity entity);

    List<PlatformPacket<Packet>> createUpdateEntityMetadataPackets(Entity entity, Component entityNameComponent, boolean visible);

    List<PlatformPacket<Packet>> createArmorStandSpawnPackets(IHoloMobArmorStand entity, Component component, boolean visible);

    List<PlatformPacket<Packet>> createUpdateArmorStandPackets(IHoloMobArmorStand entity, Component component, boolean visible);

    List<PlatformPacket<Packet>> createUpdateArmorStandLocationPackets(IHoloMobArmorStand entity);

    List<PlatformPacket<Packet>> createSpawnDamageIndicatorPackets(int entityId, UUID uuid, Component entityNameComponent, Location location, Vector velocity, boolean gravity);

    void modifyDataWatchers(List<?> dataWatchers, Component entityNameComponent, boolean visible);

}
