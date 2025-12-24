package com.loohp.holomobhealth.platform;

import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityMetadataPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityTeleportPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientRelativeEntityMoveLookPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientRelativeEntityMovePacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityLivingPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityPacket;
import org.bukkit.plugin.Plugin;

public interface PlatformPacketListenerProvider<PacketEvent, Packet> {

    void listenToPlayClientEntityMeta(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayClientEntityMetadataPacket<Packet>> listener);

    void listenToPlayClientSpawnEntityLiving(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayClientSpawnEntityLivingPacket<Packet>> listener);

    void listenToPlayClientSpawnEntity(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayClientSpawnEntityPacket<Packet>> listener);

    void listenToPlayClientEntityTeleport(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayClientEntityTeleportPacket<Packet>> listener);

    void listenToPlayClientRelativeEntityMove(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayClientRelativeEntityMovePacket<Packet>> listener);

    void listenToPlayClientRelativeEntityMoveLook(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayClientRelativeEntityMoveLookPacket<Packet>> listener);

}
