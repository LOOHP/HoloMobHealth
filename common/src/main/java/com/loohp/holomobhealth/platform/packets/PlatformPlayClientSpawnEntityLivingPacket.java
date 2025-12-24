package com.loohp.holomobhealth.platform.packets;

public abstract class PlatformPlayClientSpawnEntityLivingPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayClientSpawnEntityLivingPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayClientSpawnEntityLivingPacket<Packet> shallowClone();

    public abstract int getEntityId();

}
