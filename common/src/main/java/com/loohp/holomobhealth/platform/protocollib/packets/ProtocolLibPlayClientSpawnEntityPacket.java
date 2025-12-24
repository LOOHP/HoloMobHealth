package com.loohp.holomobhealth.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityLivingPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityPacket;

public class ProtocolLibPlayClientSpawnEntityPacket extends PlatformPlayClientSpawnEntityPacket<PacketContainer> {

    public ProtocolLibPlayClientSpawnEntityPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayClientSpawnEntityPacket shallowClone() {
        return new ProtocolLibPlayClientSpawnEntityPacket(handle.shallowClone());
    }

    @Override
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

}
