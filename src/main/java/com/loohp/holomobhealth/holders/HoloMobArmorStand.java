package com.loohp.holomobhealth.holders;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import com.loohp.holomobhealth.utils.EntityUtils;

public class HoloMobArmorStand {
	
	private int id;
	private UUID uuid;
	private EntityType type;
	private Location location;
	
	private transient Future<Integer> entityIdFuture;
	
	public HoloMobArmorStand(Location location, EntityType type) {
		this.entityIdFuture = EntityUtils.getNextEntityId();
		this.id = Integer.MIN_VALUE;
		this.uuid = UUID.randomUUID();
		this.location = location.clone();
		this.type = type;
	}
	
	public EntityType getType() {
		return type;
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
	
	public final int getEntityId() {
		if (id != Integer.MIN_VALUE) {
			return id;
		}
		try {
			return id = entityIdFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return -1;
		}
	}

}
