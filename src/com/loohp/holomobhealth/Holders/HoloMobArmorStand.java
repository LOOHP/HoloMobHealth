package com.loohp.holomobhealth.Holders;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class HoloMobArmorStand {
	
	int id;
	UUID uuid;
	EntityType type;
	Location location;
	String customName;
	boolean custonNameVisible;
	int mountId;
	
	public HoloMobArmorStand(Location location, EntityType type, int mountId) {
		this.id = (int) (Math.random() * Integer.MAX_VALUE);
		this.uuid = UUID.randomUUID();
		this.location = location.clone();
		this.customName = "";
		this.custonNameVisible = false;
		this.mountId = mountId;
		this.type = type;
	}
	
	public EntityType getType() {
		return type;
	}
	
	public int getMountId() {
		return mountId;
	}
	
	public void setMountId(int mountId) {
		this.mountId = mountId;
	}
	
	public void setRotation(float yaw, float pitch) {
		teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
	}
	
	public World getWorld() {
		return location.getWorld();
	}
	
	public void teleport(Location location) {
		this.location = location.clone();
	}
	
	public void teleport(World world, double x, double y, double z) {
		this.location = new Location(world, x, y, z, location.getYaw(), location.getPitch());
	}
	
	public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
		this.location = new Location(world, x, y, z, yaw, pitch);
	}
	
	public void setLocation(Location location) {
		this.location = location.clone();
	}
	public Location getLocation() {
		return location.clone();
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public int getEntityId() {
		return id;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}
	public String getCustomName() {
		return customName;
	}
	
	public void setCustomNameVisible(boolean bool) {
		this.custonNameVisible = bool;
	}	
	public boolean isCustomNameVisible() {
		return custonNameVisible;
	}

}
