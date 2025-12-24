package com.loohp.holomobhealth.platform.packets;

public abstract class PlatformPlayClientRelativeEntityMoveLookPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayClientRelativeEntityMoveLookPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayClientRelativeEntityMoveLookPacket<Packet> shallowClone();

    public abstract int getEntityId();

}
