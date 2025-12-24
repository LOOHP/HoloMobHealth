package com.loohp.holomobhealth.platform.packets;

public abstract class PlatformPlayClientEntityTeleportPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayClientEntityTeleportPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayClientEntityTeleportPacket<Packet> shallowClone();

    public abstract int getEntityId();

}
