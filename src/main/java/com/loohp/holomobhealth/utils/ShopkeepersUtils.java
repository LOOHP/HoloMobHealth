package com.loohp.holomobhealth.utils;

import org.bukkit.entity.Entity;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;

public class ShopkeepersUtils {
	
	public static boolean isShopkeeper(Entity entity) {
		return ShopkeepersAPI.getShopkeeperRegistry().isShopkeeper(entity);
	}

}
