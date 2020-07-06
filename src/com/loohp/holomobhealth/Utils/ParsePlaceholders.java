package com.loohp.holomobhealth.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
		
		int heartScale = HoloMobHealth.dynamicScale ? (Math.ceil(maxhealth / 2) > HoloMobHealth.heartScale ? HoloMobHealth.heartScale : (int) Math.ceil(maxhealth / 2)) : HoloMobHealth.heartScale;
		
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
			StringBuilder symbol = new StringBuilder();
			double healthpercentagescaled = percentage / 100.0 * (double) heartScale;
			int fullhearts = (int) Math.floor(healthpercentagescaled);
			for (int i = 0; i < fullhearts; i++) {
				symbol.append(HoloMobHealth.HealthyChar);
			}
			if (fullhearts < heartScale) {
				double leftover = healthpercentagescaled - (double) fullhearts;
				if (leftover > 0.67) {
					symbol.append(HoloMobHealth.HealthyChar);
				} else if (leftover > 0.33) {
					symbol.append(HoloMobHealth.HalfChar);
				} else {
					symbol.append(HoloMobHealth.EmptyChar);
				}
				for (int i = fullhearts + 1; i < heartScale; i++) {
					symbol.append(HoloMobHealth.EmptyChar);
				}
			}
			text = text.replace("{ScaledSymbols}", symbol);
		}

		text = ChatColorUtils.translateAlternateColorCodes('&', text);
		List<String> sections = new ArrayList<String>();
		sections.addAll(Arrays.asList(text.split("(?<=})|(?![^{])")));
		
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
				baselist.addAll(Arrays.asList(TextComponent.fromLegacyText(section)));
			}
			lastColor = ChatColorUtils.getLastColors(section);
		}
		
		BaseComponent product = new TextComponent("");
		for (int i = 0; i < baselist.size(); i++) {
			BaseComponent each = baselist.get(i);
			product.addExtra(each);
		}
		
		if (HoloMobHealth.version.isPost1_16()) {
			product = ChatComponentUtils.translatePluginFontFormatting(product);
		}
		
		return ComponentSerializer.toString(product);
	}

}
