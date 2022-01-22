package com.loohp.holomobhealth.utils;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import org.bukkit.entity.Entity;

public class ShopkeepersUtils {

    public static boolean isShopkeeper(Entity entity) {
        return ShopkeepersAPI.getShopkeeperRegistry().isShopkeeper(entity);
    }

}
