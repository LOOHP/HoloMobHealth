package com.loohp.holomobhealth.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.LivingEntity;

import com.loohp.holomobhealth.HoloMobHealth;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ParsePlaceholders {
	
	public static String parse(LivingEntity entity, String text) {
		double health = entity.getHealth();
		@SuppressWarnings("deprecation")
		double maxhealth = entity.getMaxHealth();
		double percentage = (health / maxhealth) * 100;
		if (text.contains("{Health_Rounded_Commas}")) {
			DecimalFormat formatter = new DecimalFormat("#,##0");
			text = text.replace("{Health_Rounded_Commas}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_Rounded_Commas}")) {
			DecimalFormat formatter = new DecimalFormat("#,##0");
			text = text.replace("{Max_Health_Rounded_Commas}", String.valueOf(formatter.format(maxhealth)));
		}
		if (text.contains("{Health_1DP_Commas}")) {
			DecimalFormat formatter = new DecimalFormat("#,##0.0");
			text = text.replace("{Health_1DP_Commas}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_1DP_Commas}")) {
			DecimalFormat formatter = new DecimalFormat("#,##0.0");
			text = text.replace("{Max_Health_1DP_Commas}", String.valueOf(formatter.format(maxhealth)));
		}
		if (text.contains("{Health_2DP_Commas}")) {
			DecimalFormat formatter = new DecimalFormat("#,##0.00");
			text = text.replace("{Health_2DP_Commas}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_2DP_Commas}")) {
			DecimalFormat formatter = new DecimalFormat("#,##0.00");
			text = text.replace("{Max_Health_2DP_Commas}", String.valueOf(formatter.format(maxhealth)));
		}		
		if (text.contains("{Health_Rounded}")) {
			DecimalFormat formatter = new DecimalFormat("0");
			text = text.replace("{Health_Rounded}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_Rounded}")) {
			DecimalFormat formatter = new DecimalFormat("0");
			text = text.replace("{Max_Health_Rounded}", String.valueOf(formatter.format(maxhealth)));
		}
		if (text.contains("{Health_1DP}")) {
			DecimalFormat formatter = new DecimalFormat("0.0");
			text = text.replace("{Health_1DP}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_1DP}")) {
			DecimalFormat formatter = new DecimalFormat("0.0");
			text = text.replace("{Max_Health_1DP}", String.valueOf(formatter.format(maxhealth)));
		}
		if (text.contains("{Health_2DP}")) {
			DecimalFormat formatter = new DecimalFormat("0.00");
			text = text.replace("{Health_2DP}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_2DP}")) {
			DecimalFormat formatter = new DecimalFormat("0.00");
			text = text.replace("{Max_Health_2DP}", String.valueOf(formatter.format(maxhealth)));
		}
		if (text.contains("{Health_Percentage}")) {
			DecimalFormat formatter = new DecimalFormat("0");
			text = text.replace("{Health_Percentage}", String.valueOf(formatter.format(percentage)));
		}
		if (text.contains("{Health_Percentage_1DP}")) {
			DecimalFormat formatter = new DecimalFormat("0.0");
			text = text.replace("{Health_Percentage_1DP}", String.valueOf(formatter.format(percentage)));
		}
		if (text.contains("{Health_Percentage_2DP}")) {
			DecimalFormat formatter = new DecimalFormat("0.00");
			text = text.replace("{Health_Percentage_2DP}", String.valueOf(formatter.format(percentage)));
		}
		if (text.contains("{DynamicColor}")) {
			double healthpercentage = percentage / 100.0;
			String symbol = "";
			if (healthpercentage < 0.33) {
				symbol = HoloMobHealth.LowColor;
			} else if (healthpercentage < 0.67) {
				symbol = HoloMobHealth.HalfColor;
			} else {
				symbol = HoloMobHealth.HealthyColor;
			}
			text = text.replace("{DynamicColor}", symbol);
		}
		if (text.contains("{ScaledSymbols}")) {
			String symbol = "";
			double healthpercentagescaled = percentage / 100.0 * (double) HoloMobHealth.heartScale;
			double i = 1;
			for (i = 1; i < healthpercentagescaled; i = i + 1) {
				symbol = symbol + HoloMobHealth.HealthyChar;
			}
			i = i - 1;
			if ((healthpercentagescaled - i) > 0 && (healthpercentagescaled - i) < 0.33) {
				symbol = symbol + HoloMobHealth.EmptyChar;
			} else if ((healthpercentagescaled - i) > 0 && (healthpercentagescaled - i) < 0.67) {
				symbol = symbol + HoloMobHealth.HalfChar;
			} else if ((healthpercentagescaled - i) > 0) {
				symbol = symbol + HoloMobHealth.HealthyChar;
			}
			for (i = HoloMobHealth.heartScale - 1; i >= healthpercentagescaled; i = i - 1) {
				symbol = symbol + HoloMobHealth.EmptyChar;
			}
			text = text.replace("{ScaledSymbols}", symbol);
		}
		
		text = ChatColor.translateAlternateColorCodes('&', text);
		List<String> sections = new ArrayList<String>();
		String[] parts = text.split("\\{Mob_Type_Or_Name\\}");
		if (text.startsWith("{Mob_Type_Or_Name}")) {
			sections.add("{Mob_Type_Or_Name}");
		}
		for (int i = 0; i < parts.length; i++) {
			String each = parts[i];
			String[] partsparts = each.split("\\{Mob_Type\\}");
			if (each.startsWith("{Mob_Type}")) {
				sections.add("{Mob_Type}");
			}
			for (int u = 0; u < partsparts.length; u++) {
				String eacheach = partsparts[u];
				sections.add(eacheach);
				if (u < partsparts.length - 1 || each.endsWith("{Mob_Type}")) {
					sections.add("{Mob_Type}");
				}
			}
			if (i < parts.length - 1 || text.endsWith("{Mob_Type_Or_Name}")) {
				sections.add("{Mob_Type_Or_Name}");
			}
		}
		
		List<BaseComponent> baselist = new ArrayList<BaseComponent>();
		String lastColor = "";
		for (String section : sections) {
			if (section.equals("{Mob_Type}")) {
				if (!HoloMobHealth.version.isLegacy()) {
					TranslatableComponent textcomp = new TranslatableComponent(EntityTypeUtils.getMinecraftLangName(entity));
					textcomp = (TranslatableComponent) ChatColorUtils.applyColor(textcomp, lastColor);
					baselist.add(textcomp);
				} else {
					TextComponent textcomp = new TextComponent(EntityTypeUtils.getMinecraftLangName(entity));
					baselist.add(textcomp);
				}
			} else if (section.equals("{Mob_Name}")) {
				if (entity.getCustomName() != null && !entity.getCustomName().equals("")) {
					TextComponent textcomp = new TextComponent(ChatColor.RESET + entity.getCustomName());
					baselist.add(textcomp);
				} else {
					TextComponent textcomp = new TextComponent("");
					baselist.add(textcomp);
				}
			} else if (section.equals("{Mob_Type_Or_Name}")) {
				if (entity.getCustomName() != null && !entity.getCustomName().equals("")) {
					TextComponent textcomp = new TextComponent(ChatColor.RESET + entity.getCustomName());
					baselist.add(textcomp);
				} else {
					if (!HoloMobHealth.version.isLegacy()) {
						TranslatableComponent textcomp = new TranslatableComponent(EntityTypeUtils.getMinecraftLangName(entity));
						textcomp = (TranslatableComponent) ChatColorUtils.applyColor(textcomp, lastColor);
						baselist.add(textcomp);
					} else {
						TextComponent textcomp = new TextComponent(EntityTypeUtils.getMinecraftLangName(entity));
						baselist.add(textcomp);
					}
				}
			} else {
				TextComponent textcomp = new TextComponent(section);
				baselist.add(textcomp);
			}
			lastColor = ChatColorUtils.getLastColors(section);
		}
		
		TextComponent product = new TextComponent("");
		for (int i = 0; i < baselist.size(); i++) {
			BaseComponent each = baselist.get(i);
			product.addExtra(each);
		}	
		
		return ComponentSerializer.toString(product);
	}

}
