package com.loohp.holomobhealth.utils;

import com.loohp.holomobhealth.HoloMobHealth;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class BoundingBoxUtils {

    private static Class<?> craftEntityClass;
    private static Class<?> nmsEntityClass;
    private static Method craftEntityGetHandlerMethod;
    private static Class<?> nmsAxisAlignedBBClass;
    private static Method nmsEntityGetBoundingBoxMethod;
    private static List<Field> nmsAxisAlignedBBFields;

    private static void _init_() {
        try {
            craftEntityClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
            nmsEntityClass = NMSUtils.getNMSClass("net.minecraft.server.%s.Entity", "net.minecraft.world.entity.Entity");
            craftEntityGetHandlerMethod = craftEntityClass.getMethod("getHandle");
            nmsAxisAlignedBBClass = NMSUtils.getNMSClass("net.minecraft.server.%s.AxisAlignedBB", "net.minecraft.world.phys.AxisAlignedBB");
            nmsEntityGetBoundingBoxMethod = nmsEntityClass.getMethod("getBoundingBox");
            nmsAxisAlignedBBFields = Arrays.asList(nmsAxisAlignedBBClass.getFields());
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static BoundingBox getBoundingBox(Entity entity) {
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_14)) {
            org.bukkit.util.BoundingBox bukkitBox = entity.getBoundingBox();
            return BoundingBox.of(bukkitBox.getMin(), bukkitBox.getMax());
        } else {
            if (craftEntityClass == null) {
                _init_();
            }
            try {
                Object craftEntityObject = craftEntityClass.cast(entity);
                Object nmsEntityObject = craftEntityGetHandlerMethod.invoke(craftEntityObject);
                Object axisAlignedBBObject = nmsEntityGetBoundingBoxMethod.invoke(nmsEntityObject);
                double[] values = nmsAxisAlignedBBFields.stream().mapToDouble(field -> {
                    try {
                        return field.getDouble(axisAlignedBBObject);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }).toArray();
                Location min = new Location(entity.getWorld(), values[0], values[1], values[2]);
                Location max = new Location(entity.getWorld(), values[3], values[4], values[5]);
                return BoundingBox.of(min, max);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
