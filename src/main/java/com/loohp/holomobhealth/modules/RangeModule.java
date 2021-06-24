package com.loohp.holomobhealth.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.protocol.EntityMetadata;
import com.loohp.holomobhealth.utils.NMSUtils;

public class RangeModule {
	
	private static Map<Player, List<Entity>> upcomming = new HashMap<>();
	private static Map<Player, List<Entity>> current = new HashMap<>();
	private static Queue<Player> updateQueue = new LinkedList<>();
	
	protected static int playersPerTick = 1;
	protected static int rate = 20;
	protected static double distance = 15;
	
	public static void reloadNumbers() {
		rate = HoloMobHealth.getConfiguration().getInt("Options.Range.UpdateRate");
		distance = HoloMobHealth.getConfiguration().getDouble("Options.Range.Distance");
	}
	
	public static boolean isEntityInRangeOfPlayer(Player player, Entity entity) {
		if (Bukkit.isPrimaryThread()) {
			Location playerLocation = player.getLocation();
			Location entityLocation = entity.getLocation();
			if (!playerLocation.getWorld().equals(entityLocation.getWorld())) {
				return false;
			}
			double diffX = Math.abs(playerLocation.getX() - entityLocation.getX());
			double y = playerLocation.getY() - entityLocation.getY();
			double diffY = Math.abs(y);
			double diffZ = Math.abs(playerLocation.getZ() - entityLocation.getZ());
			double height = NMSUtils.getEntityHeight(y > 0 ? entity : player);
			double halfWidth = NMSUtils.getEntityWidth(entity) / 2;
			return diffX < (distance + halfWidth) && diffZ < (distance + halfWidth) && diffY < (distance + height);
		} else {
			List<Entity> nearby = current.get(player);
			return nearby == null ? false : nearby.contains(entity);
		}
	}
	
	public static void run() {
		for (World world : Bukkit.getWorlds()) {
			if (!HoloMobHealth.disabledWorlds.contains(world.getName())) {
				for (Player player : world.getPlayers()) {
					updateQueue.add(player);
				}
			}
		}
		playersPerTick = updateQueue.size() / rate;
		Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> getEntitiesInRange(), 1);
	}
	
	private static void getEntitiesInRange() {
		int count = 0;
		while (!updateQueue.isEmpty()) {
			Player player = updateQueue.poll();
			upcomming.put(player, player.getNearbyEntities(distance, distance, distance));
			count++;
			if (count >= playersPerTick) {
				break;
			}
		}
		if (updateQueue.isEmpty()) {
			Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> compareEntities());
		} else {
			Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> getEntitiesInRange(), 1);
		}
	}

	private static void compareEntities() {
		for (Entry<Player, List<Entity>> entry : upcomming.entrySet()) {
			Player player = entry.getKey();
			List<Entity> last = current.get(player);
			if (last == null) {
				List<Entity> now1 = new ArrayList<>(entry.getValue());
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () ->  {
					for (Entity entity : now1) {
						EntityMetadata.updateEntity(player, entity);
					}
				}, 1);
			} else {
				List<Entity> last1 = new ArrayList<>(last);
				List<Entity> now1 = new ArrayList<>(entry.getValue());
				List<Entity> now2 = new ArrayList<>(entry.getValue());
				now1.removeAll(last1);
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () ->  {
					for (Entity entity : now1) {
						EntityMetadata.updateEntity(player, entity);
					}
				}, 1);
				last1.removeAll(now2);
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () ->  {
					for (Entity entity : last1) {
						EntityMetadata.updateEntity(player, entity);
					}
				}, 1);
			}
		}
		Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
			current = upcomming;
			upcomming = new HashMap<>();
			Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> run(), 1);
		}, 1);
	}

}
