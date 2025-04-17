/*
 * This file is part of HoloMobHealth2.
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

package com.loohp.holomobhealth.modules;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.utils.ChatColorUtils;
import com.loohp.holomobhealth.utils.CitizensUtils;
import com.loohp.holomobhealth.utils.CustomNameUtils;
import com.loohp.holomobhealth.utils.EntityTypeUtils;
import com.loohp.holomobhealth.utils.MathUtils;
import com.loohp.holomobhealth.utils.MyPetUtils;
import com.loohp.holomobhealth.utils.MythicMobsUtils;
import com.loohp.holomobhealth.utils.PacketSender;
import com.loohp.holomobhealth.utils.ParsePlaceholders;
import com.loohp.holomobhealth.utils.ShopkeepersUtils;
import com.loohp.holomobhealth.utils.WorldGuardUtils;
import net.kyori.adventure.text.Component;
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
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class DamageIndicator implements Listener {

    private static final Random RANDOM = new Random();
    private static final Vector VECTOR_ZERO = new Vector(0, 0, 0);
    private static final double EPSILON = 0.001;

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
        double height = NMS.getInstance().getEntityHeight(entity);
        double width = NMS.getInstance().getEntityWidth(entity);

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
        double height = NMS.getInstance().getEntityHeight(entity);
        double width = NMS.getInstance().getEntityWidth(entity);

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
                entityId = NMS.getInstance().getNextEntityId().get();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            UUID uuid = UUID.randomUUID();
            Location originalLocation = location.clone();

            PacketContainer[] packets = NMS.getInstance().createSpawnDamageIndicatorPackets(entityId, uuid, entityNameComponent, location, velocity, gravity);

            int range = HoloMobHealth.damageIndicatorVisibleRange;
            List<Player> players = location.getWorld().getPlayers().stream().filter(each -> {
                Location loc = each.getLocation();
                return loc.getWorld().equals(location.getWorld()) && loc.distance(location) <= range * range && HoloMobHealth.playersEnabled.contains(each);
            }).collect(Collectors.toList());

            PacketSender.sendServerPackets(players, packets);

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

                        PacketContainer packet = NMS.getInstance().createEntityTeleportPacket(entityId, location);

                        PacketSender.sendServerPacket(players, packet);
                    } else if (tick >= HoloMobHealth.damageIndicatorTimeout) {
                        this.cancel();
                        PacketContainer[] packets = NMS.getInstance().createEntityDestroyPacket(entityId);
                        Bukkit.getScheduler().runTaskLaterAsynchronously(HoloMobHealth.plugin, () -> {
                            PacketSender.sendServerPackets(players, packets);
                        }, 3);
                    }
                }
            }.runTaskTimerAsynchronously(HoloMobHealth.plugin, 0, 1);
        });
    }

}
