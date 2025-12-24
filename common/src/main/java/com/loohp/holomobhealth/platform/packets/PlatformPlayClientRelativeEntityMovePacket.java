package com.loohp.holomobhealth.platform.packets;

public abstract class PlatformPlayClientRelativeEntityMovePacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayClientRelativeEntityMovePacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayClientRelativeEntityMovePacket<Packet> shallowClone();

    public abstract int getEntityId();

}
