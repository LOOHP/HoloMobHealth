package com.loohp.holomobhealth.API;

import com.loohp.holomobhealth.Registries.CustomPlaceholderScripts;

public class HoloMobHealthAPI {
	
	public static void registerClassToCustomPlaceholderScript(String identifier, Class<?> clazz) {
		CustomPlaceholderScripts.registerClass(identifier, clazz);
	}
	
	public static boolean containsClassInCustomPlaceholderScript(String identifier) {
		return CustomPlaceholderScripts.containsClass(identifier);
	}
	
	public static boolean containsClassInCustomPlaceholderScript(Class<?> clazz) {
		return CustomPlaceholderScripts.containsClass(clazz);
	}

}
