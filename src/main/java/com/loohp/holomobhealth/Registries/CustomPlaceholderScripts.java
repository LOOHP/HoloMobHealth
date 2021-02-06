package com.loohp.holomobhealth.Registries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.Utils.CustomNameUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;

public class CustomPlaceholderScripts {
	
	public static final String PLACEHOLDER_FUNCTION = "placeholder";
	public static final Pattern pattern = Pattern.compile("//.*|/\\*[\\S\\s]*?\\*/|%([^%]+)%");
	
	private static Map<String, JavaScriptPlaceholder> scripts = new HashMap<>();
	
	public static void loadScriptsFromFolder(File folder) {
		File index = new File(folder, "scripts.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(index);
		
		File scriptFolder = new File(folder, "scripts");
		
		for (String placeholder : config.getValues(false).keySet()) {
			String fileName = config.getString(placeholder + ".file");
			File file = new File(scriptFolder, fileName);
			if (file.exists()) {
				try {
					loadScripts(file, "{" + placeholder + "}");
				} catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] Unable to load custom placeholder script from " + file.getName());
					e.printStackTrace();
				}
			} else {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] Unable to load custom placeholder script from " + file.getName() + " because it does not exist!");
			}
		}
	}
	
	public static void loadScripts(File file, String placeholder) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		String script = reader.lines().collect(Collectors.joining("\n"));
		reader.close();
        engine.put("BukkitServer", Bukkit.getServer());
        engine.put("Placeholder", placeholder);
        engine.put("PlaceholderAPI", PlaceholderAPI.class);
		scripts.put(placeholder, new JavaScriptPlaceholder(engine, placeholder, script));
	}
	
	public static void clearScripts() {
		scripts.clear();
	}
	
	public static String evaluate(String text, Double health, Double maxhealth, String customname, String mobtype, LivingEntity entity, String placeholder, ScriptEngine engine, String script) {
        try {
            engine.put("DisplayText", text);
            engine.put("Health", health);
            engine.put("MaxHealth", maxhealth);
            engine.put("CustomName", customname);
            engine.put("MobType", mobtype);
            engine.put("LivingEntity", entity);
            Object result = engine.eval(script);
            return result != null ? result.toString() : "";
        } catch (ScriptException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] An error occurred while executing the script '" + placeholder + "':\n\t" + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
        	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] Argument out of bound while executing script '" + placeholder + "':\n\t" + e.getMessage());
        }
        scripts.remove(placeholder);
        return "Script error (check console)";
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
		
		for (Entry<String, JavaScriptPlaceholder> entry : scripts.entrySet()) {
			String placeholder = entry.getKey();
			if (text.contains(placeholder)) {
				JavaScriptPlaceholder script = entry.getValue();
				String replaceText = evaluate(text, health, maxhealth, customname, mobtype, entity, placeholder, script.getEngine(), script.getScript());
				text = text.replace(placeholder, replaceText);
			}
		}
		return text;
	}
	
	public static int getScriptsCount() {
		return scripts.size();
	}
	
	public static class JavaScriptPlaceholder {
		
		private ScriptEngine engine;
		private String placeholder;
		private String script;
		
		public JavaScriptPlaceholder(ScriptEngine engine, String placeholder, String script) {
			this.engine = engine;
			this.placeholder = placeholder;
			this.script = script;
		}

		public ScriptEngine getEngine() {
			return engine;
		}
		
		public String getPlaceholder() {
			return placeholder;
		}
		
		public String getScript() {
			return script;
		}
		
	}

}
