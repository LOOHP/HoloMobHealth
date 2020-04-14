package com.loohp.holomobhealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.loohp.holomobhealth.Debug.Debug;
import com.loohp.holomobhealth.Listeners.Events;
import com.loohp.holomobhealth.Metrics.Metrics;
import com.loohp.holomobhealth.Updater.Updater;
import com.loohp.holomobhealth.Utils.CitizensUtils;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.MetadataPacket;
import com.loohp.holomobhealth.Utils.MythicMobsUtils;
import com.loohp.holomobhealth.Utils.ParsePlaceholders;

import net.md_5.bungee.api.ChatColor;

public class HoloMobHealth extends JavaPlugin {
	
	public static Plugin plugin = null;
	
	public static String version = "";
	
	public static ProtocolManager protocolManager;
	
	public static int activeShowHealthTaskID = -1;
	
	public static String DisplayText = "&b{Mob_Name}: &c{Health}&f/{Max_Health}";
	
	public static int heartScale = 10;
	
	public static String HealthyColor = "&a";
	public static String HalfColor = "&e";
	public static String LowColor = "&c";
	
	public static String HealthyChar = "&c❤";
	public static String HalfChar = "&e❤";
	public static String EmptyChar = "&7❤";
	
	public static boolean alwaysShow = true;
	public static int range = 15;
	public static boolean applyToNamed = false;
	
	public static String ReloadPlugin = "&aHoloMobHealth has been reloaded!";	
	public static String NoPermission = "&cYou do not have permission to use that command!";
	
	public static Set<Entity> nearbyEntities = new HashSet<Entity>();
	public static Set<Entity> nearbyPlus10Entities = new HashSet<Entity>();
	
	public static List<EntityType> DisabledMobTypes = new ArrayList<EntityType>();
	public static List<String> DisabledMobNamesAbsolute = new ArrayList<String>();
	public static List<String> DisabledMobNamesContains = new ArrayList<String>();
	
	public static boolean UseAlterHealth = false;
	public static int AltHealthDisplayTime = 3;
	public static boolean AltOnlyPlayer = false;
	public static HashMap<Entity, Long> altShowHealth = new HashMap<Entity, Long>();
	
	public static boolean MythicHook = false;
	public static boolean showMythicMobs = true;
	
	public static boolean CitizensHook = false;
	public static boolean showCitizens = true;
	
	public static boolean UpdaterEnabled = true;
	public static int UpdaterTaskID = -1;
	
	@Override
	public void onEnable() {	
		plugin = (Plugin)getServer().getPluginManager().getPlugin("HoloMobHealth");
		
		getServer().getPluginManager().registerEvents(new Debug(), this);

		int pluginId = 6749;

		Metrics metrics = new Metrics(this, pluginId);
        
        getServer().getPluginManager().registerEvents(new Events(), this);
		
		HoloMobHealth.plugin.getConfig().options().copyDefaults(true);
		HoloMobHealth.plugin.saveConfig();
		
		HoloMobHealth.loadConfig();
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		
	    getCommand("holomobhealth").setExecutor(new Commands());
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Citizens") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into Citizens!");
	    	CitizensHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into MythicMobs!");
	    	MythicHook = true;
		}
		
	    String packageName = getServer().getClass().getPackage().getName();

        if (packageName.contains("1_15_R1")) {
            version = "1.15";
        } else if (packageName.contains("1_14_R1")) {
            version = "1.14";
        } else if (packageName.contains("1_13_R2")) {
            version = "1.13.1";
        } else if (packageName.contains("1_13_R1")) {
            version = "1.13";
        } else if (packageName.contains("1_12_R1")) {
            version = "legacy1.12";
        } else if (packageName.contains("1_11_R1")) {
            version = "legacy1.11";
        } else if (packageName.contains("1_10_R1")) {
            version = "legacy1.10";
        } else if (packageName.contains("1_9_R2")) {
            version = "legacy1.9.4";
        } else if (packageName.contains("1_9_R1")) {
            version = "legacy1.9";
        } else if (packageName.contains("1_8_R3")) {
            version = "OLDlegacy1.8.4";
        } else if (packageName.contains("1_8_R2")) {
            version = "OLDlegacy1.8.3";
        } else if (packageName.contains("1_8_R1")) {
            version = "OLDlegacy1.8";
        } else {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
            plugin.getPluginLoader().disablePlugin(this);
        }
		
	    EntityTypeUtils.setUpList();
		EntityTypeUtils.setupLang();
		
		addEntities();
		removeEntities();
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_mobs_displaying", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return HoloMobHealth.nearbyPlus10Entities.size();
            }
        }));
	    
	    getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "HoloMobHealth has been Enabled!");
		
	}

	@Override
	public void onDisable() {		
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "HoloMobHealth has been Disabled!");
	}
	
	@SuppressWarnings("deprecation")
	public static void loadConfig() {
		HoloMobHealth.DisplayText = HoloMobHealth.plugin.getConfig().getString("Display.Text");
		
		HoloMobHealth.heartScale = HoloMobHealth.plugin.getConfig().getInt("Display.ScaledSymbolSettings.Scale");
		
		HoloMobHealth.HealthyColor = HoloMobHealth.plugin.getConfig().getString("Display.DynamicColorSettings.HealthyColor");
		HoloMobHealth.HalfColor = HoloMobHealth.plugin.getConfig().getString("Display.DynamicColorSettings.HalfColor");
		HoloMobHealth.LowColor = HoloMobHealth.plugin.getConfig().getString("Display.DynamicColorSettings.LowColor");
		
		HoloMobHealth.HealthyChar = HoloMobHealth.plugin.getConfig().getString("Display.ScaledSymbolSettings.HealthyChar");
		HoloMobHealth.HalfChar = HoloMobHealth.plugin.getConfig().getString("Display.ScaledSymbolSettings.HalfChar");
		HoloMobHealth.EmptyChar = HoloMobHealth.plugin.getConfig().getString("Display.ScaledSymbolSettings.EmptyChar");
		
		HoloMobHealth.alwaysShow = HoloMobHealth.plugin.getConfig().getBoolean("Options.AlwaysShow");
		HoloMobHealth.range = HoloMobHealth.plugin.getConfig().getInt("Options.Range");
		HoloMobHealth.applyToNamed = HoloMobHealth.plugin.getConfig().getBoolean("Options.ApplyToNamed");
		
		HoloMobHealth.ReloadPlugin = ChatColor.translateAlternateColorCodes('&', HoloMobHealth.plugin.getConfig().getString("Messages.ReloadPlugin"));
		HoloMobHealth.NoPermission = ChatColor.translateAlternateColorCodes('&', HoloMobHealth.plugin.getConfig().getString("Messages.NoPermission"));
		
		List<String> types = HoloMobHealth.plugin.getConfig().getStringList("Options.DisabledMobTypes");
		for (String each : types) {
			if (HoloMobHealth.version.contains("legacy")) {
				HoloMobHealth.DisabledMobTypes.add(EntityType.fromName(each.toUpperCase()));
			} else {
				HoloMobHealth.DisabledMobTypes.add(EntityType.valueOf(each.toUpperCase()));
			}
		}
		HoloMobHealth.DisabledMobNamesAbsolute = HoloMobHealth.plugin.getConfig().getStringList("Options.DisabledMobNamesAbsolute");
		HoloMobHealth.DisabledMobNamesContains = HoloMobHealth.plugin.getConfig().getStringList("Options.DisabledMobNamesContains");
		
		HoloMobHealth.UseAlterHealth = HoloMobHealth.plugin.getConfig().getBoolean("Options.DynamicHealthDisplay.Use");
		HoloMobHealth.AltHealthDisplayTime = HoloMobHealth.plugin.getConfig().getInt("Options.DynamicHealthDisplay.Timeout");
		HoloMobHealth.AltOnlyPlayer = HoloMobHealth.plugin.getConfig().getBoolean("Options.DynamicHealthDisplay.OnlyPlayerTrigger");
		
		if (activeShowHealthTaskID >= 0) {
			Bukkit.getScheduler().cancelTask(activeShowHealthTaskID);
		}
		
		if (!HoloMobHealth.UseAlterHealth) {  			
			sendHealth();
		} else {    			
		    sendAltHealth(); 
		}
		
		HoloMobHealth.showCitizens = HoloMobHealth.plugin.getConfig().getBoolean("Hooks.Citizens.ShowNPCMobHealth");
		
		HoloMobHealth.showMythicMobs = HoloMobHealth.plugin.getConfig().getBoolean("Hooks.MythicMobs.ShowMythicMobsHealth");
		
		if (UpdaterTaskID >= 0) {
			Bukkit.getScheduler().cancelTask(UpdaterTaskID);
		}
		HoloMobHealth.UpdaterEnabled = HoloMobHealth.plugin.getConfig().getBoolean("Updater.Enable");
		if (UpdaterEnabled) {
			Updater.updaterInterval();
		}
	}
	
	public static void addEntities() {
		Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			int delay = 1;
			int count = 0;
			int maxper = (int) Math.ceil((double) Bukkit.getOnlinePlayers().size() / (double) 20);
			for (Player eachPlayer : Bukkit.getOnlinePlayers()) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				UUID uuid = eachPlayer.getUniqueId();
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
						if (Bukkit.getPlayer(uuid) == null) {
							return;
						}
						Player player = Bukkit.getPlayer(uuid);
						
						nearbyEntities.addAll(player.getNearbyEntities(range, range, range));
						nearbyPlus10Entities.addAll(player.getNearbyEntities(range + 10, range + 10, range + 10));
				}, delay);
			}
		}, 0, 20);
	}
	
	public static void removeEntities() {
		int next = 2;
		int delay = 10;
		int count = 0;
		int maxper = (int) Math.ceil((double) nearbyPlus10Entities.size() / (double) 400); 
		for (Entity entity : nearbyPlus10Entities) {
			count++;
			if (count > 5 && count > maxper) {
				count = 0;
				delay++;
			}
			Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.getWorld().equals(entity.getWorld())) {
						if (player.getLocation().distanceSquared(entity.getLocation()) <= ((range + 10) * (range + 10))) {
							return;
						}
					}
				}
				nearbyEntities.remove(entity);
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> nearbyPlus10Entities.remove(entity), 10);
			}, delay);
		}
		next = next + delay;
		Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> removeEntities(), next);
	}
	
	public static void sendAltHealth() {
		
		activeShowHealthTaskID = Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			Iterator<Entry<Entity, Long>> itr = HoloMobHealth.altShowHealth.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<Entity, Long> entry = itr.next();
				long unix = System.currentTimeMillis();
				if (entry.getValue() < unix) {
					itr.remove();
				}
			}
			
			int delay = 1;
			int count = 0;
			int maxper = (int) Math.ceil((double) nearbyPlus10Entities.size() / (double) 3);
			Set<Entity> inRange = nearbyEntities;
			for (Entity entity : nearbyPlus10Entities) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
					if (!entity.isValid()) {
						return;
					}
					
					if (HoloMobHealth.DisabledMobTypes.contains(entity.getType())) {
						return;
					}
					if (!showCitizens && CitizensHook) {
						if (CitizensUtils.isNPC(entity)) {
							return;
						}
					}
					if (!showMythicMobs && MythicHook) {
						if (MythicMobsUtils.isMythicMob(entity)) {
							return;
						}
					}
					if (entity.getCustomName() != null) {
						if (!entity.getCustomName().equals("")) {
							boolean contain = false;
							for (String each : HoloMobHealth.DisabledMobNamesAbsolute) {
								if (entity.getCustomName().equals(ChatColor.translateAlternateColorCodes('&', each))) {
									contain = true;
									break;
								}
							}
							for (String each : HoloMobHealth.DisabledMobNamesContains) {
								if (ChatColor.stripColor(entity.getCustomName().toLowerCase()).contains(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', each).toLowerCase()))) {
									contain = true;
									break;
								}
							}
							if (contain) {
								return;
							}
						}
					}
					if (EntityTypeUtils.getMobList().contains(entity.getType())) { 
						if ((!inRange.contains(entity)) || (!HoloMobHealth.altShowHealth.containsKey(entity))) {
							String name = entity.getCustomName();							
							boolean visible = entity.isCustomNameVisible();
							MetadataPacket.sendMetadataPacket(entity, name, visible);
							return;
						}
						if (!HoloMobHealth.applyToNamed) {
							if (entity.getCustomName() != null) {
								if (!entity.getCustomName().equals("")) {
									String name = entity.getCustomName();							
									boolean visible = entity.isCustomNameVisible();
									MetadataPacket.sendMetadataPacket(entity, name, visible);
									return;
								}
							}	
						}
						String display = ParsePlaceholders.parse((LivingEntity) entity, HoloMobHealth.DisplayText);
						MetadataPacket.sendMetadataPacket(entity, display, HoloMobHealth.alwaysShow);
					}
				}, delay);
			}
		}, 0, 4).getTaskId();
	}
	
	public static void sendHealth() {
		
		activeShowHealthTaskID = Bukkit.getScheduler().runTaskTimer(HoloMobHealth.plugin, () -> {
			int delay = 1;
			int count = 0;
			int maxper = (int) Math.ceil((double) nearbyPlus10Entities.size() / (double) 3);
			Set<Entity> inRange = nearbyEntities;
			for (Entity entity : nearbyPlus10Entities) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				
				Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> {
						if (!entity.isValid()) {
							return;
						}

						if (HoloMobHealth.DisabledMobTypes.contains(entity.getType())) {
							return;
						}
						if (!showCitizens && CitizensHook) {
							if (CitizensUtils.isNPC(entity)) {
								return;
							}
						}
						if (!showMythicMobs && MythicHook) {
							if (MythicMobsUtils.isMythicMob(entity)) {
								return;
							}
						}
						if (entity.getCustomName() != null) {
							if (!entity.getCustomName().equals("")) {
								boolean contain = false;
								for (String each : HoloMobHealth.DisabledMobNamesAbsolute) {
									if (entity.getCustomName().equals(ChatColor.translateAlternateColorCodes('&', each))) {
										contain = true;
										break;
									}
								}
								for (String each : HoloMobHealth.DisabledMobNamesContains) {
									if (ChatColor.stripColor(entity.getCustomName().toLowerCase()).contains(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', each).toLowerCase()))) {
										contain = true;
										break;
									}
								}
								if (contain) {
									return;
								}
							}
						}
						if (EntityTypeUtils.getMobList().contains(entity.getType())) { 
							if (!inRange.contains(entity)) {
								String name = entity.getCustomName();							
								boolean visible = entity.isCustomNameVisible();
								MetadataPacket.sendMetadataPacket(entity, name, visible);
								return;
							}
							if (!HoloMobHealth.applyToNamed) {
								if (entity.getCustomName() != null) {
									if (!entity.getCustomName().equals("")) {
										String name = entity.getCustomName();
										boolean visible = entity.isCustomNameVisible();
										MetadataPacket.sendMetadataPacket(entity, name, visible);
										return;
									}
								}	
							}
							String display = ParsePlaceholders.parse((LivingEntity) entity, HoloMobHealth.DisplayText);
							MetadataPacket.sendMetadataPacket(entity, display, HoloMobHealth.alwaysShow);
						}
				}, delay);
			}
		}, 0, 4).getTaskId();
	}
}