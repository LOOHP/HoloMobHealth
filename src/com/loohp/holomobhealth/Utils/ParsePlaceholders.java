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
	
	@SuppressWarnings("deprecation")
	public static String parse(LivingEntity entity, String text) {
		DecimalFormat formatter = new DecimalFormat("#,###.00");	
		if (text.contains("{Health_Rounded_Commas}")) {
			long health = Math.round(entity.getHealth());	
			text = text.replace("{Health_Rounded_Commas}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_Rounded_Commas}")) {
			long health = Math.round(entity.getMaxHealth());
			text = text.replace("{Max_Health_Rounded_Commas}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Health_1DB_Commas}")) {
			double health = (double) Math.round(entity.getHealth() * (double) 10) / (double) 10;
			text = text.replace("{Health_1DB_Commas}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_1DB_Commas}")) {
			double health = (double) Math.round(entity.getMaxHealth() * (double) 10) / (double) 10;
			text = text.replace("{Max_Health_1DB_Commas}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Health_2DB_Commas}")) {
			double health = (double) Math.round(entity.getHealth() * (double) 10) / (double) 10;
			text = text.replace("{Health_2DB_Commas}", String.valueOf(formatter.format(health)));
		}
		if (text.contains("{Max_Health_2DB_Commas}")) {
			double health = (double) Math.round(entity.getMaxHealth() * (double) 10) / (double) 10;
			text = text.replace("{Max_Health_2DB_Commas}", String.valueOf(formatter.format(health)));
		}		
		if (text.contains("{Health_Rounded}")) {
			long health = Math.round(entity.getHealth());
			text = text.replace("{Health_Rounded}", String.valueOf(health));
		}
		if (text.contains("{Max_Health_Rounded}")) {
			long health = Math.round(entity.getMaxHealth());
			text = text.replace("{Max_Health_Rounded}", String.valueOf(health));
		}
		if (text.contains("{Health_1DB}")) {
			double health = (double) Math.round(entity.getHealth() * (double) 10) / (double) 10;
			text = text.replace("{Health_1DB}", String.valueOf(health));
		}
		if (text.contains("{Max_Health_1DB}")) {
			double health = (double) Math.round(entity.getMaxHealth() * (double) 10) / (double) 10;
			text = text.replace("{Max_Health_1DB}", String.valueOf(health));
		}
		if (text.contains("{Health_2DB}")) {
			double health = (double) Math.round(entity.getHealth() * (double) 10) / (double) 10;
			text = text.replace("{Health_2DB}", String.valueOf(health));
		}
		if (text.contains("{Max_Health_2DB}")) {
			double health = (double) Math.round(entity.getMaxHealth() * (double) 10) / (double) 10;
			text = text.replace("{Max_Health_2DB}", String.valueOf(health));
		}
		if (text.contains("{Health_Percentage}")) {
			long health = Math.round((entity.getHealth() / (double) entity.getMaxHealth()) * 100);
			text = text.replace("{Health_Percentage}", String.valueOf(health));
		}
		if (text.contains("{Health_Percentage_1DB}")) {
			double health = (double) Math.round((entity.getHealth() / (double) entity.getMaxHealth()) * 1000) / (double) 10;
			text = text.replace("{Health_Percentage_1DB}", String.valueOf(health));
		}
		if (text.contains("{Health_Percentage_2DB}")) {
			double health = (double) Math.round((entity.getHealth() / (double) entity.getMaxHealth()) * 10000) / (double) 10;
			text = text.replace("{Health_Percentage_2DB}", String.valueOf(health));
		}
		if (text.contains("{DynamicColor}")) {
			double healthpercentage = (entity.getHealth() / entity.getMaxHealth());
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
			double healthpercentagescaled = (entity.getHealth() / entity.getMaxHealth()) * (double) HoloMobHealth.heartScale;
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
				if (!HoloMobHealth.version.contains("legacy")) {
					TranslatableComponent textcomp = new TranslatableComponent(EntityTypeUtils.getMinecraftLangName(entity));
					textcomp = (TranslatableComponent) ChatColorUtils.applyColor(textcomp, lastColor);
					baselist.add(textcomp);
				} else {
					TextComponent textcomp = new TextComponent(EntityTypeUtils.getMinecraftLangName(entity));
					baselist.add(textcomp);
				}
			} else if (section.equals("{Mob_Type_Or_Name}")) {
				if (entity.getCustomName() != null && !entity.getCustomName().equals("")) {
					TextComponent textcomp = new TextComponent(ChatColor.RESET + entity.getCustomName());
					baselist.add(textcomp);
				} else {
					if (!HoloMobHealth.version.contains("legacy")) {
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
