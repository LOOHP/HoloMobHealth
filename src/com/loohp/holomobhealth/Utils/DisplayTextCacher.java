package com.loohp.holomobhealth.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class DisplayTextCacher {
	
	public enum HealthType {
		HEALTH("Health"),
		MAXHEALTH("MaxHealth"),
		PERCENTAGEHEALTH("PercentageHealth");
		
		String name;
		
		HealthType(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static HealthType fromName(String name) {
			for (HealthType type : HealthType.values()) {
				if (type.toString().equalsIgnoreCase(name)) {
					return type;
				}
			}
			return null;
		}
	}
	
	public static class HealthFormatData {
		DecimalFormat format;
		HealthType type;
		
		public HealthFormatData(DecimalFormat format, HealthType type) {
			this.format = format;
			this.type = type;
		}
		
		public DecimalFormat getFormatter( ) {
			return format;
		}
		
		public HealthType getType( ) {
			return type;
		}
	}
	
	private static Map<String, HealthFormatData> decimalFormatMapping = new HashMap<String, HealthFormatData>();
	
	public static List<String> cacheDecimalFormat(List<String> lines) {
		List<String> cachedList = new ArrayList<String>(lines.size());
		decimalFormatMapping.clear();
		
		int current = 0;
		for (String text : lines) {
			
			while (true) {
				Matcher matcher = Pattern.compile("\\{(Health|MaxHealth|PercentageHealth)_.+?\\}").matcher(text);
				if (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					String matched = matcher.group();
					try {
						DecimalFormat formatter = new DecimalFormat(matched.substring(matched.indexOf("_") + 1, matched.lastIndexOf("}")));
						String placeholder = "%D%" + current++ + "%F%";
						decimalFormatMapping.put(placeholder, new HealthFormatData(formatter, HealthType.fromName(matched.substring(matched.indexOf("{") + 1, matched.indexOf("_")))));
						text = CustomStringUtils.replaceFromTo(text, start, end, placeholder);
					} catch (Exception e) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] There is a syntax error with your placeholders in your mob name field in the config! (\"" + matched + "\")");
						break;
					}
				} else {
					break;
				}
			}
			
			cachedList.add(text);	
		}
		
		return cachedList;
	}
	
	public static Map<String, HealthFormatData> getDecimalFormatMapping() {
		return new HashMap<>(decimalFormatMapping);
	}

}
