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

package com.loohp.holomobhealth.placeholderapi;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.database.Database;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public String getAuthor() {
        return String.join(", ", HoloMobHealth.plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "holomobhealth";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getRequiredPlugin() {
        return HoloMobHealth.plugin.getName();
    }

    @Override
    public String onRequest(OfflinePlayer offlineplayer, String identifier) {

        if (identifier.equals("displaytoggle")) {
            if (offlineplayer.isOnline()) {
                return HoloMobHealth.playersEnabled.contains(offlineplayer.getPlayer()) ? "enabled" : "disabled";
            } else {
                return Database.getPlayerInfo(offlineplayer.getUniqueId()).getOrDefault("display", true) ? "enabled" : "disabled";
            }
        }

        return null;
    }

}
