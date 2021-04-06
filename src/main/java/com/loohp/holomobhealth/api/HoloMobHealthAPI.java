package com.loohp.holomobhealth.api;

import com.loohp.holomobhealth.registries.CustomPlaceholderScripts;

public class HoloMobHealthAPI {
	
	/**
	 * Register a class to be used in custom placeholder scripts
	 * @param The class name
	 * @param The class
	 */
	public static void registerClassToCustomPlaceholderScript(String identifier, Class<?> clazz) {
		CustomPlaceholderScripts.registerClass(identifier, clazz);
	}
	
	/**
	 * Check if a class has already be registered to be used in custom placeholder scripts
	 * @param The class name
	 * @return TRUE/FALSE
	 */
	public static boolean containsClassInCustomPlaceholderScript(String identifier) {
		return CustomPlaceholderScripts.containsClass(identifier);
	}
	
	/**
	 * Check if a class has already be registered to be used in custom placeholder scripts
	 * @param The class
	 * @return TRUE/FALSE
	 */
	public static boolean containsClassInCustomPlaceholderScript(Class<?> clazz) {
		return CustomPlaceholderScripts.containsClass(clazz);
	}

}
