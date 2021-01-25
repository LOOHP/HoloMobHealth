package com.loohp.holomobhealth.Modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Protocol.EntityMetadata;

public class RangeModule {
	
	private static Map<Player, List<Entity>> upcomming = new HashMap<>();
	private static Map<Player, List<Entity>> current = new HashMap<>();
	private static Queue<Player> updateQueue = new LinkedList<>();
	
	protected static int playersPerTick = 1;
	protected static int rate = 20;
	protected static double distance = 15;
	
	public static void reloadNumbers() {
		rate = HoloMobHealth.plugin.getConfig().getInt("Options.Range.UpdateRate");
		distance = HoloMobHealth.plugin.getConfig().getDouble("Options.Range.Distance");
	}
	
	public static boolean isEntityInRangeOfPlayer(Player player, Entity entity) {
		if (Bukkit.isPrimaryThread()) {
			return player.getNearbyEntities(distance, distance, distance).contains(entity);
		} else {
			List<Entity> nearby = current.get(player);
			return nearby == null ? false : nearby.contains(entity);
		}
	}
	
	public static void run() {
		for (World world : Bukkit.getWorlds()) {
			if (!HoloMobHealth.DisabledWorlds.contains(world.getName())) {
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
