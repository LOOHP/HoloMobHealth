/*
 * This file is part of HoloMobHealth.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.HoloMobHealth;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PacketSender {

    private static void schedule(Runnable runnable) {
        if (HoloMobHealth.sendPacketsOnMainThread && !Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(HoloMobHealth.plugin, runnable);
        } else {
            runnable.run();
        }
    }

    public static void sendServerPacket(Player receiver, PacketContainer packet) {
        schedule(() -> HoloMobHealth.protocolManager.sendServerPacket(receiver, packet));
    }

    public static void sendServerPacket(Collection<? extends Player> receivers, PacketContainer packet) {
        schedule(() -> {
            for (Player receiver : receivers) {
                HoloMobHealth.protocolManager.sendServerPacket(receiver, packet);
            }
        });
    }

    public static void sendServerPackets(Player receiver, PacketContainer... packets) {
        schedule(() -> {
            for (PacketContainer packet : packets) {
                HoloMobHealth.protocolManager.sendServerPacket(receiver, packet);
            }
        });
    }

    public static void sendServerPackets(Player receiver, Collection<? extends PacketContainer> packets) {
        schedule(() -> {
            for (PacketContainer packet : packets) {
                HoloMobHealth.protocolManager.sendServerPacket(receiver, packet);
            }
        });
    }

    public static void sendServerPackets(Collection<? extends Player> receivers, PacketContainer... packets) {
        schedule(() -> {
            for (Player receiver : receivers) {
                for (PacketContainer packet : packets) {
                    HoloMobHealth.protocolManager.sendServerPacket(receiver, packet);
                }
            }
        });
    }

    public static void sendServerPackets(Collection<? extends Player> receivers, Collection<? extends PacketContainer> packets) {
        schedule(() -> {
            for (Player receiver : receivers) {
                for (PacketContainer packet : packets) {
                    HoloMobHealth.protocolManager.sendServerPacket(receiver, packet);
                }
            }
        });
    }

    public static void sendServerPacket(Player receiver, PacketContainer packet, boolean filters) {
        schedule(() -> HoloMobHealth.protocolManager.sendServerPacket(receiver, packet, filters));
    }

}
