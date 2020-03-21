package com.loohp.holomobhealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.loohp.holomobhealth.Debug.Debug;
import com.loohp.holomobhealth.Metrics.Metrics;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.MetadataPacket;

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
	
	public static HashMap<Player, List<Entity>> nearbyEntities = new HashMap<Player, List<Entity>>();
	public static HashMap<Player, List<Entity>> nearbyPlus10Entities = new HashMap<Player, List<Entity>>();
	
	public static List<EntityType> DisabledMobTypes = new ArrayList<EntityType>();
	public static List<String> DisabledMobNamesAbsolute = new ArrayList<String>();
	public static List<String> DisabledMobNamesContains = new ArrayList<String>();
	
	public static boolean UseAlterHealth = false;
	public static int AltHealthDisplayTime = 3;
	public static boolean AltOnlyPlayer = false;
	public static HashMap<Entity, Long> altShowHealth = new HashMap<Entity, Long>();
	
	private static int startUpTaskId;
	
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
		
	    if (getServer().getClass().getPackage().getName().contains("1_15_R1") == true) {
	    	version = "1.15";
	    } else if (getServer().getClass().getPackage().getName().contains("1_14_R1") == true) {
	    	version = "1.14";
	    } else if (getServer().getClass().getPackage().getName().contains("1_13_R2") == true) {
	    	version = "1.13.1";
	    } else if (getServer().getClass().getPackage().getName().contains("1_13_R1") == true) {
	    	version = "1.13";
	    } else if (getServer().getClass().getPackage().getName().contains("1_12_R1") == true) {
	    	version = "legacy1.12";
	    } else if (getServer().getClass().getPackage().getName().contains("1_11_R1") == true) {
	    	version = "legacy1.11";
	    } else if (getServer().getClass().getPackage().getName().contains("1_10_R1") == true) {
	    	version = "legacy1.10";
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R2") == true) {
	    	version = "legacy1.9.4";
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R1") == true) {
	    	version = "legacy1.9";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R3") == true) {
	    	version = "OLDlegacy1.8.4";
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    	plugin.getPluginLoader().disablePlugin(this);
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R2") == true) {
	    	version = "OLDlegacy1.8.3";
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    	plugin.getPluginLoader().disablePlugin(this);
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R1") == true) {
	    	version = "OLDlegacy1.8";
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    	plugin.getPluginLoader().disablePlugin(this);
	    } else {
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    	plugin.getPluginLoader().disablePlugin(this);
	    }
		
	    EntityTypeUtils.setUpList();
		EntityTypeUtils.setupLang();
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_mobs_displaying", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
            	int total = 0;
            	for (Entry<Player, List<Entity>> entry : HoloMobHealth.nearbyPlus10Entities.entrySet()) {
            		total = total + entry.getValue().size();
            	}
                return total;
            }
        }));
	    
	    getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "HoloMobHealth has been Enabled!");
		
	}

	@Override
	public void onDisable() {		
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "HoloMobHealth has been Disabled!");
	}
	
	private static void canncelStartUpSelf() {
        Bukkit.getScheduler().cancelTask(startUpTaskId);
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
		
		if (HoloMobHealth.UseAlterHealth == false) {
			startUpTaskId = new BukkitRunnable() {  	    	
		    	public void run() {
		    		if (Bukkit.getOnlinePlayers().size() > 0) {	    			
		    			getEntities();
		    			sendHealth();
		    			canncelStartUpSelf();
		    		}
		    	}
		    }.runTaskTimer(HoloMobHealth.plugin, 1, 100).getTaskId();
		} else {
			startUpTaskId = new BukkitRunnable() {  	    	
		    	public void run() {
		    		if (Bukkit.getOnlinePlayers().size() > 0) {	    			
		    			getEntities();
		    			sendAltHealth();
		    			canncelStartUpSelf();
		    		}
		    	}
		    }.runTaskTimer(HoloMobHealth.plugin, 1, 100).getTaskId();
		}
	}
	
	public static void getEntities() {
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					List<Entity> inRange = player.getNearbyEntities(range, range, range);
					List<Entity> inRangePlus10 = player.getNearbyEntities(range + 10, range + 10, range + 10);
					HoloMobHealth.nearbyEntities.put(player, inRange);
					HoloMobHealth.nearbyPlus10Entities.put(player, inRangePlus10);
				}
			}
		}.runTaskTimer(HoloMobHealth.plugin, 0, 10);
	}
	
	public static void sendAltHealth() {
		
		activeShowHealthTaskID = new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				List<Entity> remove = new ArrayList<Entity>();
				for (Entry<Entity, Long> entry : HoloMobHealth.altShowHealth.entrySet()) {
					long unix = System.currentTimeMillis();
					if (entry.getValue() < unix) {
						remove.add(entry.getKey());
					}
				}
				for (Entity entity : remove) {
					HoloMobHealth.altShowHealth.remove(entity);
				}
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (!player.hasPermission("holomobhealth.use")) {
						continue;
					}
					if (!HoloMobHealth.nearbyEntities.containsKey(player)) {
						continue;
					}
					List<Entity> inRange =  HoloMobHealth.nearbyEntities.get(player);
					for (Entity entity : HoloMobHealth.nearbyPlus10Entities.get(player)) {
						if (HoloMobHealth.DisabledMobTypes.contains(entity.getType())) {
							continue;
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
								if (contain == true) {
									continue;
								}
							}
						}
						if (EntityTypeUtils.getMobList().contains(entity.getType())) { 
							if ((!inRange.contains(entity)) || (!HoloMobHealth.altShowHealth.containsKey(entity))) {
								String name = entity.getCustomName();							
								boolean visible = entity.isCustomNameVisible();
								MetadataPacket.sendMetadataPacket(player, entity, name, visible);
								continue;
							}
							if (HoloMobHealth.applyToNamed == false) {
								if (entity.getCustomName() != null) {
									if (!entity.getCustomName().equals("")) {
										String name = entity.getCustomName();							
										boolean visible = entity.isCustomNameVisible();
										MetadataPacket.sendMetadataPacket(player, entity, name, visible);
										continue;
									}
								}	
							}
							String display = "";
							display = HoloMobHealth.DisplayText;
							if (display.contains("{Health_Rounded}")) {
								long health = Math.round(((LivingEntity) entity).getHealth());
								display = display.replace("{Health_Rounded}", String.valueOf(health));
							}
							if (display.contains("{Max_Health_Rounded}")) {
								long health = Math.round(((LivingEntity) entity).getMaxHealth());
								display = display.replace("{Max_Health_Rounded}", String.valueOf(health));
							}
							if (display.contains("{Health_1DB}")) {
								double health = (double) Math.round(((LivingEntity) entity).getHealth() * (double) 10) / (double) 10;
								display = display.replace("{Health_1DB}", String.valueOf(health));
							}
							if (display.contains("{Max_Health_1DB}")) {
								double health = (double) Math.round(((LivingEntity) entity).getMaxHealth() * (double) 10) / (double) 10;
								display = display.replace("{Max_Health_1DB}", String.valueOf(health));
							}
							if (display.contains("{Health_2DB}")) {
								double health = (double) Math.round(((LivingEntity) entity).getHealth() * (double) 10) / (double) 10;
								display = display.replace("{Health_2DB}", String.valueOf(health));
							}
							if (display.contains("{Max_Health_2DB}")) {
								double health = (double) Math.round(((LivingEntity) entity).getMaxHealth() * (double) 10) / (double) 10;
								display = display.replace("{Max_Health_2DB}", String.valueOf(health));
							}
							if (display.contains("{Health_Percentage}")) {
								long health = Math.round((((LivingEntity) entity).getHealth() / (double) ((LivingEntity) entity).getMaxHealth()) * 100);
								display = display.replace("{Health_Percentage}", String.valueOf(health));
							}
							if (display.contains("{Health_Percentage_1DB}")) {
								double health = (double) Math.round((((LivingEntity) entity).getHealth() / (double) ((LivingEntity) entity).getMaxHealth()) * 1000) / (double) 10;
								display = display.replace("{Health_Percentage_1DB}", String.valueOf(health));
							}
							if (display.contains("{Health_Percentage_2DB}")) {
								double health = (double) Math.round((((LivingEntity) entity).getHealth() / (double) ((LivingEntity) entity).getMaxHealth()) * 10000) / (double) 10;
								display = display.replace("{Health_Percentage_2DB}", String.valueOf(health));
							}
							if (display.contains("{Mob_Type}")) {
								String type = EntityTypeUtils.getMinecraftName(entity);
								display = display.replace("{Mob_Type}", type);
							}
							if (display.contains("{DynamicColor}")) {
								double healthpercentage = (((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth());
								String symbol = "";
								if (healthpercentage < 0.33) {
									symbol = HoloMobHealth.LowColor;
								} else if (healthpercentage < 0.67) {
									symbol = HoloMobHealth.HalfColor;
								} else {
									symbol = HoloMobHealth.HealthyColor;
								}
								display = display.replace("{DynamicColor}", symbol);
							}
							if (display.contains("{ScaledSymbols}")) {
								String symbol = "";
								double healthpercentagescaled = (((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth()) * (double) HoloMobHealth.heartScale;
								double i = 1;
								for (i = 1; i < healthpercentagescaled; i = i + 1) {
									symbol = symbol + HoloMobHealth.HealthyChar;
								}
								i = i - 1;
								if ((healthpercentagescaled - i) > 0 && (healthpercentagescaled - i) < 0.33) {
									symbol = symbol + HoloMobHealth.EmptyChar;
								} else if ((healthpercentagescaled - i) > 0 && (healthpercentagescaled - i) < 0.67) {
									symbol = symbol + HoloMobHealth.HalfChar;
								} else if ((healthpercentagescaled - i) > 0) {
									symbol = symbol + HoloMobHealth.HealthyChar;
								}
								for (i = HoloMobHealth.heartScale - 1; i >= healthpercentagescaled; i = i - 1) {
									symbol = symbol + HoloMobHealth.EmptyChar;
								}
								display = display.replace("{ScaledSymbols}", symbol);
							}
							
							display = ChatColor.translateAlternateColorCodes('&', display);
							
							if (display.contains("{Mob_Type_Or_Name}")) {
								String name = "";
								if (entity.getCustomName() != null) {
									name = ChatColor.RESET + entity.getCustomName();
								}
								if (name.equals("")) {
									name = ChatColor.translateAlternateColorCodes('&', EntityTypeUtils.getMinecraftName(entity));
								}
								display = display.replace("{Mob_Type_Or_Name}", String.valueOf(name));
							}
							MetadataPacket.sendMetadataPacket(player, entity, display, HoloMobHealth.alwaysShow);
						}
					}
				}
			}
		}.runTaskTimerAsynchronously(HoloMobHealth.plugin, 0, 3).getTaskId();
	}
	
	public static void sendHealth() {
		
		activeShowHealthTaskID = new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (!player.hasPermission("holomobhealth.use")) {
						continue;
					}
					if (!HoloMobHealth.nearbyEntities.containsKey(player)) {
						continue;
					}
					List<Entity> inRange =  HoloMobHealth.nearbyEntities.get(player);
					for (Entity entity : HoloMobHealth.nearbyPlus10Entities.get(player)) {
						if (HoloMobHealth.DisabledMobTypes.contains(entity.getType())) {
							continue;
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
								if (contain == true) {
									continue;
								}
							}
						}
						if (EntityTypeUtils.getMobList().contains(entity.getType())) { 
							if (!inRange.contains(entity)) {
								String name = entity.getCustomName();							
								boolean visible = entity.isCustomNameVisible();
								MetadataPacket.sendMetadataPacket(player, entity, name, visible);
								continue;
							}
							if (HoloMobHealth.applyToNamed == false) {
								if (entity.getCustomName() != null) {
									if (!entity.getCustomName().equals("")) {
										String name = entity.getCustomName();
										boolean visible = entity.isCustomNameVisible();
										MetadataPacket.sendMetadataPacket(player, entity, name, visible);
										continue;
									}
								}	
							}
							String display = "";
							display = HoloMobHealth.DisplayText;
							if (display.contains("{Health_Rounded}")) {
								long health = Math.round(((LivingEntity) entity).getHealth());
								display = display.replace("{Health_Rounded}", String.valueOf(health));
							}
							if (display.contains("{Max_Health_Rounded}")) {
								long health = Math.round(((LivingEntity) entity).getMaxHealth());
								display = display.replace("{Max_Health_Rounded}", String.valueOf(health));
							}
							if (display.contains("{Health_1DB}")) {
								double health = (double) Math.round(((LivingEntity) entity).getHealth() * (double) 10) / (double) 10;
								display = display.replace("{Health_1DB}", String.valueOf(health));
							}
							if (display.contains("{Max_Health_1DB}")) {
								double health = (double) Math.round(((LivingEntity) entity).getMaxHealth() * (double) 10) / (double) 10;
								display = display.replace("{Max_Health_1DB}", String.valueOf(health));
							}
							if (display.contains("{Health_2DB}")) {
								double health = (double) Math.round(((LivingEntity) entity).getHealth() * (double) 10) / (double) 10;
								display = display.replace("{Health_2DB}", String.valueOf(health));
							}
							if (display.contains("{Max_Health_2DB}")) {
								double health = (double) Math.round(((LivingEntity) entity).getMaxHealth() * (double) 10) / (double) 10;
								display = display.replace("{Max_Health_2DB}", String.valueOf(health));
							}
							if (display.contains("{Health_Percentage}")) {
								long health = Math.round((((LivingEntity) entity).getHealth() / (double) ((LivingEntity) entity).getMaxHealth()) * 100);
								display = display.replace("{Health_Percentage}", String.valueOf(health));
							}
							if (display.contains("{Health_Percentage_1DB}")) {
								double health = (double) Math.round((((LivingEntity) entity).getHealth() / (double) ((LivingEntity) entity).getMaxHealth()) * 1000) / (double) 10;
								display = display.replace("{Health_Percentage_1DB}", String.valueOf(health));
							}
							if (display.contains("{Health_Percentage_2DB}")) {
								double health = (double) Math.round((((LivingEntity) entity).getHealth() / (double) ((LivingEntity) entity).getMaxHealth()) * 10000) / (double) 10;
								display = display.replace("{Health_Percentage_2DB}", String.valueOf(health));
							}
							if (display.contains("{Mob_Type}")) {
								String type = EntityTypeUtils.getMinecraftName(entity);
								display = display.replace("{Mob_Type}", type);
							}
							if (display.contains("{DynamicColor}")) {
								double healthpercentage = (((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth());
								String symbol = "";
								if (healthpercentage < 0.33) {
									symbol = HoloMobHealth.LowColor;
								} else if (healthpercentage < 0.67) {
									symbol = HoloMobHealth.HalfColor;
								} else {
									symbol = HoloMobHealth.HealthyColor;
								}
								display = display.replace("{DynamicColor}", symbol);
							}
							if (display.contains("{ScaledSymbols}")) {
								String symbol = "";
								double healthpercentagescaled = (((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth()) * (double) HoloMobHealth.heartScale;
								double i = 1;
								for (i = 1; i < healthpercentagescaled; i = i + 1) {
									symbol = symbol + HoloMobHealth.HealthyChar;
								}
								i = i - 1;
								if ((healthpercentagescaled - i) > 0 && (healthpercentagescaled - i) < 0.33) {
									symbol = symbol + HoloMobHealth.EmptyChar;
								} else if ((healthpercentagescaled - i) > 0 && (healthpercentagescaled - i) < 0.67) {
									symbol = symbol + HoloMobHealth.HalfChar;
								} else if ((healthpercentagescaled - i) > 0) {
									symbol = symbol + HoloMobHealth.HealthyChar;
								}
								for (i = HoloMobHealth.heartScale - 1; i >= healthpercentagescaled; i = i - 1) {
									symbol = symbol + HoloMobHealth.EmptyChar;
								}
								display = display.replace("{ScaledSymbols}", symbol);
							}
							
							display = ChatColor.translateAlternateColorCodes('&', display);
							
							if (display.contains("{Mob_Type_Or_Name}")) {
								String name = "";
								if (entity.getCustomName() != null) {
									name = ChatColor.RESET + entity.getCustomName();
								}
								if (name.equals("")) {
									name = ChatColor.translateAlternateColorCodes('&', EntityTypeUtils.getMinecraftName(entity));
								}
								display = display.replace("{Mob_Type_Or_Name}", String.valueOf(name));
							}
							MetadataPacket.sendMetadataPacket(player, entity, display, HoloMobHealth.alwaysShow);
						}
					}
				}
			}
		}.runTaskTimerAsynchronously(HoloMobHealth.plugin, 0, 3).getTaskId();
	}
}