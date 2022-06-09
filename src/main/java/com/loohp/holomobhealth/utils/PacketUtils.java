/*
 * This file is part of HoloMobHealth.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.holomobhealth.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.HoloMobHealth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PacketUtils {

    private static Constructor<?> entityDestroyIntListConstructor;

    static {
        try {
            try {
                Class<?> entityDestroyClass = NMSUtils.getNMSClass("net.minecraft.server.%s.PacketPlayOutEntityDestroy", "net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy");
                entityDestroyIntListConstructor = entityDestroyClass.getConstructor(int[].class);
            } catch (NoSuchMethodException e) {
                entityDestroyIntListConstructor = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PacketContainer[] createEntityDestroyPacket(int... entityIds) {
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
            if (entityDestroyIntListConstructor == null) {
                PacketContainer[] packets = new PacketContainer[entityIds.length];
                for (int i = 0; i < entityIds.length; i++) {
                    PacketContainer packet = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                    packet.getIntegers().write(0, entityIds[i]);
                    packets[i] = packet;
                }
                return packets;
            } else {
                try {
                    return new PacketContainer[] {PacketContainer.fromPacket(entityDestroyIntListConstructor.newInstance(entityIds))};
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
