package com.loohp.holomobhealth.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.loohp.holomobhealth.HoloMobHealth;

public class LegacyEntityTypeUtils {
		
	private static File file;
	private static JSONObject json;
    private static JSONParser parser = new JSONParser();
    private static HashMap<Short, String> entry = new HashMap<Short, String>();
	
	@SuppressWarnings("deprecation")
	public static String getLegacyMinecraftName(Entity entity) {			
		EntityType type = entity.getType();
		
		if (entry.containsKey(type.getTypeId())) {
			return entry.get(type.getTypeId());
		}
		return null;
	}
	
	public static void reloadLegacyLang() {	
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(file));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

	    JSONArray each = (JSONArray) obj;

	    for (int i = 0; i < each.size(); i++) {
	    	json = (JSONObject) each.get(i);
	    	if (!json.containsKey("internalId")) {
	    		continue;
	    	}
	    	short id = Short.parseShort(json.get("internalId").toString());
			String name = json.get("displayName").toString();
			entry.put(id, name);
	    }  
	}
	
	public static void setupLegacyLang() {		
		String langVersion = "Legacy";
		
		if (!HoloMobHealth.plugin.getDataFolder().exists()) {
			HoloMobHealth.plugin.getDataFolder().mkdir();
		}
		
		File langFolder = new File(HoloMobHealth.plugin.getDataFolder().getAbsolutePath() + "/lang");
		if (!langFolder.exists()) {
			langFolder.mkdir();
		}
		
        String resourceName = "/lang/" + langVersion + ".json";
        
        File langFile = new File(langFolder.getAbsolutePath() + "/" + langVersion + ".json");
        if (!langFile.exists()) {
        	try (InputStream in = HoloMobHealth.plugin.getClass().getResourceAsStream(resourceName)) {
                Files.copy(in, langFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  
        
        file = langFile;
        reloadLegacyLang();
	}
		
}
