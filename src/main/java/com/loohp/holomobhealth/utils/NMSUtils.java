/*
 * This file is part of HoloMobHealth.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.holomobhealth.utils;

import com.loohp.holomobhealth.HoloMobHealth;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NMSUtils {

    private static Class<?> craftWorldClass;
    private static Class<?> craftEntityClass;
    private static Class<?> nmsEntityClass;
    private static Class<?> nmsWorldServerClass;
    private static Method craftWorldGetHandleMethod;
    private static Method nmsWorldServerGetEntityByIDMethod;
    private static Method nmsWorldServerGetEntityByUUIDMethod;
    private static Method nmsEntityGetBukkitEntityMethod;
    private static Method nmsEntityGetUniqueIDMethod;
    private static Method nmsEntityGetBoundingBox;
    private static Method nmsEntityGetHandle;
    private static Class<?> nmsAxisAlignedBBClass;
    private static Field[] nmsAxisAlignedBBFields;

    private static Field nmsWorldEntityManagerField;
    private static Method nmsEntityManagerGetEntityGetterMethod;
    private static Class<?> nmsLevelEntityGetterClass;

    private static Method nmsGetEntityLookUpMethod;

    private static Method nmsLevelEntityGetterGetEntityByIDMethod;
    private static Method nmsLevelEntityGetterGetEntityByUUIDMethod;

    static {
        try {
            craftWorldClass = getNMSClass("org.bukkit.craftbukkit.%s.CraftWorld");
            craftEntityClass = getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
            nmsEntityClass = getNMSClass("net.minecraft.server.%s.Entity", "net.minecraft.world.entity.Entity");
            nmsWorldServerClass = getNMSClass("net.minecraft.server.%s.WorldServer", "net.minecraft.server.level.WorldServer");
            craftWorldGetHandleMethod = craftWorldClass.getMethod("getHandle");
            nmsWorldServerGetEntityByIDMethod = reflectiveLookup(Method.class, () -> {
                return nmsWorldServerClass.getMethod("getEntity", int.class);
            }, () -> {
                return nmsWorldServerClass.getMethod("a", int.class);
            });
            nmsWorldServerGetEntityByUUIDMethod = reflectiveLookup(Method.class, () -> {
                return nmsWorldServerClass.getMethod("getEntity", UUID.class);
            }, () -> {
                return nmsWorldServerClass.getMethod("a", UUID.class);
            });
            nmsEntityGetBukkitEntityMethod = nmsEntityClass.getMethod("getBukkitEntity");
            nmsEntityGetUniqueIDMethod = reflectiveLookup(Method.class, () -> {
                return nmsEntityClass.getMethod("getUniqueID");
            }, () -> {
                Method method = nmsEntityClass.getMethod("cm");
                if (!method.getReturnType().equals(UUID.class)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            }, () -> {
                Method method = nmsEntityClass.getMethod("cp");
                if (!method.getReturnType().equals(UUID.class)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            }, () -> {
                Method method = nmsEntityClass.getMethod("co");
                if (!method.getReturnType().equals(UUID.class)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            }, () -> {
                Method method = nmsEntityClass.getMethod("cs");
                if (!method.getReturnType().equals(UUID.class)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            });
            nmsAxisAlignedBBClass = getNMSClass("net.minecraft.server.%s.AxisAlignedBB", "net.minecraft.world.phys.AxisAlignedBB");
            nmsEntityGetBoundingBox = reflectiveLookup(Method.class, () -> {
                return nmsEntityClass.getMethod("getBoundingBox");
            }, () -> {
                Method method = nmsEntityClass.getMethod("cw");
                if (!method.getReturnType().equals(nmsAxisAlignedBBClass)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            }, () -> {
                Method method = nmsEntityClass.getMethod("cx");
                if (!method.getReturnType().equals(nmsAxisAlignedBBClass)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            }, () -> {
                Method method = nmsEntityClass.getMethod("cA");
                if (!method.getReturnType().equals(nmsAxisAlignedBBClass)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            }, () -> {
                Method method = nmsEntityClass.getMethod("cz");
                if (!method.getReturnType().equals(nmsAxisAlignedBBClass)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            }, () -> {
                Method method = nmsEntityClass.getMethod("cD");
                if (!method.getReturnType().equals(nmsAxisAlignedBBClass)) {
                    throw new NoSuchMethodException("Incorrect return type");
                }
                return method;
            });
            nmsEntityGetHandle = craftEntityClass.getMethod("getHandle");
            nmsAxisAlignedBBFields = Arrays.stream(nmsAxisAlignedBBClass.getFields()).filter(each -> each.getType().equals(double.class) && !Modifier.isStatic(each.getModifiers())).toArray(Field[]::new);
            if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
                try {
                    nmsGetEntityLookUpMethod = nmsWorldServerClass.getMethod("getEntityLookup");
                    nmsLevelEntityGetterGetEntityByIDMethod = nmsGetEntityLookUpMethod.getReturnType().getMethod("get", int.class);
                    nmsLevelEntityGetterGetEntityByUUIDMethod = nmsGetEntityLookUpMethod.getReturnType().getMethod("get", UUID.class);
                } catch (NoSuchMethodException e) {
                    if (HoloMobHealth.version.equals(MCVersion.V1_17)) {
                        nmsWorldEntityManagerField = nmsWorldServerClass.getDeclaredField("G");
                    } else if (HoloMobHealth.version.equals(MCVersion.V1_18)) {
                        nmsWorldEntityManagerField = nmsWorldServerClass.getDeclaredField("P");
                    } else if (HoloMobHealth.version.equals(MCVersion.V1_18_2)) {
                        nmsWorldEntityManagerField = nmsWorldServerClass.getDeclaredField("O");
                    } else if (HoloMobHealth.version.equals(MCVersion.V1_19)) {
                        nmsWorldEntityManagerField = nmsWorldServerClass.getDeclaredField("P");
                    }
                    nmsEntityManagerGetEntityGetterMethod = nmsWorldEntityManagerField.getType().getMethod("d");
                    nmsLevelEntityGetterClass = getNMSClass("net.minecraft.world.level.entity.LevelEntityGetterAdapter");
                    nmsLevelEntityGetterGetEntityByIDMethod = nmsLevelEntityGetterClass.getMethod("a", int.class);
                    nmsLevelEntityGetterGetEntityByUUIDMethod = nmsLevelEntityGetterClass.getMethod("a", UUID.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UUID getEntityUUIDFromID(World world, int id) {
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
            try {
                Object craftWorldObject = craftWorldClass.cast(world);
                Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
                Object nmsEntityObject;
                if (nmsGetEntityLookUpMethod == null) {
                    nmsWorldEntityManagerField.setAccessible(true);
                    Object nmsEntityManagerObject = nmsWorldEntityManagerField.get(nmsWorldServerObject);
                    Object nmsLevelEntityGetterObject = nmsEntityManagerGetEntityGetterMethod.invoke(nmsEntityManagerObject);
                    nmsEntityObject = nmsLevelEntityGetterGetEntityByIDMethod.invoke(nmsLevelEntityGetterObject, id);
                } else {
                    Object nmsEntityLookup = nmsGetEntityLookUpMethod.invoke(nmsWorldServerObject);
                    nmsEntityObject = nmsLevelEntityGetterGetEntityByIDMethod.invoke(nmsEntityLookup, id);
                }
                if (nmsEntityObject == null) {
                    return null;
                }
                return (UUID) nmsEntityGetUniqueIDMethod.invoke(nmsEntityObject);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        } else if (HoloMobHealth.version.isOld()) {
            List<Entity> entities = world.getEntities();
            for (Entity entity : entities) {
                if (entity.getEntityId() == id) {
                    return entity.getUniqueId();
                }
            }
            return null;
        } else {
            try {
                Object craftWorldObject = craftWorldClass.cast(world);
                Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
                Object nmsEntityObject = nmsWorldServerGetEntityByIDMethod.invoke(nmsWorldServerObject, id);
                if (nmsEntityObject == null) {
                    return null;
                }
                return (UUID) nmsEntityGetUniqueIDMethod.invoke(nmsEntityObject);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Entity getEntityFromUUID(UUID uuid) {
        Entity entity = null;
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_17)) {
            for (World world : Bukkit.getWorlds()) {
                try {
                    Object craftWorldObject = craftWorldClass.cast(world);
                    Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
                    Object nmsEntityObject;
                    if (nmsGetEntityLookUpMethod == null) {
                        nmsWorldEntityManagerField.setAccessible(true);
                        Object nmsEntityManagerObject = nmsWorldEntityManagerField.get(nmsWorldServerObject);
                        Object nmsLevelEntityGetterObject = nmsEntityManagerGetEntityGetterMethod.invoke(nmsEntityManagerObject);
                        nmsEntityObject = nmsLevelEntityGetterGetEntityByUUIDMethod.invoke(nmsLevelEntityGetterObject, uuid);
                    } else {
                        Object nmsEntityLookup = nmsGetEntityLookUpMethod.invoke(nmsWorldServerObject);
                        nmsEntityObject = nmsLevelEntityGetterGetEntityByUUIDMethod.invoke(nmsEntityLookup, uuid);
                    }
                    if (nmsEntityObject == null) {
                        continue;
                    }
                    return (Entity) nmsEntityGetBukkitEntityMethod.invoke(nmsEntityObject);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_12)) {
            entity = Bukkit.getEntity(uuid);
        } else {
            for (World world : Bukkit.getWorlds()) {
                try {
                    Object craftWorldObject = craftWorldClass.cast(world);
                    Object nmsWorldServerObject = craftWorldGetHandleMethod.invoke(craftWorldObject);
                    Object nmsEntityObject = nmsWorldServerGetEntityByUUIDMethod.invoke(nmsWorldServerObject, uuid);
                    if (nmsEntityObject == null) {
                        continue;
                    }
                    return (Entity) nmsEntityGetBukkitEntityMethod.invoke(nmsEntityObject);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        if (entity == null) {
            entity = StackerUtils.getEntityFromStack(uuid);
        }
        return entity;
    }

    public static double getEntityHeight(Entity entity) {
        try {
            Object craftEntityObject = craftEntityClass.cast(entity);
            Object nmsEntityObject = nmsEntityGetHandle.invoke(craftEntityObject);
            Object aabbObj = nmsEntityGetBoundingBox.invoke(nmsEntityObject);
            double minY = nmsAxisAlignedBBFields[1].getDouble(aabbObj);
            double maxY = nmsAxisAlignedBBFields[4].getDouble(aabbObj);
            return maxY - minY;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static double getEntityWidth(Entity entity) {
        try {
            Object craftEntityObject = craftEntityClass.cast(entity);
            Object nmsEntityObject = nmsEntityGetHandle.invoke(craftEntityObject);
            Object aabbObj = nmsEntityGetBoundingBox.invoke(nmsEntityObject);
            double minX = nmsAxisAlignedBBFields[0].getDouble(aabbObj);
            double maxX = nmsAxisAlignedBBFields[3].getDouble(aabbObj);
            return maxX - minX;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Class<?> getNMSClass(String path, String... paths) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        ClassNotFoundException error = null;
        try {
            return Class.forName(path.replace("%s", version));
        } catch (ClassNotFoundException e) {
            error = e;
        }
        for (String classpath : paths) {
            try {
                return Class.forName(classpath.replace("%s", version));
            } catch (ClassNotFoundException e) {
                error = e;
            }
        }
        throw error;
    }

    @SafeVarargs
    public static <T extends AccessibleObject> T reflectiveLookup(Class<T> lookupType, ReflectionLookupSupplier<T> methodLookup, ReflectionLookupSupplier<T>... methodLookups) throws ReflectiveOperationException {
        ReflectiveOperationException error = null;
        try {
            return methodLookup.lookup();
        } catch (ReflectiveOperationException e) {
            error = e;
        }
        for (ReflectionLookupSupplier<T> supplier : methodLookups) {
            try {
                return supplier.lookup();
            } catch (ReflectiveOperationException e) {
                error = e;
            }
        }
        throw error;
    }

    @FunctionalInterface
    public interface ReflectionLookupSupplier<T> {

        T lookup() throws ReflectiveOperationException;

    }

}
