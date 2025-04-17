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

package com.loohp.holomobhealth.metrics;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.registries.CustomPlaceholderScripts;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;

public class Charts {

    public static void setup(Metrics metrics) {

        metrics.addCustomChart(new Metrics.SingleLineChart("total_mobs_displaying", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return Bukkit.getWorlds().stream().mapToInt(world -> world.getEntities().size()).sum();
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_custom_placeholder_scripts", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return CustomPlaceholderScripts.getScriptsCount();
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("damage_indicator_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (HoloMobHealth.useDamageIndicator) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("range_module_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (HoloMobHealth.rangeEnabled) {
                    string = "Enabled";
                }
                return string;
            }
        }));
    }

}
