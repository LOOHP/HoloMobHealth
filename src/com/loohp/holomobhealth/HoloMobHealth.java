package com.loohp.holomobhealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.World;
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
import com.loohp.holomobhealth.Listeners.Events;
import com.loohp.holomobhealth.Metrics.Metrics;
import com.loohp.holomobhealth.Updater.Updater;
import com.loohp.holomobhealth.Utils.CitizensUtils;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.MetadataPacket;
import com.loohp.holomobhealth.Utils.MythicMobsUtils;

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
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R2") == true) {
	    	version = "OLDlegacy1.8.3";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R1") == true) {
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
		
		if (HoloMobHealth.UseAlterHealth == false) {  			
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
		if (UpdaterEnabled == true) {
			Updater.updaterInterval();
		}
	}
	
	public static void addEntities() {
		new BukkitRunnable() {
			public void run() {
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
					new BukkitRunnable() {
						public void run() {
							if (Bukkit.getPlayer(uuid) == null) {
								return;
							}
							Player player = Bukkit.getPlayer(uuid);
							
							nearbyEntities.addAll(player.getNearbyEntities(range, range, range));
							nearbyPlus10Entities.addAll(player.getNearbyEntities(range + 10, range + 10, range + 10));
						}
					}.runTaskLater(HoloMobHealth.plugin, delay);
				}
			}
		}.runTaskTimer(HoloMobHealth.plugin, 0, 20);
	}
	
	public static void removeEntities() {
		int next = 2;
		int delay = 1;
		int count = 0;
		Queue<Entity> allentites = new LinkedList<Entity>();
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (entity instanceof LivingEntity && !(entity instanceof Player)) {
					allentites.add(entity);
				}
			}
		}
		int size = allentites.size();
		for (int i = 0; i < size; i++) {
			count++;
			if (count > 5) {
				count = 0;
				delay++;
			}
			new BukkitRunnable() {
				public void run() {
					Entity rawentity = allentites.poll();
					if (rawentity == null) {
						return;
					}
					if (rawentity.isValid() == false) {
						return;
					}
					LivingEntity entity = (LivingEntity) rawentity;
					for (Entity each : entity.getNearbyEntities(range, range, range)) {
						if (each instanceof Player) {
							return;
						}
					}
					nearbyPlus10Entities.remove(entity);
					nearbyEntities.remove(entity);
				}
			}.runTaskLater(HoloMobHealth.plugin, delay);
		}
		next = next + delay;
		Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> removeEntities(), next);
	}
	
	public static void sendAltHealth() {
		
		activeShowHealthTaskID = new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
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
					
					new BukkitRunnable() {
						public void run() {
							if (entity.isValid() == false) {
								return;
							}
							
							if (HoloMobHealth.DisabledMobTypes.contains(entity.getType())) {
								return;
							}
							if (showCitizens == false && CitizensHook == true) {
								if (CitizensUtils.isNPC(entity)) {
									return;
								}
							}
							if (showMythicMobs == false && MythicHook == true) {
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
									if (contain == true) {
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
								if (HoloMobHealth.applyToNamed == false) {
									if (entity.getCustomName() != null) {
										if (!entity.getCustomName().equals("")) {
											String name = entity.getCustomName();							
											boolean visible = entity.isCustomNameVisible();
											MetadataPacket.sendMetadataPacket(entity, name, visible);
											return;
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
								MetadataPacket.sendMetadataPacket(entity, display, HoloMobHealth.alwaysShow);
							}
						}
					}.runTaskLaterAsynchronously(HoloMobHealth.plugin, delay);
				}
			}
		}.runTaskTimerAsynchronously(HoloMobHealth.plugin, 0, 3).getTaskId();
	}
	
	public static void sendHealth() {
		
		activeShowHealthTaskID = new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
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
					
					new BukkitRunnable() {
						public void run() {
							if (entity.isValid() == false) {
								return;
							}

							if (HoloMobHealth.DisabledMobTypes.contains(entity.getType())) {
								return;
							}
							if (showCitizens == false && CitizensHook == true) {
								if (CitizensUtils.isNPC(entity)) {
									return;
								}
							}
							if (showMythicMobs == false && MythicHook == true) {
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
									if (contain == true) {
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
								if (HoloMobHealth.applyToNamed == false) {
									if (entity.getCustomName() != null) {
										if (!entity.getCustomName().equals("")) {
											String name = entity.getCustomName();
											boolean visible = entity.isCustomNameVisible();
											MetadataPacket.sendMetadataPacket(entity, name, visible);
											return;
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
								MetadataPacket.sendMetadataPacket(entity, display, HoloMobHealth.alwaysShow);
							}
						}
					}.runTaskLaterAsynchronously(HoloMobHealth.plugin, delay);
				}
			}
		}.runTaskTimerAsynchronously(HoloMobHealth.plugin, 0, 3).getTaskId();
	}
}