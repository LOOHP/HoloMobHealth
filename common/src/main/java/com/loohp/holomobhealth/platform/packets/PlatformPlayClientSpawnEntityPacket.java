package com.loohp.holomobhealth.platform.packets;

public abstract class PlatformPlayClientSpawnEntityPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayClientSpawnEntityPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayClientSpawnEntityPacket<Packet> shallowClone();

    public abstract int getEntityId();

}
