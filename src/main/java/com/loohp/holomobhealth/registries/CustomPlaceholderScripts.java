package com.loohp.holomobhealth.registries;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.utils.CustomNameUtils;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CustomPlaceholderScripts {

    public static final String PLACEHOLDER_FUNCTION = "placeholder";
    public static final Pattern PATTERN = Pattern.compile("//.*|/\\*[\\S\\s]*?\\*/|%([^%]+)%");
    private static final Map<String, JavaScriptPlaceholder> scripts = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> scriptDataTypes = new ConcurrentHashMap<>();
    private static ScriptEngineFactory scriptEngineFactory;

    static {
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_15)) {
            scriptEngineFactory = new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory();
        } else {
            try {
                scriptEngineFactory = (ScriptEngineFactory) Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory").getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
        ScriptEngine engine = scriptEngineFactory.getScriptEngine();
        if (engine == null) {
            throw new RuntimeException("JavaScript ScriptEngine isn't supported on your JVM! Is your version of Java too new?");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        String script = reader.lines().collect(Collectors.joining("\n"));
        reader.close();
        engine.put("BukkitServer", Bukkit.getServer());
        engine.put("Placeholder", placeholder);
        scripts.put(placeholder, new JavaScriptPlaceholder(engine, placeholder, script));
    }

    public static void clearScripts() {
        scripts.clear();
    }

    public static String evaluate(String text, Double health, Double maxhealth, Double healthchange, String customname, String mobtype, LivingEntity entity, String placeholder, ScriptEngine engine, String script) {
        try {
            engine.put("DisplayText", text);
            engine.put("Health", health);
            engine.put("MaxHealth", maxhealth);
            engine.put("HealthChange", healthchange);
            engine.put("CustomName", customname);
            engine.put("MobType", mobtype);
            engine.put("LivingEntity", entity);
            for (Entry<String, Class<?>> entry : scriptDataTypes.entrySet()) {
                engine.put(entry.getKey(), entry.getValue());
            }
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
    public static String runScripts(String text, LivingEntity entity, double healthchange) throws Exception {
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
                String replaceText = evaluate(text, health, maxhealth, healthchange, customname, mobtype, entity, placeholder, script.getEngine(), script.getScript());
                text = text.replace(placeholder, replaceText);
            }
        }
        return text;
    }

    public static int getScriptsCount() {
        return scripts.size();
    }

    public static void registerClass(String identifier, Class<?> clazz) {
        scriptDataTypes.put(identifier, clazz);
    }

    public static boolean containsClass(String identifier) {
        return scriptDataTypes.containsKey(identifier);
    }

    public static boolean containsClass(Class<?> clazz) {
        Iterator<Entry<String, Class<?>>> itr = scriptDataTypes.entrySet().iterator();
        while (itr.hasNext()) {
            if (itr.next().getValue().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static class JavaScriptPlaceholder {

        private final ScriptEngine engine;
        private final String placeholder;
        private final String script;

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
