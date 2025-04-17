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

package com.loohp.holomobhealth.api;

import com.loohp.holomobhealth.registries.CustomPlaceholderScripts;

public class HoloMobHealthAPI {

    /**
     * Register a class to be used in custom placeholder scripts
     *
     * @param The class name
     * @param The class
     */
    public static void registerClassToCustomPlaceholderScript(String identifier, Class<?> clazz) {
        CustomPlaceholderScripts.registerClass(identifier, clazz);
    }

    /**
     * Check if a class has already be registered to be used in custom placeholder scripts
     *
     * @param The class name
     * @return TRUE/FALSE
     */
    public static boolean containsClassInCustomPlaceholderScript(String identifier) {
        return CustomPlaceholderScripts.containsClass(identifier);
    }

    /**
     * Check if a class has already be registered to be used in custom placeholder scripts
     *
     * @param The class
     * @return TRUE/FALSE
     */
    public static boolean containsClassInCustomPlaceholderScript(Class<?> clazz) {
        return CustomPlaceholderScripts.containsClass(clazz);
    }

}
