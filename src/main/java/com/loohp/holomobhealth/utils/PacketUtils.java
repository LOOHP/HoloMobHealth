package com.loohp.holomobhealth.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.HoloMobHealth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PacketUtils {

    private static Constructor<?> entityDestoryIntListConstructor;

    static {
        try {
            try {
                Class<?> entityDestroyClass = NMSUtils.getNMSClass("net.minecraft.server.%s.PacketPlayOutEntityDestroy", "net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy");
                entityDestoryIntListConstructor = entityDestroyClass.getConstructor(int[].class);
            } catch (NoSuchMethodException e) {
                entityDestoryIntListConstructor = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PacketContainer[] createEntityDestoryPacket(int... entityIds) {
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
            if (entityDestoryIntListConstructor == null) {
                PacketContainer[] packets = new PacketContainer[entityIds.length];
                for (int i = 0; i < entityIds.length; i++) {
                    PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                    packet.getIntegers().write(0, entityIds[i]);
                    packets[i] = packet;
                }
                return packets;
            } else {
                try {
                    return new PacketContainer[] {PacketContainer.fromPacket(entityDestoryIntListConstructor.newInstance(entityIds))};
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                    return new PacketContainer[0];
                }
            }
        } else {
            PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, entityIds);
            return new PacketContainer[] {packet};
        }
    }

}
