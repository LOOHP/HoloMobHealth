package com.loohp.holomobhealth.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityMetadataPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityLivingPacket;

import java.util.List;

public class ProtocolLibPlayClientSpawnEntityLivingPacket extends PlatformPlayClientSpawnEntityLivingPacket<PacketContainer> {

    public ProtocolLibPlayClientSpawnEntityLivingPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayClientSpawnEntityLivingPacket shallowClone() {
        return new ProtocolLibPlayClientSpawnEntityLivingPacket(handle.shallowClone());
    }

    @Override
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

}
