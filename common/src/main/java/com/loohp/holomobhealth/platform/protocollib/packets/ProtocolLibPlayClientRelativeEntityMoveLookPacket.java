package com.loohp.holomobhealth.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientRelativeEntityMoveLookPacket;

public class ProtocolLibPlayClientRelativeEntityMoveLookPacket extends PlatformPlayClientRelativeEntityMoveLookPacket<PacketContainer> {

    public ProtocolLibPlayClientRelativeEntityMoveLookPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayClientRelativeEntityMoveLookPacket shallowClone() {
        return new ProtocolLibPlayClientRelativeEntityMoveLookPacket(handle.shallowClone());
    }

    @Override
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

}
