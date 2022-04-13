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
import com.loohp.holomobhealth.registries.CustomPlaceholderScripts;
import com.loohp.holomobhealth.registries.DisplayTextCacher;
import com.loohp.holomobhealth.registries.DisplayTextCacher.HealthFormatData;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map.Entry;

public class ParsePlaceholders {

    public static Component parse(LivingEntity entity, String text, double healthChange) {
        return parse(null, entity, text, healthChange);
    }

    public static Component parse(Player player, LivingEntity entity, String text) {
        return parse(player, entity, text, 0);
    }

    @SuppressWarnings("deprecation")
    public static Component parse(Player player, LivingEntity entity, String text, double healthChange) {
        double health = entity.getHealth();
        double maxHealth = 0.0;
        try {
            if (!HoloMobHealth.version.isLegacy()) {
                maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            } else {
                maxHealth = entity.getMaxHealth();
            }
        } catch (Throwable ignored) {
        }
        double percentage = (health / maxHealth) * 100;

        int heartScale = HoloMobHealth.dynamicScale ? (Math.ceil(maxHealth / 2) > HoloMobHealth.heartScale ? HoloMobHealth.heartScale : (int) Math.ceil(maxHealth / 2)) : HoloMobHealth.heartScale;

        for (Entry<String, HealthFormatData> entry : DisplayTextCacher.getDecimalFormatMapping().entrySet()) {
            HealthFormatData data = entry.getValue();
            switch (data.getType()) {
                case HEALTH:
                    text = text.replace(entry.getKey(), String.valueOf(data.getFormatter().format(health)));
                    break;
                case MAXHEALTH:
                    text = text.replace(entry.getKey(), String.valueOf(data.getFormatter().format(maxHealth)));
                    break;
                case PERCENTAGEHEALTH:
                    text = text.replace(entry.getKey(), String.valueOf(data.getFormatter().format(percentage)));
                    break;
                case INDICATOR:
                    text = text.replace(entry.getKey(), String.valueOf(data.getFormatter().format(Math.abs(healthChange))));
                    break;
            }
        }

        if (text.contains("{DynamicColor}")) {
            String symbol;
            if (percentage < 33.33) {
                symbol = HoloMobHealth.lowColor;
            } else if (percentage < 66.67) {
                symbol = HoloMobHealth.halfColor;
            } else {
                symbol = HoloMobHealth.healthyColor;
            }
            text = text.replace("{DynamicColor}", symbol);
        }
        if (text.contains("{ScaledSymbols}")) {
            StringBuilder symbol = new StringBuilder();
            double healthpercentagescaled = percentage / 100.0 * (double) heartScale;
            int fullhearts = (int) Math.floor(healthpercentagescaled);
            for (int i = 0; i < fullhearts; i++) {
                symbol.append(HoloMobHealth.healthyChar);
            }
            if (fullhearts < heartScale) {
                double leftover = healthpercentagescaled - (double) fullhearts;
                if (leftover >= 0.5) {
                    symbol.append(HoloMobHealth.healthyChar);
                } else {
                    symbol.append(HoloMobHealth.halfChar);
                }
                for (int i = fullhearts + 1; i < heartScale; i++) {
                    symbol.append(HoloMobHealth.emptyChar);
                }
            }
            text = text.replace("{ScaledSymbols}", symbol);
        }

        try {
            text = CustomPlaceholderScripts.runScripts(text, entity, healthChange);
        } catch (Exception e) {
            e.printStackTrace();
        }

        text = ChatColorUtils.translateAlternateColorCodes('&', text);

        if (HoloMobHealth.placeholderAPIHook && player != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        text = ChatColorUtils.translateAlternateColorCodes('&', text);

        String rawName = CustomNameUtils.getMobCustomName(entity);
        Component customName = rawName == null ? Component.empty() : LegacyComponentSerializer.legacySection().deserialize(rawName);

        Component displayComponent = LegacyComponentSerializer.legacySection().deserialize(text);

        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_16)) {
            displayComponent = ComponentFont.parseFont(displayComponent);
        }

        displayComponent = displayComponent.replaceText(TextReplacementConfig.builder().matchLiteral("{Mob_Type}").replacement(Component.translatable(LanguageUtils.getTranslationKey(entity))).build());
        displayComponent = displayComponent.replaceText(TextReplacementConfig.builder().matchLiteral("{Mob_Name}").replacement(customName).build());
        displayComponent = displayComponent.replaceText(TextReplacementConfig.builder().matchLiteral("{Mob_Type_Or_Name}").replacement(customName.equals(Component.empty()) ? Component.translatable(LanguageUtils.getTranslationKey(entity)) : customName).build());

        return displayComponent;
    }

}
