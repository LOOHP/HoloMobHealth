package com.loohp.holomobhealth.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import com.loohp.holomobhealth.HoloMobHealth;

public class LegacyPlaceholdersConverter {
	
	private static Map<String, String> legacyPlaceholders = new HashMap<String, String>();
	
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
		FileConfiguration config = HoloMobHealth.plugin.getConfig();		
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
		
	}

}
