package com.loohp.holomobhealth.holders;

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
import com.loohp.holomobhealth.protocol.ArmorStandPacket;
import com.loohp.holomobhealth.utils.NMSUtils;

public class MultilineStands {
	
	private static final Vector SPACING = new Vector(0, 0.27, 0);
	
	private List<HoloMobArmorStand> stands;
	private Location location;
	private Entity entity;
	private UUID uuid;
	private int id;
	private int offset;
	private int gctask;
	
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
		stands = new ArrayList<>();
		for (int i = 0; i < HoloMobHealth.displayText.size(); i++) {
			HoloMobArmorStand stand = new HoloMobArmorStand(standloc, EntityType.ARMOR_STAND);
			stands.add(stand);
		}
		Location base = entity.getLocation().add(0, NMSUtils.getEntityHeight(entity), 0);
		for (int i = 0; i < stands.size(); i++) {
			HoloMobArmorStand stand = stands.get(i);
			stand.setLocation(base.clone());
			base.add(SPACING);
		}
		Collections.reverse(stands);
		this.entity = entity;
		this.uuid = entity.getUniqueId();
		this.id = entity.getEntityId();
		gc();
	}
	
	private void gc() {
		gctask = Bukkit.getScheduler().runTaskTimerAsynchronously(HoloMobHealth.plugin, () -> {
			if (!entity.isValid() || !entity.getUniqueId().equals(uuid)) {
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
	
	public void setLocation(Location location) {
		Location base = entity.getLocation().add(0, NMSUtils.getEntityHeight(entity), 0);
		for (int i = stands.size() - 1; i >= 0; i--) {
			HoloMobArmorStand stand = stands.get(i);
			stand.setLocation(base.clone());
			base.add(SPACING);
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
		for (HoloMobArmorStand stand : stands) {
			Bukkit.getScheduler().runTask(HoloMobHealth.plugin, () -> ArmorStandPacket.removeArmorStand(Bukkit.getOnlinePlayers(), stand, true, false));
		}
		Bukkit.getScheduler().cancelTask(gctask);
	}

}
