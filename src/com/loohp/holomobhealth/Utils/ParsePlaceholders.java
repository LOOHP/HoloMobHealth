package com.loohp.holomobhealth.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Registries.CustomPlaceholderScripts;
import com.loohp.holomobhealth.Registries.DisplayTextCacher;
import com.loohp.holomobhealth.Registries.DisplayTextCacher.HealthFormatData;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ParsePlaceholders {
	
	@SuppressWarnings("deprecation")
	public static String parse(Player player, LivingEntity entity, String text) {	
		double health = entity.getHealth();
		double maxhealth = 0.0;
		if (!HoloMobHealth.version.isLegacy()) {
			maxhealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		} else {
			maxhealth = entity.getMaxHealth();
		}
		double percentage = (health / maxhealth) * 100;
		
		int heartScale = HoloMobHealth.dynamicScale ? (Math.ceil(maxhealth / 2) > HoloMobHealth.heartScale ? HoloMobHealth.heartScale : (int) Math.ceil(maxhealth / 2)) : HoloMobHealth.heartScale;
		
		for (Entry<String, HealthFormatData> entry : DisplayTextCacher.getDecimalFormatMapping().entrySet()) {
			HealthFormatData data = entry.getValue();
			switch (data.getType()) {
			case HEALTH:
				text = text.replace(entry.getKey(), String.valueOf(data.getFormatter().format(health)));
				break;
			case MAXHEALTH:
				text = text.replace(entry.getKey(), String.valueOf(data.getFormatter().format(maxhealth)));
				break;
			case PERCENTAGEHEALTH:
				text = text.replace(entry.getKey(), String.valueOf(data.getFormatter().format(percentage)));
				break;
			}
		}

		if (text.contains("{DynamicColor}")) {
			String symbol;
			if (percentage < 33.33) {
				symbol = HoloMobHealth.LowColor;
			} else if (percentage < 66.67) {
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
		
		try {
			text = CustomPlaceholderScripts.runScripts(text, entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (HoloMobHealth.PlaceholderAPIHook) {
			text = PlaceholderAPI.setPlaceholders(player, text);
		}

		text = ChatColorUtils.translateAlternateColorCodes('&', text);
		
		List<String> sections = new ArrayList<String>();
		sections.addAll(Arrays.asList(text.split("(?<=})|(?![^{])")));
		
		String rawName = CustomNameUtils.getMobCustomName(entity);
		BaseComponent customName = rawName != null ? ChatComponentUtils.join(TextComponent.fromLegacyText(ChatColor.RESET + rawName)) : null;
		
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
				if (customName != null) {
					baselist.add(customName);
				} else {
					TextComponent textcomp = new TextComponent("");
					baselist.add(textcomp);
				}
			} else if (section.equals("{Mob_Type_Or_Name}")) {
				if (customName != null) {
					baselist.add(customName);
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
			product = ChatComponentUtils.cleanUpLegacyText(ChatComponentUtils.translatePluginFontFormatting(product));
		}
		
		//Bukkit.getConsoleSender().sendMessage(ComponentSerializer.toString(product));
		return ComponentSerializer.toString(product);
	}

}
