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

package com.loohp.holomobhealth.modules;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.utils.ChatColorUtils;
import com.loohp.holomobhealth.utils.CitizensUtils;
import com.loohp.holomobhealth.utils.CustomNameUtils;
import com.loohp.holomobhealth.utils.DataWatcherUtils;
import com.loohp.holomobhealth.utils.EntityTypeUtils;
import com.loohp.holomobhealth.utils.EntityUtils;
import com.loohp.holomobhealth.utils.LanguageUtils;
import com.loohp.holomobhealth.utils.MCVersion;
import com.loohp.holomobhealth.utils.MathUtils;
import com.loohp.holomobhealth.utils.MyPetUtils;
import com.loohp.holomobhealth.utils.MythicMobsUtils;
import com.loohp.holomobhealth.utils.NMSUtils;
import com.loohp.holomobhealth.utils.PacketUtils;
import com.loohp.holomobhealth.utils.ParsePlaceholders;
import com.loohp.holomobhealth.utils.ShopkeepersUtils;
import com.loohp.holomobhealth.utils.WorldGuardUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class DamageIndicator implements Listener {

    private static final Random RANDOM = new Random();
    private static final Vector VECTOR_ZERO = new Vector(0, 0, 0);
    private static final double EPSILON = 0.001;

    private int metaVersion;
    private Serializer booleanSerializer;
    private Serializer stringSerializer;
    private Serializer byteSerializer;
    private Serializer optChatSerializer;

    public DamageIndicator() {
        if (!HoloMobHealth.version.isOld()) {
            booleanSerializer = Registry.get(Boolean.class);
            stringSerializer = Registry.get(String.class);
            byteSerializer = Registry.get(Byte.class);
            if (!HoloMobHealth.version.isLegacy()) {
                optChatSerializer = Registry.getChatComponentSerializer(true);
            }
        }

        switch (HoloMobHealth.version) {
            case V1_19_3:
            case V1_19:
            case V1_18_2:
            case V1_18:
            case V1_17:
                metaVersion = 4;
                break;
            case V1_16_4:
            case V1_16_2:
            case V1_16:
            case V1_15:
                metaVersion = 3;
                break;
            case V1_14:
                metaVersion = 2;
                break;
            case V1_13_1:
            case V1_13:
                metaVersion = 1;
                break;
            case V1_12:
            case V1_11:
            case V1_10:
            case V1_9_4:
            case V1_9:
            case V1_8_4:
            case V1_8_3:
            case V1_8:
                metaVersion = 0;
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (HoloMobHealth.useDamageIndicator && HoloMobHealth.damageIndicatorDamageEnabled && !HoloMobHealth.damageIndicatorPlayerTriggered) {
            if (event.getCause().equals(DamageCause.SUICIDE) || event.getFinalDamage() > Integer.MAX_VALUE) {
                return;
            }

            Entity entity = event.getEntity();
            if (HoloMobHealth.disabledWorlds.contains(entity.getWorld().getName())) {
                return;
            }
            if (!HoloMobHealth.showCitizens && HoloMobHealth.citizensHook) {
                if (CitizensUtils.isNPC(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showMythicMobs && HoloMobHealth.mythicHook) {
                if (MythicMobsUtils.isMythicMob(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showShopkeepers && HoloMobHealth.shopkeepersHook) {
                if (ShopkeepersUtils.isShopkeeper(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showMyPet && HoloMobHealth.myPetHook) {
                if (MyPetUtils.isMyPet(entity)) {
                    return;
                }
            }
            String customName = CustomNameUtils.getMobCustomName(entity);
            if (customName != null) {
                for (String each : HoloMobHealth.disabledMobNamesAbsolute) {
                    if (customName.equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
                        return;
                    }
                }
                for (String each : HoloMobHealth.disabledMobNamesContains) {
                    if (ChatColorUtils.stripColor(customName.toLowerCase()).contains(ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
                        return;
                    }
                }
            }

            double finalDamage = event.getFinalDamage();
            if (finalDamage >= HoloMobHealth.damageIndicatorDamageMinimum && entity instanceof LivingEntity && (EntityTypeUtils.getMobsTypesSet().contains(EntityTypeUtils.getEntityType(entity)) || EntityTypeUtils.getEntityType(entity).equals(EntityType.PLAYER))) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (MathUtils.greaterThan(livingEntity.getHealth(), 0.0, EPSILON) && !livingEntity.isDead()) {
                    damage(livingEntity, finalDamage);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (HoloMobHealth.useDamageIndicator && HoloMobHealth.damageIndicatorDamageEnabled && HoloMobHealth.damageIndicatorPlayerTriggered) {
            if (event.getCause().equals(DamageCause.SUICIDE) || event.getFinalDamage() > Integer.MAX_VALUE) {
                return;
            }

            Entity entity = event.getEntity();
            if (HoloMobHealth.disabledWorlds.contains(entity.getWorld().getName())) {
                return;
            }
            if (!HoloMobHealth.showCitizens && HoloMobHealth.citizensHook) {
                if (CitizensUtils.isNPC(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showMythicMobs && HoloMobHealth.mythicHook) {
                if (MythicMobsUtils.isMythicMob(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showShopkeepers && HoloMobHealth.shopkeepersHook) {
                if (ShopkeepersUtils.isShopkeeper(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showMyPet && HoloMobHealth.myPetHook) {
                if (MyPetUtils.isMyPet(entity)) {
                    return;
                }
            }
            String customName = CustomNameUtils.getMobCustomName(entity);
            if (customName != null) {
                for (String each : HoloMobHealth.disabledMobNamesAbsolute) {
                    if (customName.equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
                        return;
                    }
                }
                for (String each : HoloMobHealth.disabledMobNamesContains) {
                    if (ChatColorUtils.stripColor(customName.toLowerCase()).contains(ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
                        return;
                    }
                }
            }

            if (!event.getDamager().getType().equals(EntityType.PLAYER)) {
                if (event.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) event.getDamager();
                    if (projectile.getShooter() == null) {
                        return;
                    } else {
                        if (!(projectile.getShooter() instanceof Player)) {
                            return;
                        }
                    }
                } else {
                    return;
                }
            }

            double finalDamage = event.getFinalDamage();
            if (finalDamage >= HoloMobHealth.damageIndicatorDamageMinimum && entity instanceof LivingEntity && (EntityTypeUtils.getMobsTypesSet().contains(EntityTypeUtils.getEntityType(entity)) || EntityTypeUtils.getEntityType(entity).equals(EntityType.PLAYER))) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (MathUtils.greaterThan(livingEntity.getHealth(), 0.0, EPSILON) && !livingEntity.isDead()) {
                    damage(livingEntity, finalDamage);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegen(EntityRegainHealthEvent event) {
        if (HoloMobHealth.useDamageIndicator && HoloMobHealth.damageIndicatorRegenEnabled) {

            Entity entity = event.getEntity();
            if (HoloMobHealth.disabledWorlds.contains(entity.getWorld().getName())) {
                return;
            }
            if (!HoloMobHealth.showCitizens && HoloMobHealth.citizensHook) {
                if (CitizensUtils.isNPC(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showMythicMobs && HoloMobHealth.mythicHook) {
                if (MythicMobsUtils.isMythicMob(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showShopkeepers && HoloMobHealth.shopkeepersHook) {
                if (ShopkeepersUtils.isShopkeeper(entity)) {
                    return;
                }
            }
            if (!HoloMobHealth.showMyPet && HoloMobHealth.myPetHook) {
                if (MyPetUtils.isMyPet(entity)) {
                    return;
                }
            }
            String customName = CustomNameUtils.getMobCustomName(entity);
            if (customName != null) {
                for (String each : HoloMobHealth.disabledMobNamesAbsolute) {
                    if (customName.equals(ChatColorUtils.translateAlternateColorCodes('&', each))) {
                        return;
                    }
                }
                for (String each : HoloMobHealth.disabledMobNamesContains) {
                    if (ChatColorUtils.stripColor(customName.toLowerCase()).contains(ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', each).toLowerCase()))) {
                        return;
                    }
                }
            }

            if (HoloMobHealth.damageIndicatorPlayerTriggered) {
                if (entity instanceof Player) {
                    LivingEntity livingentity = (LivingEntity) entity;
                    double health = livingentity.getHealth();
                    double maxHealth;
                    if (!HoloMobHealth.version.isLegacy()) {
                        maxHealth = livingentity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    } else {
                        maxHealth = livingentity.getMaxHealth();
                    }

                    double gain = Math.min(maxHealth - health, event.getAmount());
                    if (gain >= HoloMobHealth.damageIndicatorRegenMinimum) {
                        regen(livingentity, gain);
                    }
                }
            } else {
                if (entity instanceof LivingEntity && (EntityTypeUtils.getMobsTypesSet().contains(EntityTypeUtils.getEntityType(entity)) || EntityTypeUtils.getEntityType(entity).equals(EntityType.PLAYER))) {
                    LivingEntity livingentity = (LivingEntity) entity;
                    double health = livingentity.getHealth();
                    double maxHealth;
                    if (!HoloMobHealth.version.isLegacy()) {
                        maxHealth = livingentity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    } else {
                        maxHealth = livingentity.getMaxHealth();
                    }

                    double gain = Math.min(maxHealth - health, event.getAmount());
                    if (gain >= HoloMobHealth.damageIndicatorRegenMinimum) {
                        regen(livingentity, gain);
                    }
                }
            }
        }
    }

    public void damage(LivingEntity entity, double damage) {
        Location location = entity.getLocation();
        if (HoloMobHealth.worldGuardHook) {
            if (!WorldGuardUtils.checkStateFlag(location, null, WorldGuardUtils.getDamageIndicatorFlag())) {
                return;
            }
        }
        double height = NMSUtils.getEntityHeight(entity);
        double width = NMSUtils.getEntityWidth(entity);

        double x;
        double y = height / 2 + (RANDOM.nextDouble() - 0.5) * 0.5;
        double z;
        if (RANDOM.nextBoolean()) {
            x = RANDOM.nextBoolean() ? width : -width;
            z = (RANDOM.nextDouble() * width) - (width / 2);
        } else {
            x = (RANDOM.nextDouble() * width) - (width / 2);
            z = RANDOM.nextBoolean() ? width : -width;
        }

        Vector velocity;
        location.add(0, y + HoloMobHealth.damageIndicatorDamageY, 0);
        Location indicator = location.clone().add(x, 0, z);
        if (HoloMobHealth.damageIndicatorDamageAnimation) {
            velocity = indicator.toVector().subtract(location.toVector()).normalize().multiply(0.15).add(new Vector(0, 0.1, 0));
        } else {
            velocity = VECTOR_ZERO;
        }

        Component component = ParsePlaceholders.parse(entity, HoloMobHealth.damageIndicatorDamageText, -damage);
        playIndicator(component, indicator, velocity, true, height);
    }

    public void regen(LivingEntity entity, double gain) {
        Location location = entity.getLocation();
        if (HoloMobHealth.worldGuardHook) {
            if (!WorldGuardUtils.checkStateFlag(location, null, WorldGuardUtils.getRegenIndicatorFlag())) {
                return;
            }
        }
        double height = NMSUtils.getEntityHeight(entity);
        double width = NMSUtils.getEntityWidth(entity);

        double x;
        double z;
        if (RANDOM.nextBoolean()) {
            x = RANDOM.nextBoolean() ? width : -width;
            z = (RANDOM.nextDouble() * width) - (width / 2);
        } else {
            x = (RANDOM.nextDouble() * width) - (width / 2);
            z = RANDOM.nextBoolean() ? width : -width;
        }

        location.add(x, (height / 2 + (RANDOM.nextDouble() - 1) * 0.5) + HoloMobHealth.damageIndicatorRegenY, z);

        Vector velocity = HoloMobHealth.damageIndicatorRegenAnimation ? new Vector(0, 0.2, 0) : VECTOR_ZERO;

        Component component = ParsePlaceholders.parse(entity, HoloMobHealth.damageIndicatorRegenText, gain);
        playIndicator(component, location, velocity, false, height);
    }

    public void playIndicator(Component entityNameComponent, Location location, Vector velocity, boolean gravity, double fallHeight) {
        Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> {
            int entityId;
            try {
                entityId = EntityUtils.getNextEntityId().get();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            UUID uuid = UUID.randomUUID();
            Location originalLocation = location.clone();

            PacketContainer packet1 = HoloMobHealth.protocolManager.createPacket(HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_19) ? PacketType.Play.Server.SPAWN_ENTITY : PacketType.Play.Server.SPAWN_ENTITY_LIVING);
            packet1.getIntegers().write(0, entityId);
            if (packet1.getUUIDs().size() > 0) {
                packet1.getUUIDs().write(0, uuid);
            }
            if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_19)) {
                packet1.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
                packet1.getIntegers().write(1, (int) (velocity.getX() * 8000));
                packet1.getIntegers().write(2, (int) (velocity.getY() * 8000));
                packet1.getIntegers().write(3, (int) (velocity.getZ() * 8000));
            } else {
                packet1.getIntegers().write(1, HoloMobHealth.version.isLegacy() ? 30 : 1);
                packet1.getIntegers().write(2, (int) (velocity.getX() * 8000));
                packet1.getIntegers().write(3, (int) (velocity.getY() * 8000));
                packet1.getIntegers().write(4, (int) (velocity.getZ() * 8000));
            }
            packet1.getDoubles().write(0, location.getX());
            packet1.getDoubles().write(1, location.getY());
            packet1.getDoubles().write(2, location.getZ());
            packet1.getBytes().write(0, (byte) 0); //Yaw
            packet1.getBytes().write(1, (byte) 0); //Pitch
            packet1.getBytes().write(2, (byte) 0); //Head

            PacketContainer packet2 = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet2.getIntegers().write(0, entityId);
            WrappedDataWatcher watcher = new WrappedDataWatcher();

            byte bitmask = 0x20;
            watcher.setObject(new WrappedDataWatcherObject(0, byteSerializer), bitmask);

            if (entityNameComponent != null && !entityNameComponent.equals(Component.empty())) {
                if (HoloMobHealth.version.isOld()) {
                    watcher.setObject(2, LegacyComponentSerializer.legacySection().serialize(LanguageUtils.convert(entityNameComponent, HoloMobHealth.language)));
                } else if (HoloMobHealth.version.isLegacy()) {
                    WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, stringSerializer);
                    watcher.setObject(object, LegacyComponentSerializer.legacySection().serialize(LanguageUtils.convert(entityNameComponent, HoloMobHealth.language)));
                } else {
                    Optional<?> opt = Optional.of(WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(entityNameComponent)).getHandle());
                    watcher.setObject(new WrappedDataWatcherObject(2, optChatSerializer), opt);
                }
            } else {
                if (HoloMobHealth.version.isOld()) {
                    watcher.setObject(2, "");
                } else if (HoloMobHealth.version.isLegacy()) {
                    WrappedDataWatcherObject object = new WrappedDataWatcherObject(2, stringSerializer);
                    watcher.setObject(object, "");
                } else {
                    Optional<?> opt = Optional.empty();
                    watcher.setObject(new WrappedDataWatcherObject(2, optChatSerializer), opt);
                }
            }

            watcher.setObject(new WrappedDataWatcherObject(3, booleanSerializer), true);
            watcher.setObject(new WrappedDataWatcherObject(4, booleanSerializer), true);
            watcher.setObject(new WrappedDataWatcherObject(5, booleanSerializer), !gravity);

            byte standBitmask = (byte) 0x01 | 0x08 | 0x10;

            switch (metaVersion) {
                case 0:
                case 1:
                    watcher.setObject(new WrappedDataWatcherObject(11, byteSerializer), standBitmask);
                    break;
                case 2:
                    watcher.setObject(new WrappedDataWatcherObject(13, byteSerializer), standBitmask);
                    break;
                case 3:
                    watcher.setObject(new WrappedDataWatcherObject(14, byteSerializer), standBitmask);
                    break;
                case 4:
                    watcher.setObject(new WrappedDataWatcherObject(15, byteSerializer), standBitmask);
                    break;
            }

            DataWatcherUtils.writeMetadataPacket(packet2, watcher);

            int range = HoloMobHealth.damageIndicatorVisibleRange;
            List<Player> players = location.getWorld().getPlayers().stream().filter(each -> {
                Location loc = each.getLocation();
                return loc.getWorld().equals(location.getWorld()) && loc.distance(location) <= range * range && HoloMobHealth.playersEnabled.contains(each);
            }).collect(Collectors.toList());
            Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> {
                for (Player player : players) {
                    HoloMobHealth.protocolManager.sendServerPacket(player, packet1);
                    HoloMobHealth.protocolManager.sendServerPacket(player, packet2);
                }
            });

            Vector downwardAccel = new Vector(0, -0.05, 0);

            new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    tick++;
                    if (!velocity.equals(VECTOR_ZERO) && tick < HoloMobHealth.damageIndicatorTimeout && originalLocation.getY() - location.getY() < fallHeight) {
                        Vector drag = velocity.clone().normalize().multiply(-0.03);
                        if (gravity) {
                            velocity.add(downwardAccel);
                        }
                        velocity.add(drag);
                        location.add(velocity);

                        PacketContainer packet3 = HoloMobHealth.protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
                        packet3.getIntegers().write(0, entityId);
                        packet3.getDoubles().write(0, location.getX());
                        packet3.getDoubles().write(1, location.getY());
                        packet3.getDoubles().write(2, location.getZ());
                        packet3.getBytes().write(0, (byte) 0);
                        packet3.getBytes().write(1, (byte) 0);

                        Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> {
                            for (Player player : players) {
                                HoloMobHealth.protocolManager.sendServerPacket(player, packet3);
                            }
                        });
                    } else if (tick >= HoloMobHealth.damageIndicatorTimeout) {
                        this.cancel();
                        PacketContainer[] packets = PacketUtils.createEntityDestroyPacket(entityId);
                        Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
                            for (Player player : players) {
                                for (PacketContainer packet : packets) {
                                    HoloMobHealth.protocolManager.sendServerPacket(player, packet);
                                }
                            }
                        }, 3);
                    }
                }
            }.runTaskTimerAsynchronously(HoloMobHealth.plugin, 0, 1);
        });
    }

}
