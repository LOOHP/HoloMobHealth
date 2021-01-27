package com.loohp.holomobhealth;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import com.loohp.holomobhealth.Metrics.Charts;
import com.loohp.holomobhealth.Metrics.Metrics;
import com.loohp.holomobhealth.Modules.ArmorstandDisplay;
import com.loohp.holomobhealth.Modules.NameTagDisplay;
import com.loohp.holomobhealth.Modules.RangeModule;
import com.loohp.holomobhealth.Protocol.ArmorStandPacket;
import com.loohp.holomobhealth.Registries.CustomPlaceholderScripts;
import com.loohp.holomobhealth.Registries.DisplayTextCacher;
import com.loohp.holomobhealth.Updater.Updater;
import com.loohp.holomobhealth.Updater.Updater.UpdaterResponse;
import com.loohp.holomobhealth.Utils.ChatColorUtils;
import com.loohp.holomobhealth.Utils.JarUtils;
import com.loohp.holomobhealth.Utils.JarUtils.CopyOption;
import com.loohp.holomobhealth.Utils.LegacyPlaceholdersConverter;
import com.loohp.holomobhealth.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class HoloMobHealth extends JavaPlugin {
	
	public static final int BSTATS_PLUGIN_ID = 6749;
	
	public static Plugin plugin = null;
	
	public static MCVersion version;
	
	public static ProtocolManager protocolManager;
	
	public static int activeShowHealthTaskID = -1;
	
	public static List<String> DisplayText = new ArrayList<String>();
	
	public static int heartScale = 10;
	public static boolean dynamicScale = true;
	
	public static Set<Player> playersEnabled = ConcurrentHashMap.newKeySet();
	
	public static String healthyColor = "&a";
	public static String halfColor = "&e";
	public static String lowColor = "&c";
	
	public static String healthyChar = "&c❤";
	public static String halfChar = "&e❤";
	public static String emptyChar = "&7❤";
	
	public static boolean alwaysShow = true;
	public static boolean applyToNamed = false;
	
	public static String reloadPluginMessage = "";	
	public static String noPermissionMessage = "";
	public static String playersOnlyMessage = "";
	public static String playersNotFoundMessage = "";
	public static String toggleDisplayOnMessage = "";
	public static String toggleDisplayOffMessage = "";
	
	public static Set<EntityType> disabledMobTypes = new HashSet<EntityType>();
	public static Set<String> disabledMobNamesAbsolute = new HashSet<String>();
	public static List<String> disabledMobNamesContains = new ArrayList<String>();
	public static Set<String> disabledWorlds = new HashSet<String>();
	
	public static boolean useAlterHealth = false;
	public static int altHealthDisplayTime = 3;
	public static boolean altOnlyPlayer = false;
	public static Map<UUID, Long> altShowHealth = new ConcurrentHashMap<>();
	
	public static boolean placeholderAPIHook = false;
	
	public static boolean mythicHook = false;
	public static boolean showMythicMobs = true;
	
	public static boolean citizensHook = false;
	public static boolean showCitizens = true;
	
	public static boolean shopkeepersHook = false;
	public static boolean showShopkeepers = true;
	
	public static boolean myPetHook = false;
	public static boolean showMyPet = true;
	
	public static boolean ultimateStackerHook = false;
	
	public static boolean armorStandMode = false;
	public static int armorStandYOffset = 0;
	
	public static HashMap<EntityType, Integer> specialTypeOffset = new HashMap<EntityType, Integer>();
	public static HashMap<String, Integer> specialNameOffset = new HashMap<String, Integer>();
	
	public static boolean rangeEnabled = false;
	
	public static boolean legacyChatAPI = false;
	
	public static boolean updaterEnabled = true;
	
	@Override
	public void onEnable() {	
		plugin = this;
		
		version = MCVersion.fromPackageName(getServer().getClass().getPackage().getName());
		
		getServer().getPluginManager().registerEvents(new Debug(), this);

		Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
		Charts.setup(metrics);
		
        if (!version.isSupported()) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
        }
		
		protocolManager = ProtocolLibrary.getProtocolManager();
        
        getServer().getPluginManager().registerEvents(new Events(), this);
		
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        try {
			JarUtils.copyFolderFromJar("placeholder_scripts", getDataFolder(), CopyOption.COPY_IF_NOT_EXIST);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
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
	    	citizensHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into MythicMobs!");
	    	mythicHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Shopkeepers") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into Shopkeepers!");
	    	shopkeepersHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("MyPet") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into MyPet!");
	    	myPetHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("UltimateStacker") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into UltimateStacker!");
	    	ultimateStackerHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into PlaceholderAPI!");
	    	placeholderAPIHook = true;
		}
		
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
		
		if (updaterEnabled) {
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
			if (updaterEnabled) {
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
				int [] entityIdArray = ArmorStandPacket.active.stream().mapToInt(each -> each.getEntityId()).toArray();
				
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
		rangeEnabled = plugin.getConfig().getBoolean("Options.Range.Use");
		
		specialNameOffset.clear();
		specialTypeOffset.clear();
		
		DisplayText = DisplayTextCacher.cacheDecimalFormat(plugin.getConfig().getStringList("Display.Text"));
		
		heartScale = plugin.getConfig().getInt("Display.ScaledSymbolSettings.Scale");
		dynamicScale = plugin.getConfig().getBoolean("Display.ScaledSymbolSettings.DynamicScale");
		
		healthyColor = plugin.getConfig().getString("Display.DynamicColorSettings.HealthyColor");
		halfColor = plugin.getConfig().getString("Display.DynamicColorSettings.HalfColor");
		lowColor = plugin.getConfig().getString("Display.DynamicColorSettings.LowColor");
		
		healthyChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.HealthyChar");
		halfChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.HalfChar");
		emptyChar = plugin.getConfig().getString("Display.ScaledSymbolSettings.EmptyChar");
		
		alwaysShow = plugin.getConfig().getBoolean("Options.AlwaysShow");
		applyToNamed = plugin.getConfig().getBoolean("Options.ApplyToNamed");
		
		reloadPluginMessage = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ReloadPlugin"));
		noPermissionMessage = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission"));
		playersOnlyMessage = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.PlayersOnly"));
		playersNotFoundMessage = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.PlayerNotFound"));
		toggleDisplayOnMessage = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ToggleDisplayOn"));
		toggleDisplayOffMessage = ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ToggleDisplayOff"));
		
		List<String> types = plugin.getConfig().getStringList("Options.DisabledMobTypes");
		for (String each : types) {
			if (version.isLegacy()) {
				disabledMobTypes.add(EntityType.fromName(each.toUpperCase()));
			} else {
				disabledMobTypes.add(EntityType.valueOf(each.toUpperCase()));
			}
		}
		disabledMobNamesAbsolute = plugin.getConfig().getStringList("Options.DisabledMobNamesAbsolute").stream().collect(Collectors.toSet());
		disabledMobNamesContains = plugin.getConfig().getStringList("Options.DisabledMobNamesContains");
		
		disabledWorlds = plugin.getConfig().getStringList("Options.DisabledWorlds").stream().collect(Collectors.toSet());
		
		useAlterHealth = plugin.getConfig().getBoolean("Options.DynamicHealthDisplay.Use");
		altHealthDisplayTime = plugin.getConfig().getInt("Options.DynamicHealthDisplay.Timeout");
		altOnlyPlayer = plugin.getConfig().getBoolean("Options.DynamicHealthDisplay.OnlyPlayerTrigger");
		
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
		
		updaterEnabled = plugin.getConfig().getBoolean("Updater.Enable");
		
		CustomPlaceholderScripts.clearScripts();
		CustomPlaceholderScripts.loadScriptsFromFolder(new File(plugin.getDataFolder(), "placeholder_scripts"));
	}
	
	public static int getUpdateRange(World world) {
		if (version.isOld()) {
			return 80;
		}
		return Math.min((version.isNewerOrEqualTo(MCVersion.V1_16) ? world.getViewDistance() : Bukkit.getViewDistance()) * 16 / 2, 80);
	}
	
}