package com.loohp.holomobhealth.Holders;

public class HoloMobCache {
	
	String customName;
	boolean custonNameVisible;
	
	public HoloMobCache(String customName, boolean custonNameVisible) {
		this.customName = customName;
		this.custonNameVisible = custonNameVisible;
	}
	
	public String getCustomName() {
		return customName;
	}
	
	public boolean getCustomNameVisible() {
		return custonNameVisible;
	}

}
