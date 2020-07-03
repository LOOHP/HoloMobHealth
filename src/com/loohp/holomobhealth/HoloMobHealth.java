package com.loohp.holomobhealth;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.Database.Database;
import com.loohp.holomobhealth.Debug.Debug;
import com.loohp.holomobhealth.Holders.HoloMobArmorStand;
import com.loohp.holomobhealth.Listeners.Events;
import com.loohp.holomobhealth.Metrics.Metrics;
import com.loohp.holomobhealth.Modules.ArmorstandDisplay;
import com.loohp.holomobhealth.Modules.NameTagDisplay;
import com.loohp.holomobhealth.Protocol.ArmorStandPacket;
import com.loohp.holomobhealth.Updater.Updater;
import com.loohp.holomobhealth.Utils.BoundingBoxUtils;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;

public class HoloMobHealth extends JavaPlugin {
	
	public static Plugin plugin = null;
	
	public static MCVersion version;
	
	public static ProtocolManager protocolManager;
	
	public static int activeShowHealthTaskID = -1;
	
	public static List<String> DisplayText = new ArrayList<String>();
	
	public static int heartScale = 10;
	
	public static List<Player> playersEnabled = new LinkedList<Player>();
	
	public static String HealthyColor = "&a";
	public static String HalfColor = "&e";
	public static String LowColor = "&c";
	
	public static String HealthyChar = "&c❤";
	public static String HalfChar = "&e❤";
	public static String EmptyChar = "&7❤";
	
	public static boolean alwaysShow = true;
	public static int range = 15;
	public static int updateRange = 45;
	public static boolean applyToNamed = false;
	
	public static String ReloadPlugin = "";	
	public static String NoPermission = "";
	public static String PlayersOnly = "";
	public static String PlayersNotFound = "";
	public static String ToggleDisplayOn = "";
	public static String ToggleDisplayOff = "";
	
	public static Set<Entity> nearbyEntities = new HashSet<Entity>();
	public static Set<Entity> nearbyPlus10Entities = new HashSet<Entity>();
	
	public static Set<Entity> updateQueue = new LinkedHashSet<Entity>();
	
	public static Set<EntityType> DisabledMobTypes = new HashSet<EntityType>();
	public static Set<String> DisabledMobNamesAbsolute = new HashSet<String>();
	public static List<String> DisabledMobNamesContains = new ArrayList<String>();
	public static Set<String> DisabledWorlds = new HashSet<String>();
	
	public static boolean UseAlterHealth = false;
	public static int AltHealthDisplayTime = 3;
	public static boolean AltOnlyPlayer = false;
	public static HashMap<Entity, Long> altShowHealth = new HashMap<Entity, Long>();
	
	public static boolean MythicHook = false;
	public static boolean showMythicMobs = true;
	
	public static boolean CitizensHook = false;
	public static boolean showCitizens = true;
	
	public static boolean armorStandMode = false;
	public static int armorStandYOffset = 0;
	
	public static HashMap<EntityType, Integer> specialTypeOffset = new HashMap<EntityType, Integer>();
	public static HashMap<String, Integer> specialNameOffset = new HashMap<String, Integer>();
	
	public static int mobsPerTick = 4;
	
	public static boolean UpdaterEnabled = true;
	
	@Override
	public void onEnable() {	
		plugin = (Plugin)getServer().getPluginManager().getPlugin("HoloMobHealth");
		
		getServer().getPluginManager().registerEvents(new Debug(), this);

		int pluginId = 6749;

		Metrics metrics = new Metrics(this, pluginId);
		
		version = MCVersion.fromPackageName(getServer().getClass().getPackage().getName());
		
        if (!version.isSupported()) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
        }
		
		protocolManager = ProtocolLibrary.getProtocolManager();
        
        getServer().getPluginManager().registerEvents(new Events(), this);
		
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		
		loadConfig();
		
		if (armorStandMode) {
			getServer().getPluginManager().registerEvents(new ArmorStandPacket(), plugin);
			ArmorStandPacket.update();
		}
		
		BoundingBoxUtils.setup();
		
	    getCommand("holomobhealth").setExecutor(new Commands());
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Citizens") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into Citizens!");
	    	CitizensHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into MythicMobs!");
	    	MythicHook = true;
		}
		
	    EntityTypeUtils.setUpList();
		EntityTypeUtils.setupLang();
		
		addEntities();
		removeEntities();
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_mobs_displaying", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return nearbyPlus10Entities.size();
            }
        }));
		
		if (UpdaterEnabled) {
			getServer().getPluginManager().registerEvents(new Updater(), this);
		}
	    
	    getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[HoloMobHealth] HoloMobHealth has been Enabled!");
	    
	    Bukkit.getScheduler().runTask(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				Bukkit.getScheduler().runTaskAsynchronously(HoloMobHealth.plugin, () -> {
					if (!Database.playerExists(player)) {
						Database.createPlayer(player);
					}
					Database.loadPlayer(player);
				});
			}
			
			if (armorStandMode) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					ArmorStandPacket.playerStatus.put(player, Collections.newSetFromMap(new ConcurrentHashMap<HoloMobArmorStand, Boolean>()));
				}
			}
		});
	    
	    Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
			if (UpdaterEnabled) {
				String version = Updater.checkUpdate();
				if (!version.equals("latest")) {
					Updater.sendUpdateMessage(Bukkit.getConsoleSender(), version);
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.hasPermission("holomobhealth.update")) {
							Updater.sendUpdateMessage(player, version);
						}
					}
				}
			}
		}, 100);
		
	}

	@Override
	public void onDisable() {		
		if (!Bukkit.getOnlinePlayers().isEmpty()) {
			if (armorStandMode) {
				getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[HoloMobHealth] Plugin reload detected, attempting to despawn all visual entities. If anything went wrong, please restart! (Reloads are always not recommended)");
				int [] entityIdArray = new int[ArmorStandPacket.active.size()];
				int i = 0;
				for (HoloMobArmorStand stand : ArmorStandPacket.active) {
					entityIdArray[i] = stand.getEntityId();
					i++;
				}
				
				PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
				packet1.getIntegerArrays().write(0, entityIdArray);
				
				try {
					for (Player player : Bukkit.getOnlinePlayers()) {
						protocolManager.sendServerPacket(player, packet1);
					}
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] HoloMobHealth has been Disabled!");
	}
	
	@SuppressWarnings("deprecation")
	public static void loadConfig() {
		mobsPerTick = plugin.getConfig().getInt("Settings.MobsPerTick");
		
		specialNameOffset.clear();
		specialTypeOffset.clear();
		/*
		if (plugin.getConfig().contains("Options.MultiLine.Enable")) {
			List<String> list = new ArrayList<String>();
			list.add(plugin.getConfig().getString("Display.Text"));
			plugin.getConfig().set("Display.Text", list);
			plugin.saveConfig();
		}
		*/
		
		DisplayText = plugin.getConfig().getStringList("Display.Text");
		
		heartScale = plugin.getConfig().getInt("Display.ScaledSymbolSettings.Scale");
		
		HealthyColor = plugin.getConfig().getString("Display.DynamicColorSettings.HealthyColor");
		HalfColor = plugin.getConfig().getString("Display.DynamicColorSettings.HalfColor");
		LowColor = plugin.getConfig().getString("Display.DynamicColorSettings.LowColor");
		
		HealthyChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.HealthyChar");
		HalfChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.HalfChar");
		EmptyChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.EmptyChar");
		
		alwaysShow = plugin.getConfig().getBoolean("Options.AlwaysShow");
		range = plugin.getConfig().getInt("Options.Range");
		updateRange = range + 30;
		applyToNamed = plugin.getConfig().getBoolean("Options.ApplyToNamed");
		
		ReloadPlugin = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ReloadPlugin"));
		NoPermission = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission"));
		PlayersOnly = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.PlayersOnly"));
		PlayersNotFound = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.PlayerNotFound"));
		ToggleDisplayOn = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ToggleDisplayOn"));
		ToggleDisplayOff = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ToggleDisplayOff"));
		
		List<String> types = plugin.getConfig().getStringList("Options.DisabledMobTypes");
		for (String each : types) {
			if (version.isLegacy()) {
				DisabledMobTypes.add(EntityType.fromName(each.toUpperCase()));
			} else {
				DisabledMobTypes.add(EntityType.valueOf(each.toUpperCase()));
			}
		}
		DisabledMobNamesAbsolute = plugin.getConfig().getStringList("Options.DisabledMobNamesAbsolute").stream().collect(Collectors.toSet());
		DisabledMobNamesContains = plugin.getConfig().getStringList("Options.DisabledMobNamesContains");
		
		DisabledWorlds = plugin.getConfig().getStringList("Options.DisabledWorlds").stream().collect(Collectors.toSet());
		
		UseAlterHealth = plugin.getConfig().getBoolean("Options.DynamicHealthDisplay.Use");
		AltHealthDisplayTime = plugin.getConfig().getInt("Options.DynamicHealthDisplay.Timeout");
		AltOnlyPlayer = plugin.getConfig().getBoolean("Options.DynamicHealthDisplay.OnlyPlayerTrigger");
		
		armorStandMode = plugin.getConfig().getBoolean("Options.MultiLine.Enable");
		armorStandYOffset = plugin.getConfig().getInt("Options.MultiLine.MasterYOffset");
		List<String> armorStandSpecial = plugin.getConfig().getStringList("Options.MultiLine.Special");
		for (String cases : armorStandSpecial) {
			int offset = Integer.valueOf(cases.substring(cases.lastIndexOf(":") + 1));
			switch (cases.substring(0, cases.indexOf(":")).toLowerCase()) {
			case "name":
				String regex = cases.substring(cases.indexOf(":") + 1, cases.lastIndexOf(":"));
				specialNameOffset.put(regex, offset);
				break;
			case "type":
				String type = cases.substring(cases.indexOf(":") + 1, cases.lastIndexOf(":"));
				EntityType entitytype = EntityType.valueOf(type.toUpperCase());
				specialTypeOffset.put(entitytype, offset);
				break;
			}
		}
		
		if (activeShowHealthTaskID >= 0) {
			Bukkit.getScheduler().cancelTask(activeShowHealthTaskID);
		}
		
		if (!armorStandMode || version.isOld() || version.equals(MCVersion.V1_9) || version.equals(MCVersion.V1_9_4)) {
			if (!UseAlterHealth) {  			
				NameTagDisplay.sendHealth();
			} else {    			
				NameTagDisplay.sendAltHealth(); 
			}
			if (armorStandMode) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[HoloMobHealth] Multi-line is not supported on this version of Minecraft. Using Single line instead!");
			}
		} else {
			if (!UseAlterHealth) {
				ArmorstandDisplay.sendHealth();
			} else {    			
				ArmorstandDisplay.sendAltHealth(); 
			}
		}
		
		showCitizens = plugin.getConfig().getBoolean("Hooks.Citizens.ShowNPCMobHealth");
		
		showMythicMobs = plugin.getConfig().getBoolean("Hooks.MythicMobs.ShowMythicMobsHealth");
		
		UpdaterEnabled = plugin.getConfig().getBoolean("Updater.Enable");
	}
	
	public static void addEntities() {
		Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			int delay = 1;
			int count = 0;
			int maxper = (int) Math.ceil((double) Bukkit.getOnlinePlayers().size() / (double) 20);
			for (Player eachPlayer : Bukkit.getOnlinePlayers().stream().filter(each -> !DisabledWorlds.contains(each.getWorld().getName())).collect(Collectors.toList())) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				UUID uuid = eachPlayer.getUniqueId();
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					if (Bukkit.getPlayer(uuid) == null) {
						return;
					}
					Player player = Bukkit.getPlayer(uuid);
					
					List<Entity> inRange = player.getNearbyEntities(range, range, range);
					nearbyEntities.addAll(inRange);
					updateQueue.addAll(inRange);
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
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.getWorld().equals(entity.getWorld())) {
						if (player.getLocation().distanceSquared(entity.getLocation()) <= ((range + 10) * (range + 10))) {
							return;
						}
					}
				}
				nearbyEntities.remove(entity);
				updateQueue.add(entity);
				Bukkit.getScheduler().runTaskLater(plugin, () -> nearbyPlus10Entities.remove(entity), 10);
			}, delay);
		}
		next = next + delay;
		Bukkit.getScheduler().runTaskLater(plugin, () -> removeEntities(), next);
	}
	
}