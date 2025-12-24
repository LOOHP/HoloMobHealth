package com.loohp.holomobhealth.platform;

import com.loohp.holomobhealth.platform.packets.PlatformPacket;

@FunctionalInterface
public interface PlatformPacketEventListener<PacketEvent, Packet, PlatformPacketTyped extends PlatformPacket<Packet>> {

    void handle(PlatformPacketEvent<PacketEvent, Packet, PlatformPacketTyped> event);

}
