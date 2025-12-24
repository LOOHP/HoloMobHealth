/*
 * This file is part of HoloMobHealth2.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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

package com.loohp.holomobhealth.holders;

public class DataWatcherFields {

    private final DataWatcherField byteField;
    private final DataWatcherField customNameField;
    private final DataWatcherField customNameVisibleField;
    private final DataWatcherField silentField;
    private final DataWatcherField noGravityField;
    private final DataWatcherField armorStandByteField;

    public DataWatcherFields(DataWatcherField byteField, DataWatcherField customNameField, DataWatcherField customNameVisibleField, DataWatcherField silentField, DataWatcherField noGravityField, DataWatcherField armorStandByteField) {
        this.byteField = byteField;
        this.customNameField = customNameField;
        this.customNameVisibleField = customNameVisibleField;
        this.silentField = silentField;
        this.noGravityField = noGravityField;
        this.armorStandByteField = armorStandByteField;
    }

    public DataWatcherField getByteField() {
        return byteField;
    }

    public DataWatcherField getCustomNameField() {
        return customNameField;
    }

    public DataWatcherField getCustomNameVisibleField() {
        return customNameVisibleField;
    }

    public boolean hasSilentField() {
        return silentField != null;
    }

    public DataWatcherField getSilentField() {
        return silentField;
    }

    public boolean hasNoGravityField() {
        return noGravityField != null;
    }

    public DataWatcherField getNoGravityField() {
        return noGravityField;
    }

    public DataWatcherField getArmorStandByteField() {
        return armorStandByteField;
    }
}
