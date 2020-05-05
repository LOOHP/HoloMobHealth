package com.loohp.holomobhealth.Holders;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

public class HoloMobCache {
	
	String customName;
	boolean custonNameVisible;
	Set<Player> players;
	
	public HoloMobCache(String customName, boolean custonNameVisible) {
		this.customName = customName;
		this.custonNameVisible = custonNameVisible;
	}
	
	public HoloMobCache(String customName, boolean custonNameVisible, Set<Player> players) {
		this(customName, custonNameVisible);
		this.players = new HashSet<Player>(players);		
	}
	
	public String getCustomName() {
		return customName;
	}
	
	public boolean getCustomNameVisible() {
		return custonNameVisible;
	}
	
	public Set<Player> getPlayers() {
		return players;
	}

}
