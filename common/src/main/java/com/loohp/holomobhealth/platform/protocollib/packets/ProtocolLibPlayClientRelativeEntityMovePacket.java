package com.loohp.holomobhealth.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityTeleportPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientRelativeEntityMovePacket;

public class ProtocolLibPlayClientRelativeEntityMovePacket extends PlatformPlayClientRelativeEntityMovePacket<PacketContainer> {

    public ProtocolLibPlayClientRelativeEntityMovePacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayClientRelativeEntityMovePacket shallowClone() {
        return new ProtocolLibPlayClientRelativeEntityMovePacket(handle.shallowClone());
    }

    @Override
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

}
