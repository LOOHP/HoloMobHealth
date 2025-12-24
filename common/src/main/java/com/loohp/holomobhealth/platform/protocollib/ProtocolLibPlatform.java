/*
 * This file is part of InteractiveChat4.
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

package com.loohp.holomobhealth.platform.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.platform.PlatformPacketCreatorProvider;
import com.loohp.holomobhealth.platform.ProtocolPlatform;
import com.loohp.holomobhealth.platform.packets.PlatformPacket;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ProtocolLibPlatform implements ProtocolPlatform<PacketEvent, PacketContainer> {

    private final ProtocolManager protocolManager;
    private final ProtocolLibPacketListenerProvider listenerProvider;
    private final ProtocolLibPacketCreatorProvider creatorProvider;

    public ProtocolLibPlatform() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.listenerProvider = new ProtocolLibPacketListenerProvider(this);
        this.creatorProvider = new ProtocolLibPacketCreatorProvider();
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    @Override
    public int getProtocolVersion(Player player) {
        return protocolManager.getProtocolVersion(player);
    }

    @Override
    public void sendServerPacket(Player player, PlatformPacket<?> packet, boolean filtered) {
        Object handle = packet.getHandle();
        if (!(handle instanceof PacketContainer)) {
            throw new IllegalArgumentException("packet belongs to another platform " + handle.getClass());
        }
        protocolManager.sendServerPacket(player, (PacketContainer) handle, filtered);
    }

    @Override
    public ProtocolLibPacketListenerProvider getPlatformPacketListenerProvider() {
        return listenerProvider;
    }

    @Override
    public PlatformPacketCreatorProvider<PacketContainer> getPlatformPacketCreatorProvider() {
        return creatorProvider;
    }

    @Override
    public Plugin getRegisteredPlugin() {
        return HoloMobHealth.plugin;
    }

    @Override
    public Plugin getProtocolPlatformPlugin() {
        return ProtocolLibrary.getPlugin();
    }

}
