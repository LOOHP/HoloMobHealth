package com.loohp.holomobhealth.registries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.utils.CustomStringUtils;

import net.md_5.bungee.api.ChatColor;

public class DisplayTextCacher {
	
	private static Map<String, HealthFormatData> decimalFormatMapping = new ConcurrentHashMap<String, HealthFormatData>();
	private static AtomicInteger counter = new AtomicInteger(0);
	private static Object lock = new Object();
	
	public static List<String> cacheDecimalFormat(List<String> lines) {
		synchronized (DisplayTextCacher.lock) {
			List<String> cachedList = new ArrayList<>(lines.size());
			
			for (String text : lines) {							
				cachedList.add(cacheDecimalFormat(text));	
			}
			
			return cachedList;
		}
	}
	
	public static String cacheDecimalFormat(String line) {
		synchronized (DisplayTextCacher.lock) {
			String text = line;
			while (true) {
				Matcher matcher = Pattern.compile("\\{(Health|MaxHealth|PercentageHealth|Indicator)_.+?\\}").matcher(text);
				if (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					String matched = matcher.group();
					try {
						DecimalFormat formatter = new DecimalFormat(matched.substring(matched.indexOf("_") + 1, matched.lastIndexOf("}")));
						formatter.setRoundingMode(HoloMobHealth.roundingMode);
						String placeholder = "%D%" + counter.getAndIncrement() + "%F%";
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
			return text;
		}
	}
	
	public static Map<String, HealthFormatData> getDecimalFormatMapping() {
		return Collections.unmodifiableMap(decimalFormatMapping);
	}
	
	public static void clearDecimalFormatMappings() {
		synchronized (DisplayTextCacher.lock) {
			decimalFormatMapping.clear();
			counter.set(0);
		}
	}
	
	public static enum HealthType {
		HEALTH("Health"),
		MAXHEALTH("MaxHealth"),
		PERCENTAGEHEALTH("PercentageHealth"),
		INDICATOR("Indicator");
		
		private String name;
		
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
		private DecimalFormat format;
		private HealthType type;
		
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

}
