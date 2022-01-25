package com.loohp.holomobhealth.utils;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.yamlconfiguration.YamlConfiguration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LegacyPlaceholdersConverter {

    private static final Map<String, String> legacyPlaceholders = new HashMap<>();

    static {
        legacyPlaceholders.put("{Health_Rounded}", "{Health_0}");
        legacyPlaceholders.put("{Max_Health_Rounded}", "{MaxHealth_0}");
        legacyPlaceholders.put("{Health_1DP}", "{Health_0.0}");
        legacyPlaceholders.put("{Max_Health_1DP}", "{MaxHealth_0.0}");
        legacyPlaceholders.put("{Health_2DP}", "{Health_0.00}");
        legacyPlaceholders.put("{Max_Health_2DP}", "{MaxHealth_0.00}");
        legacyPlaceholders.put("{Health_Rounded_Commas}", "{Health_#,##0}");
        legacyPlaceholders.put("{Max_Health_Rounded_Commas}", "{MaxHealth_#,##0}");
        legacyPlaceholders.put("{Health_1DP_Commas}", "{Health_#,##0.0}");
        legacyPlaceholders.put("{Max_Health_1DP_Commas}", "{MaxHealth_#,##0.0}");
        legacyPlaceholders.put("{Health_2DP_Commas}", "{Health_#,##0.00}");
        legacyPlaceholders.put("{Max_Health_2DP_Commas}", "{MaxHealth_#,##0.00}");
        legacyPlaceholders.put("{Health_Percentage}", "{PercentageHealth_0}");
        legacyPlaceholders.put("{Health_Percentage_1DP}", "{PercentageHealth_0.0}");
        legacyPlaceholders.put("{Health_Percentage_2DP}", "{PercentageHealth_0.00}");
    }

    public static Set<String> getLegacyPlaceholderSet() {
        return new HashSet<>(legacyPlaceholders.keySet());
    }

    public static void convert() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[HoloMobHealth] Translating old placeholders to new placeholders...");

        YamlConfiguration config = HoloMobHealth.getConfiguration();
        List<String> lines = config.getStringList("Display.Text");

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (getLegacyPlaceholderSet().stream().anyMatch(each -> line.contains(each))) {
                String newline = line;
                for (Entry<String, String> entry : legacyPlaceholders.entrySet()) {
                    newline = newline.replace(entry.getKey(), entry.getValue());
                }
                lines.set(i, newline);
            }
        }

        config.set("Display.Text", lines);
        HoloMobHealth.plugin.saveConfig();

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[HoloMobHealth] Translation complete!");
    }

}
