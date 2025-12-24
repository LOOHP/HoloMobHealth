package com.loohp.holomobhealth.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityMetadataPacket;

import java.util.List;

public class ProtocolLibPlayClientEntityMetadataPacket extends PlatformPlayClientEntityMetadataPacket<PacketContainer> {

    public ProtocolLibPlayClientEntityMetadataPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayClientEntityMetadataPacket shallowClone() {
        return new ProtocolLibPlayClientEntityMetadataPacket(handle.shallowClone());
    }

    @Override
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    @Override
    public List<?> getEntityDataWatchers() {
        return NMS.getInstance().readDataWatchersFromMetadataPacket(handle);
    }

    @Override
    public void setEntityDataWatchers(List<?> dataWatchers) {
        PacketContainer packetContainer = NMS.getInstance().createModifiedMetadataPacket(handle, dataWatchers);
        for (int i = 0; i < Math.min(handle.getModifier().size(), packetContainer.getModifier().size()); i++) {
            handle.getModifier().write(i, packetContainer.getModifier().read(i));
        }
    }

}
