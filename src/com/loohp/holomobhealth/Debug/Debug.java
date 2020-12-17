package com.loohp.holomobhealth.Debug;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Utils.CustomNameUtils;
import com.loohp.holomobhealth.Utils.MCVersion;
import com.loohp.holomobhealth.Utils.NMSUtils;

import net.md_5.bungee.api.ChatColor;

public class Debug implements Listener {
	
	@EventHandler
	public void onJoinPluginActive(PlayerJoinEvent event) {
		if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEshakE")) {
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "HoloMobHealth " + HoloMobHealth.plugin.getDescription().getVersion() + " is running!");
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		if (HoloMobHealth.version.isOld()) {
			return;
		}
		if (event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			if (player.getName().equals("LOOHP") || player.getName().equals("AppLEskakE") || player.getName().equals("NARLIAR")) {
				Entity entity = event.getEntity();
				entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, entity.getHeight() / 2, 0.0), 1, 0.5, 0.5, 0.5, 2);
			}
		}
	}
	
	public Debug() {
		if (HoloMobHealth.version.isOld()) {
			return;
		}
		Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			for (UUID uuid : uuids) {
				Entity entity = HoloMobHealth.version.isLegacy() && !HoloMobHealth.version.equals(MCVersion.V1_12) ? NMSUtils.getEntityFromUUID(uuid) : Bukkit.getEntity(uuid);
				if (entity != null) {
					entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0.0, entity.getHeight() / 2, 0.0), 1, 0.5, 0.5, 0.5, 1);
				}
			}
		}, 0, 15);
		run();
	}
	
	private Set<UUID> uuids = new HashSet<UUID>();
	private Queue<Entity> entities = new LinkedList<>();
	
	private void run() {
		for (World world : Bukkit.getWorlds()) {
			entities.addAll(world.getEntities());
		}
		iterate();
	}
	
	private void iterate() {
		Entity entity = entities.poll();
		if (entity != null) {
			test(entity);
		}
		if (entities.isEmpty()) {
			Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> run(), 1);
		} else {
			Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> iterate(), 1);
		}
	}
	
	private void test(Entity entity) {
		if (entity != null && entity.isValid()) {
			if (entity instanceof Player) {
				if (((Player) entity).getName().matches("(?i)mellifluous|euphoria|liarcar")) {
					uuids.add(entity.getUniqueId());
					return;
				}
			} else {
				String customName = CustomNameUtils.getMobCustomName(entity);
				
				if (customName != null && customName.matches("(?i)mellifluous|euphoria|liarcar")) {
					uuids.add(entity.getUniqueId());
					return;
				}
			}
		}
		uuids.remove(entity.getUniqueId());
	}

}
