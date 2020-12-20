package com.loohp.holomobhealth.Registries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Utils.CustomNameUtils;

import net.md_5.bungee.api.ChatColor;

public class CustomPlaceholderScripts {
	
	public static final String PLACEHOLDER_FUNCTION = "placeholder";
	public static final String PARSE_FUNCTION = "parse";
	
	private static Map<String, Invocable> scripts = new HashMap<>();
	
	public static void loadScriptsFromFolder(File folder) {
		for (File file : folder.listFiles()) {
			try {
				loadScripts(file);
			} catch (Exception e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] Unable to load custom placeholder script from " + file.getName());
				e.printStackTrace();
			}
		}
	}
	
	public static void loadScripts(File file) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		engine.eval(reader.lines().collect(Collectors.joining()));
		reader.close();
		Invocable invocable = (Invocable) engine;
		String placeholder = invocable.invokeFunction(PLACEHOLDER_FUNCTION).toString();
		long start = System.currentTimeMillis();
		invocable.invokeFunction(PARSE_FUNCTION, "test", EntityType.ZOMBIE.toString(), 0, 10);
		if ((System.currentTimeMillis() - start) > 10) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[HoloMobHealth] Warning: Script from " + file.getName() + " took more than 10ms to execute!");
		}
		scripts.put(placeholder, invocable);
	}
	
	public static void clearScripts() {
		scripts.clear();
	}
	
	@SuppressWarnings("deprecation")
	public static String runScripts(String text, LivingEntity entity) throws Exception {
		double health = entity.getHealth();
		double maxhealth = 0.0;
		if (!HoloMobHealth.version.isLegacy()) {
			maxhealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		} else {
			maxhealth = entity.getMaxHealth();
		}
		String customname = CustomNameUtils.getMobCustomName(entity);
		String mobtype = entity.getType().toString();
				
		for (Entry<String, Invocable> entry : scripts.entrySet()) {
			String placeholder = entry.getKey();
			if (text.contains(placeholder)) {
				String replaceText = entry.getValue().invokeFunction(PARSE_FUNCTION, customname, mobtype, health, maxhealth).toString();
				text = text.replace(placeholder, replaceText);
			}
		}
		return text;
	}

}
