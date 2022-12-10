/*
 * This file is part of HoloMobHealth2.
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

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.loohp.holomobhealth.HoloMobHealth;

import java.util.ArrayList;
import java.util.List;

public class DataWatcherUtils {

    public static WrappedDataWatcher fromDataValueList(List<WrappedDataValue> dataValues) {
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        for (WrappedDataValue dataValue : dataValues) {
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(dataValue.getIndex(), dataValue.getSerializer()), dataValue.getRawValue());
        }
        return watcher;
    }

    public static List<WrappedDataValue> toDataValueList(WrappedDataWatcher wrappedDataWatcher) {
        List<WrappedWatchableObject> watchableObjectList = wrappedDataWatcher.getWatchableObjects();
        List<WrappedDataValue> wrappedDataValues = new ArrayList<>(watchableObjectList.size());
        for (WrappedWatchableObject wrappedWatchableObject : wrappedDataWatcher.getWatchableObjects()) {
            WrappedDataWatcher.WrappedDataWatcherObject wrappedDataWatcherObject = wrappedWatchableObject.getWatcherObject();
            wrappedDataValues.add(new WrappedDataValue(wrappedDataWatcherObject.getIndex(), wrappedDataWatcherObject.getSerializer(), wrappedWatchableObject.getRawValue()));
        }
        return wrappedDataValues;
    }

    public static WrappedDataWatcher fromMetadataPacket(PacketContainer packet) {
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
            List<WrappedDataValue> data = packet.getDataValueCollectionModifier().read(0);
            return fromDataValueList(data);
        } else {
            List<WrappedWatchableObject> data = packet.getWatchableCollectionModifier().read(0);
            return new WrappedDataWatcher(data);
        }
    }

    public static void writeMetadataPacket(PacketContainer packet, WrappedDataWatcher watcher) {
        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
            packet.getDataValueCollectionModifier().write(0, toDataValueList(watcher));
        } else {
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        }
    }

}
