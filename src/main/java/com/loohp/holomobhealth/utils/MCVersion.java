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

public enum MCVersion {

    V1_19_4("1.19.4", 20),
    V1_19_3("1.19.3", 19),
    V1_19("1.19", 18),
    V1_18_2("1.18.2", 17),
    V1_18("1.18", 16),
    V1_17("1.17", 15),
    V1_16_4("1.16.4", 14),
    V1_16_2("1.16.2", 13),
    V1_16("1.16", 12),
    V1_15("1.15", 11),
    V1_14("1.14", 10),
    V1_13_1("1.13.1", 9),
    V1_13("1.13", 8),
    V1_12("1.12", 7),
    V1_11("1.11", 6),
    V1_10("1.10", 5),
    V1_9_4("1.9.4", 4),
    V1_9("1.9", 3),
    V1_8_4("1.8.4", 2),
    V1_8_3("1.8.3", 1),
    V1_8("1.8", 0),
    UNSUPPORTED("Unsupported", -1);

    public static MCVersion fromPackageName(String packageName) {
        if (packageName.contains("1_19_R3")) {
            return V1_19_4;
        } else if (packageName.contains("1_19_R2")) {
            return V1_19_3;
        } else if (packageName.contains("1_19_R1")) {
            return V1_19;
        } else if (packageName.contains("1_18_R2")) {
            return V1_18_2;
        } else if (packageName.contains("1_18_R1")) {
            return V1_18;
        } else if (packageName.contains("1_17_R1")) {
            return V1_17;
        } else if (packageName.contains("1_16_R3")) {
            return V1_16_4;
        } else if (packageName.contains("1_16_R2")) {
            return V1_16_2;
        } else if (packageName.contains("1_16_R1")) {
            return V1_16;
        } else if (packageName.contains("1_15_R1")) {
            return V1_15;
        } else if (packageName.contains("1_14_R1")) {
            return V1_14;
        } else if (packageName.contains("1_13_R2")) {
            return V1_13_1;
        } else if (packageName.contains("1_13_R1")) {
            return V1_13;
        } else if (packageName.contains("1_12_R1")) {
            return V1_12;
        } else if (packageName.contains("1_11_R1")) {
            return V1_11;
        } else if (packageName.contains("1_10_R1")) {
            return V1_10;
        } else if (packageName.contains("1_9_R2")) {
            return V1_9_4;
        } else if (packageName.contains("1_9_R1")) {
            return V1_9;
        } else if (packageName.contains("1_8_R3")) {
            return V1_8_4;
        } else if (packageName.contains("1_8_R2")) {
            return V1_8_3;
        } else if (packageName.contains("1_8_R1")) {
            return V1_8;
        } else {
            return UNSUPPORTED;
        }
    }

    public static MCVersion fromNumber(int number) {
        for (MCVersion version : values()) {
            if (version.shortNum == number) {
                return version;
            }
        }
        return UNSUPPORTED;
    }
    private final String name;
    private final int shortNum;

    MCVersion(String name, int shortNum) {
        this.name = name;
        this.shortNum = shortNum;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getNumber() {
        return shortNum;
    }

    public int compareWith(MCVersion version) {
        return this.shortNum - version.shortNum;
    }

    public boolean isOlderThan(MCVersion version) {
        return compareWith(version) < 0;
    }

    public boolean isOlderOrEqualTo(MCVersion version) {
        return compareWith(version) <= 0;
    }

    public boolean isNewerThan(MCVersion version) {
        return compareWith(version) > 0;
    }

    public boolean isNewerOrEqualTo(MCVersion version) {
        return compareWith(version) >= 0;
    }

    public boolean isBetweenInclusively(MCVersion v1, MCVersion v2) {
        int difference = v1.compareWith(v2);
        if (difference == 0) {
            return this.equals(v1);
        } else if (difference < 0) {
            return this.isNewerOrEqualTo(v1) && this.isOlderOrEqualTo(v2);
        } else {
            return this.isNewerOrEqualTo(v2) && this.isOlderOrEqualTo(v1);
        }
    }

    public boolean isLegacy() {
        return isOlderOrEqualTo(V1_12);
    }

    public boolean isOld() {
        return isOlderOrEqualTo(V1_8_4);
    }

    public boolean isSupported() {
        return this.shortNum >= 0;
    }

    public boolean isLegacyRGB() {
        return isOlderThan(MCVersion.V1_16);
    }

}
