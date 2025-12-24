package com.loohp.holomobhealth.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityTeleportPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityPacket;

public class ProtocolLibPlayClientEntityTeleportPacket extends PlatformPlayClientEntityTeleportPacket<PacketContainer> {

    public ProtocolLibPlayClientEntityTeleportPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayClientEntityTeleportPacket shallowClone() {
        return new ProtocolLibPlayClientEntityTeleportPacket(handle.shallowClone());
    }

    @Override
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

}
