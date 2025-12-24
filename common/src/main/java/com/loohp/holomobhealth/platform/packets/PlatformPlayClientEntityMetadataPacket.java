package com.loohp.holomobhealth.platform.packets;

import java.util.List;

public abstract class PlatformPlayClientEntityMetadataPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayClientEntityMetadataPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayClientEntityMetadataPacket<Packet> shallowClone();

    public abstract int getEntityId();

    public abstract List<?> getEntityDataWatchers();

    public abstract void setEntityDataWatchers(List<?> dataWatchers);

}
