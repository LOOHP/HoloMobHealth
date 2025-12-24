package com.loohp.holomobhealth.platform.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.holomobhealth.platform.PlatformPacketEventListener;
import com.loohp.holomobhealth.platform.PlatformPacketListenerPriority;
import com.loohp.holomobhealth.platform.PlatformPacketListenerProvider;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityMetadataPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientEntityTeleportPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientRelativeEntityMoveLookPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientRelativeEntityMovePacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityLivingPacket;
import com.loohp.holomobhealth.platform.packets.PlatformPlayClientSpawnEntityPacket;
import com.loohp.holomobhealth.platform.protocollib.packets.ProtocolLibPlayClientEntityMetadataPacket;
import com.loohp.holomobhealth.platform.protocollib.packets.ProtocolLibPlayClientEntityTeleportPacket;
import com.loohp.holomobhealth.platform.protocollib.packets.ProtocolLibPlayClientRelativeEntityMoveLookPacket;
import com.loohp.holomobhealth.platform.protocollib.packets.ProtocolLibPlayClientRelativeEntityMovePacket;
import com.loohp.holomobhealth.platform.protocollib.packets.ProtocolLibPlayClientSpawnEntityLivingPacket;
import com.loohp.holomobhealth.platform.protocollib.packets.ProtocolLibPlayClientSpawnEntityPacket;
import org.bukkit.plugin.Plugin;

public class ProtocolLibPacketListenerProvider implements PlatformPacketListenerProvider<PacketEvent, PacketContainer> {

    private static ListenerPriority c(PlatformPacketListenerPriority priority) {
        switch (priority) {
            case LOWEST:
                return ListenerPriority.LOWEST;
            case LOW:
                return ListenerPriority.LOW;
            case NORMAL:
                return ListenerPriority.NORMAL;
            case HIGH:
                return ListenerPriority.HIGH;
            case HIGHEST:
                return ListenerPriority.HIGHEST;
            case MONITOR:
                return ListenerPriority.MONITOR;
        }
        throw new IllegalArgumentException("Unknown priority " + priority.name());
    }

    private final ProtocolLibPlatform platform;

    public ProtocolLibPacketListenerProvider(ProtocolLibPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void listenToPlayClientEntityMeta(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayClientEntityMetadataPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().listenerPriority(c(priority)).plugin(plugin).types(PacketType.Play.Server.ENTITY_METADATA)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayClientEntityMetadataPacket(packet)));
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void listenToPlayClientSpawnEntityLiving(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayClientSpawnEntityLivingPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().listenerPriority(c(priority)).plugin(plugin).types(PacketType.Play.Server.SPAWN_ENTITY_LIVING)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayClientSpawnEntityLivingPacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayClientSpawnEntity(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayClientSpawnEntityPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().listenerPriority(c(priority)).plugin(plugin).types(PacketType.Play.Server.SPAWN_ENTITY)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayClientSpawnEntityPacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayClientEntityTeleport(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayClientEntityTeleportPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().listenerPriority(c(priority)).plugin(plugin).types(PacketType.Play.Server.ENTITY_TELEPORT)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayClientEntityTeleportPacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayClientRelativeEntityMove(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayClientRelativeEntityMovePacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().listenerPriority(c(priority)).plugin(plugin).types(PacketType.Play.Server.REL_ENTITY_MOVE)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayClientRelativeEntityMovePacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayClientRelativeEntityMoveLook(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayClientRelativeEntityMoveLookPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().listenerPriority(c(priority)).plugin(plugin).types(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayClientRelativeEntityMoveLookPacket(packet)));
            }
        });
    }


}
