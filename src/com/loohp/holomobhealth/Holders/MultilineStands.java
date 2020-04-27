package com.loohp.holomobhealth.Holders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Protocol.ArmorStandPacket;

public class MultilineStands {
	
	List<HoloMobArmorStand> stands;
	List<HoloMobArmorStand> eachentity;
	Location location;
	Entity entity;
	UUID uuid;
	int id;
	int offset;
	int gctask;
	
	public MultilineStands(Entity entity) {
		Location location = entity.getLocation().clone();
		offset = HoloMobHealth.armorStandYOffset;
		if (entity.getCustomName() != null) {
			for (Entry<String, Integer> entry : HoloMobHealth.specialNameOffset.entrySet()) {
				if (entity.getCustomName().matches(entry.getKey())) {
					offset = offset + entry.getValue();
					break;
				}
			}
		}
		for (Entry<EntityType, Integer> entry : HoloMobHealth.specialTypeOffset.entrySet()) {
			if (entity.getType().equals(entry.getKey())) {
				offset = offset + entry.getValue();
				break;
			}
		}
		this.location = location.clone();
		Location standloc = location.clone();
		stands = new ArrayList<HoloMobArmorStand>();
		eachentity = new ArrayList<HoloMobArmorStand>();
		int mountId = entity.getEntityId();
		for (int i = 0; i < HoloMobHealth.DisplayText.size() + offset; i++) {
			HoloMobArmorStand stand = new HoloMobArmorStand(standloc, EntityType.ARMOR_STAND, mountId);
			mountId = stand.getEntityId();
			if (offset - 1 < i) {
				stands.add(stand);
			}
			HoloMobArmorStand spacing = new HoloMobArmorStand(standloc, EntityType.RABBIT, mountId);
			eachentity.add(stand);
			eachentity.add(spacing);
			mountId = spacing.getEntityId();
		}
		Collections.reverse(stands);
		Collections.reverse(eachentity);
		this.entity = entity;
		this.uuid = entity.getUniqueId();
		this.id = entity.getEntityId();
		gc();
	}
	
	private void gc() {
		gctask = Bukkit.getScheduler().runTaskTimerAsynchronously(HoloMobHealth.plugin, () -> {
			if (!entity.isValid()) {
				for (HoloMobArmorStand stand : stands) {
					Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> ArmorStandPacket.removeArmorStand(Bukkit.getOnlinePlayers(), stand, true));
				}
				remove();
			}
		}, 0, 15).getTaskId();
	}
	
	public HoloMobArmorStand getStand(int line) {
		return stands.get(line);
	}
	
	public List<HoloMobArmorStand> getStands() {
		return stands;
	}
	
	public List<HoloMobArmorStand> getAllRelatedEntities() {
		return eachentity;
	}
	
	public void setLocation(Location location) {
		Vector vector = location.toVector().subtract(this.location.clone().toVector());
		this.location = location.clone();
		for (HoloMobArmorStand stand : stands) {
			stand.teleport(stand.getLocation().add(vector));
		}
	}
	
	public Location getLocation() {
		return location.clone();
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public int getEntityId() {
		return id;
	}
	
	public void remove() {
		Bukkit.getScheduler().cancelTask(gctask);
	}

}
