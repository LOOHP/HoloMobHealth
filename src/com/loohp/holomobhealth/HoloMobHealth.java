package com.loohp.holomobhealth;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
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
import com.loohp.holomobhealth.Modules.RangeModule;
import com.loohp.holomobhealth.Protocol.ArmorStandPacket;
import com.loohp.holomobhealth.Updater.Updater;
import com.loohp.holomobhealth.Updater.Updater.UpdaterResponse;
import com.loohp.holomobhealth.Utils.ChatColorUtils;
import com.loohp.holomobhealth.Utils.DisplayTextCacher;
import com.loohp.holomobhealth.Utils.EntityTypeUtils;
import com.loohp.holomobhealth.Utils.LegacyPlaceholdersConverter;
import com.loohp.holomobhealth.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class HoloMobHealth extends JavaPlugin {
	
	public static Plugin plugin = null;
	
	public static MCVersion version;
	
	public static ProtocolManager protocolManager;
	
	public static int activeShowHealthTaskID = -1;
	
	public static List<String> DisplayText = new ArrayList<String>();
	
	public static int heartScale = 10;
	public static boolean dynamicScale = true;
	
	public static Set<Player> playersEnabled = ConcurrentHashMap.newKeySet();
	
	public static String HealthyColor = "&a";
	public static String HalfColor = "&e";
	public static String LowColor = "&c";
	
	public static String HealthyChar = "&c❤";
	public static String HalfChar = "&e❤";
	public static String EmptyChar = "&7❤";
	
	public static boolean alwaysShow = true;
	public static boolean applyToNamed = false;
	
	public static String ReloadPlugin = "";	
	public static String NoPermission = "";
	public static String PlayersOnly = "";
	public static String PlayersNotFound = "";
	public static String ToggleDisplayOn = "";
	public static String ToggleDisplayOff = "";
	
	public static Set<EntityType> DisabledMobTypes = new HashSet<EntityType>();
	public static Set<String> DisabledMobNamesAbsolute = new HashSet<String>();
	public static List<String> DisabledMobNamesContains = new ArrayList<String>();
	public static Set<String> DisabledWorlds = new HashSet<String>();
	
	public static boolean UseAlterHealth = false;
	public static int AltHealthDisplayTime = 3;
	public static boolean AltOnlyPlayer = false;
	public static Map<UUID, Long> altShowHealth = new ConcurrentHashMap<>();
	
	public static boolean MythicHook = false;
	public static boolean showMythicMobs = true;
	
	public static boolean CitizensHook = false;
	public static boolean showCitizens = true;
	
	public static boolean ShopkeepersHook = false;
	public static boolean showShopkeepers = true;
	
	public static boolean MyPetHook = false;
	public static boolean showMyPet = true;
	
	public static boolean UltimateStackerHook = false;
	
	public static boolean armorStandMode = false;
	public static int armorStandYOffset = 0;
	
	public static HashMap<EntityType, Integer> specialTypeOffset = new HashMap<EntityType, Integer>();
	public static HashMap<String, Integer> specialNameOffset = new HashMap<String, Integer>();
	
	public static boolean rangeEnabled = false;
	
	public static boolean legacyChatAPI = false;
	
	public static boolean UpdaterEnabled = true;
	
	@Override
	public void onEnable() {	
		plugin = this;
		
		version = MCVersion.fromPackageName(getServer().getClass().getPackage().getName());
		
		getServer().getPluginManager().registerEvents(new Debug(), this);

		int pluginId = 6749;

		Metrics metrics = new Metrics(this, pluginId);
		
        if (!version.isSupported()) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
        }
		
		protocolManager = ProtocolLibrary.getProtocolManager();
        
        getServer().getPluginManager().registerEvents(new Events(), this);
		
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        //Legacy Placeholders Converter
        List<String> lines = plugin.getConfig().getStringList("Display.Text");
        if (LegacyPlaceholdersConverter.getLegacyPlaceholderSet().stream().anyMatch(each -> lines.stream().anyMatch(line -> line.contains(each)))) {
        	LegacyPlaceholdersConverter.convert();
        }
		
		loadConfig();
		
		if (armorStandMode) {
			getServer().getPluginManager().registerEvents(new ArmorStandPacket(), plugin);
			ArmorStandPacket.update();
		}
		
	    getCommand("holomobhealth").setExecutor(new Commands());
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Citizens") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into Citizens!");
	    	CitizensHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into MythicMobs!");
	    	MythicHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Shopkeepers") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into Shopkeepers!");
	    	ShopkeepersHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("MyPet") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into MyPet!");
	    	MyPetHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("UltimateStacker") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into UltimateStacker!");
	    	UltimateStackerHook = true;
		}
		
	    EntityTypeUtils.setUpList();
		EntityTypeUtils.setupLang();
		
		try {
			TextComponent test = new TextComponent("Legacy Bungeecord Chat API Test");
			test.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Text("Test Hover Text")));
			test.getHoverEvent().getContents();
			legacyChatAPI = false;
		} catch (Throwable e) {
			legacyChatAPI = true;
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[HoloMobHealth] Legacy Bungeecord Chat API detected, using legacy methods...");
		};
		
		if (rangeEnabled) {
			RangeModule.reloadNumbers();
			RangeModule.run();
		}
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_mobs_displaying", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return Bukkit.getWorlds().stream().mapToInt(world -> world.getEntities().size()).sum();
            }
        }));
		
		if (UpdaterEnabled) {
			getServer().getPluginManager().registerEvents(new Updater(), this);
		}
		
		ArmorstandDisplay.run();
	    
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
				UpdaterResponse version = Updater.checkUpdate();
				if (!version.getResult().equals("latest")) {
					Updater.sendUpdateMessage(Bukkit.getConsoleSender(), version.getResult(), version.getSpigotPluginId());
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.hasPermission("holomobhealth.update")) {
							Updater.sendUpdateMessage(player, version.getResult(), version.getSpigotPluginId());
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
		plugin.getConfig().set("Settings.MobsPerTick", null);
		plugin.getConfig().set("Settings", null);
		plugin.saveConfig();
		
		rangeEnabled = plugin.getConfig().getBoolean("Options.Range.Use");
		
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
		
		DisplayText = DisplayTextCacher.cacheDecimalFormat(plugin.getConfig().getStringList("Display.Text"));
		
		heartScale = plugin.getConfig().getInt("Display.ScaledSymbolSettings.Scale");
		dynamicScale = plugin.getConfig().getBoolean("Display.ScaledSymbolSettings.DynamicScale");
		
		HealthyColor = plugin.getConfig().getString("Display.DynamicColorSettings.HealthyColor");
		HalfColor = plugin.getConfig().getString("Display.DynamicColorSettings.HalfColor");
		LowColor = plugin.getConfig().getString("Display.DynamicColorSettings.LowColor");
		
		HealthyChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.HealthyChar");
		HalfChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.HalfChar");
		EmptyChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.EmptyChar");
		
		alwaysShow = plugin.getConfig().getBoolean("Options.AlwaysShow");
		applyToNamed = plugin.getConfig().getBoolean("Options.ApplyToNamed");
		
		ReloadPlugin = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ReloadPlugin"));
		NoPermission = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission"));
		PlayersOnly = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.PlayersOnly"));
		PlayersNotFound = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.PlayerNotFound"));
		ToggleDisplayOn = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ToggleDisplayOn"));
		ToggleDisplayOff = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ToggleDisplayOff"));
		
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
		
		HoloMobHealth.protocolManager.removePacketListeners(plugin);
		if (!armorStandMode || version.isOld() || version.equals(MCVersion.V1_9) || version.equals(MCVersion.V1_9_4)) {
			NameTagDisplay.entityMetadataPacketListener();
			if (armorStandMode) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[HoloMobHealth] Multi-line is not supported on this version of Minecraft. Using Single line instead!");
				armorStandMode = false;
			}
		} else {
			ArmorstandDisplay.entityMetadataPacketListener();
		}
		
		showCitizens = plugin.getConfig().getBoolean("Hooks.Citizens.ShowNPCMobHealth");		
		showMythicMobs = plugin.getConfig().getBoolean("Hooks.MythicMobs.ShowMythicMobsHealth");
		showShopkeepers = plugin.getConfig().getBoolean("Hooks.Shopkeepers.ShowShopkeepersHealth");
		showMyPet = plugin.getConfig().getBoolean("Hooks.MyPet.ShowMyPetHealth");
		
		UpdaterEnabled = plugin.getConfig().getBoolean("Updater.Enable");
	}
	
	public static int getUpdateRange(World world) {
		if (version.isOld()) {
			return 80;
		}
		return Math.min((version.isPost1_16() ? world.getViewDistance() : Bukkit.getViewDistance()) * 16 / 2, 80);
	}
	
}