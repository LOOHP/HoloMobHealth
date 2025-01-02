/*
 * This file is part of HoloMobHealth.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.holomobhealth;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.holomobhealth.api.HoloMobHealthAPI;
import com.loohp.holomobhealth.config.Config;
import com.loohp.holomobhealth.database.Database;
import com.loohp.holomobhealth.debug.Debug;
import com.loohp.holomobhealth.listeners.Events;
import com.loohp.holomobhealth.metrics.Charts;
import com.loohp.holomobhealth.metrics.Metrics;
import com.loohp.holomobhealth.modules.ArmorstandDisplay;
import com.loohp.holomobhealth.modules.DamageIndicator;
import com.loohp.holomobhealth.modules.NameTagDisplay;
import com.loohp.holomobhealth.modules.RangeModule;
import com.loohp.holomobhealth.nms.NMS;
import com.loohp.holomobhealth.placeholderapi.Placeholders;
import com.loohp.holomobhealth.protocol.ArmorStandPacket;
import com.loohp.holomobhealth.registries.CustomPlaceholderScripts;
import com.loohp.holomobhealth.registries.DisplayTextCacher;
import com.loohp.holomobhealth.updater.Updater;
import com.loohp.holomobhealth.updater.Updater.UpdaterResponse;
import com.loohp.holomobhealth.utils.ChatColorUtils;
import com.loohp.holomobhealth.utils.JarUtils;
import com.loohp.holomobhealth.utils.JarUtils.CopyOption;
import com.loohp.holomobhealth.utils.LanguageUtils;
import com.loohp.holomobhealth.utils.LegacyPlaceholdersConverter;
import com.loohp.holomobhealth.utils.MCVersion;
import com.loohp.holomobhealth.utils.ModelEngineUtils;
import com.loohp.holomobhealth.utils.PacketSender;
import com.loohp.holomobhealth.utils.WorldGuardUtils;
import com.loohp.yamlconfiguration.YamlConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HoloMobHealth extends JavaPlugin {

    public static final int BSTATS_PLUGIN_ID = 6749;
    public static final String CONFIG_ID = "config";

    public static Plugin plugin = null;

    public static String exactMinecraftVersion;
    public static MCVersion version;

    public static ProtocolManager protocolManager;

    public static int activeShowHealthTaskID = -1;

    public static RoundingMode roundingMode = RoundingMode.UP;

    public static List<String> displayText = new ArrayList<>();

    public static int heartScale = 10;
    public static boolean dynamicScale = true;

    public static Set<Player> playersEnabled = ConcurrentHashMap.newKeySet();

    public static String healthyColor = "&a";
    public static String halfColor = "&e";
    public static String lowColor = "&c";

    public static String healthyChar = "&câ¤";
    public static String halfChar = "&eâ¤";
    public static String emptyChar = "&7â¤";

    public static boolean alwaysShow = true;
    public static boolean applyToNamed = false;

    public static String reloadPluginMessage = "";
    public static String noPermissionMessage = "";
    public static String playersOnlyMessage = "";
    public static String playersNotFoundMessage = "";
    public static String toggleDisplayOnMessage = "";
    public static String toggleDisplayOffMessage = "";

    public static Set<EntityType> disabledMobTypes = new HashSet<>();
    public static Set<String> disabledMobNamesAbsolute = new HashSet<>();
    public static List<String> disabledMobNamesContains = new ArrayList<>();
    public static Set<String> disabledWorlds = new HashSet<>();

    public static boolean useAlterHealth = false;
    public static int altHealthDisplayTime = 3;
    public static boolean altOnlyPlayer = false;
    public static Map<UUID, Long> altShowHealth = new ConcurrentHashMap<>();

    public static boolean idleUse = false;
    public static List<String> idleDisplayText = new ArrayList<>();

    public static boolean useDamageIndicator = true;
    public static boolean damageIndicatorPlayerTriggered = false;
    public static int damageIndicatorVisibleRange = 64;
    public static int damageIndicatorTimeout = 40;
    public static boolean damageIndicatorDamageEnabled = true;
    public static boolean damageIndicatorDamageAnimation = true;
    public static String damageIndicatorDamageText = "";
    public static double damageIndicatorDamageY = 0;
    public static double damageIndicatorDamageMinimum = 0.5;
    public static boolean damageIndicatorRegenEnabled = true;
    public static boolean damageIndicatorRegenAnimation = true;
    public static String damageIndicatorRegenText = "";
    public static double damageIndicatorRegenY = 0;
    public static double damageIndicatorRegenMinimum = 0.5;

    public static boolean placeholderAPIHook = false;

    public static boolean mythicHook = false;
    public static boolean showMythicMobs = true;
    public static boolean useMythicMobCustomNamesFirst = true;

    public static boolean citizensHook = false;
    public static boolean showCitizens = true;

    public static boolean shopkeepersHook = false;
    public static boolean showShopkeepers = true;

    public static boolean myPetHook = false;
    public static boolean showMyPet = true;

    public static boolean ultimateStackerHook = false;
    public static boolean roseStackerHook = false;

    public static boolean modelEngineHook = false;

    public static boolean worldGuardHook = false;

    public static boolean armorStandMode = false;
    public static int armorStandYOffset = 0;

    public static HashMap<EntityType, Integer> specialTypeOffset = new HashMap<>();
    public static HashMap<String, Integer> specialNameOffset = new HashMap<>();

    public static boolean rangeEnabled = false;

    public static boolean updaterEnabled = true;
    public static String language = "en_us";
    public static boolean sendPacketsOnMainThread = false;

    public static YamlConfiguration getConfiguration() {
        return Config.getConfig(CONFIG_ID).getConfiguration();
    }

    public static boolean isPluginEnabled(String name) {
        return isPluginEnabled(name, true);
    }

    public static boolean isPluginEnabled(String name, boolean checkRunning) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && (!checkRunning || plugin.isEnabled());
    }

    @SuppressWarnings("deprecation")
    public static void loadConfig() {
        Config config = Config.getConfig(CONFIG_ID);
        config.reload();

        rangeEnabled = config.getConfiguration().getBoolean("Options.Range.Use");

        roundingMode = RoundingMode.valueOf(config.getConfiguration().getString("Options.NumberRounding").toUpperCase());

        specialNameOffset.clear();
        specialTypeOffset.clear();

        displayText = DisplayTextCacher.cacheDecimalFormat(config.getConfiguration().getStringList("Display.Text"));

        heartScale = config.getConfiguration().getInt("Display.ScaledSymbolSettings.Scale");
        dynamicScale = config.getConfiguration().getBoolean("Display.ScaledSymbolSettings.DynamicScale");

        healthyColor = config.getConfiguration().getString("Display.DynamicColorSettings.HealthyColor");
        halfColor = config.getConfiguration().getString("Display.DynamicColorSettings.HalfColor");
        lowColor = config.getConfiguration().getString("Display.DynamicColorSettings.LowColor");

        healthyChar = config.getConfiguration().getString("Display.ScaledSymbolSettings.HealthyChar");
        halfChar = config.getConfiguration().getString("Display.ScaledSymbolSettings.HalfChar");
        emptyChar = config.getConfiguration().getString("Display.ScaledSymbolSettings.EmptyChar");

        alwaysShow = config.getConfiguration().getBoolean("Options.AlwaysShow");
        applyToNamed = config.getConfiguration().getBoolean("Options.ApplyToNamed");

        reloadPluginMessage = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.ReloadPlugin"));
        noPermissionMessage = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.NoPermission"));
        playersOnlyMessage = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.PlayersOnly"));
        playersNotFoundMessage = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.PlayerNotFound"));
        toggleDisplayOnMessage = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.ToggleDisplayOn"));
        toggleDisplayOffMessage = ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("Messages.ToggleDisplayOff"));

        List<String> types = config.getConfiguration().getStringList("Options.DisabledMobTypes");
        for (String each : types) {
            if (version.isLegacy()) {
                disabledMobTypes.add(EntityType.fromName(each.toUpperCase()));
            } else {
                disabledMobTypes.add(EntityType.valueOf(each.toUpperCase()));
            }
        }
        disabledMobNamesAbsolute = new HashSet<>(config.getConfiguration().getStringList("Options.DisabledMobNamesAbsolute"));
        disabledMobNamesContains = config.getConfiguration().getStringList("Options.DisabledMobNamesContains");

        disabledWorlds = new HashSet<>(config.getConfiguration().getStringList("Options.DisabledWorlds"));

        useAlterHealth = config.getConfiguration().getBoolean("Options.DynamicHealthDisplay.Use");
        altHealthDisplayTime = config.getConfiguration().getInt("Options.DynamicHealthDisplay.Timeout");
        altOnlyPlayer = config.getConfiguration().getBoolean("Options.DynamicHealthDisplay.OnlyPlayerTrigger");

        idleUse = config.getConfiguration().getBoolean("Options.DynamicHealthDisplay.IdleDisplay.Use");
        idleDisplayText = DisplayTextCacher.cacheDecimalFormat(config.getConfiguration().getStringList("Options.DynamicHealthDisplay.IdleDisplay.Text"));

        armorStandMode = config.getConfiguration().getBoolean("Options.MultiLine.Enable");
        armorStandYOffset = config.getConfiguration().getInt("Options.MultiLine.MasterYOffset");
        List<String> armorStandSpecial = config.getConfiguration().getStringList("Options.MultiLine.Special");
        for (String cases : armorStandSpecial) {
            int offset = Integer.parseInt(cases.substring(cases.lastIndexOf(":") + 1));
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
            if (modelEngineHook) {
                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> ModelEngineUtils.updateModelEngineNametags(), 0, 10);
            }
        }

        useDamageIndicator = config.getConfiguration().getBoolean("DamageIndicator.Enabled");
        damageIndicatorVisibleRange = config.getConfiguration().getInt("DamageIndicator.VisibleRange");
        damageIndicatorTimeout = config.getConfiguration().getInt("DamageIndicator.Timeout");
        damageIndicatorPlayerTriggered = config.getConfiguration().getBoolean("DamageIndicator.OnlyPlayerTriggered");
        damageIndicatorDamageEnabled = config.getConfiguration().getBoolean("DamageIndicator.Damage.Enabled");
        damageIndicatorDamageAnimation = config.getConfiguration().getBoolean("DamageIndicator.Damage.Animation");
        damageIndicatorDamageText = DisplayTextCacher.cacheDecimalFormat(ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DamageIndicator.Damage.HoloText")));
        damageIndicatorDamageY = config.getConfiguration().getDouble("DamageIndicator.Damage.Y-Offset");
        damageIndicatorDamageMinimum = config.getConfiguration().getDouble("DamageIndicator.Damage.Minimum");
        damageIndicatorRegenEnabled = config.getConfiguration().getBoolean("DamageIndicator.Regen.Enabled");
        damageIndicatorRegenAnimation = config.getConfiguration().getBoolean("DamageIndicator.Regen.Animation");
        damageIndicatorRegenText = DisplayTextCacher.cacheDecimalFormat(ChatColorUtils.translateAlternateColorCodes('&', config.getConfiguration().getString("DamageIndicator.Regen.HoloText")));
        damageIndicatorRegenY = config.getConfiguration().getDouble("DamageIndicator.Regen.Y-Offset");
        damageIndicatorRegenMinimum = config.getConfiguration().getDouble("DamageIndicator.Regen.Minimum");

        showCitizens = config.getConfiguration().getBoolean("Hooks.Citizens.ShowNPCMobHealth");
        showMythicMobs = config.getConfiguration().getBoolean("Hooks.MythicMobs.ShowMythicMobsHealth");
        useMythicMobCustomNamesFirst = config.getConfiguration().getBoolean("Hooks.MythicMobs.UseMythicMobCustomNamesFirst");
        showShopkeepers = config.getConfiguration().getBoolean("Hooks.Shopkeepers.ShowShopkeepersHealth");
        showMyPet = config.getConfiguration().getBoolean("Hooks.MyPet.ShowMyPetHealth");

        updaterEnabled = config.getConfiguration().getBoolean("Updater.Enable");

        language = config.getConfiguration().getString("Options.Language");

        sendPacketsOnMainThread = config.getConfiguration().getBoolean("Settings.SendPacketsOnMainThread");

        CustomPlaceholderScripts.clearScripts();
        CustomPlaceholderScripts.loadScriptsFromFolder(new File(plugin.getDataFolder(), "placeholder_scripts"));

        boolean silentClassNotFound = config.getConfiguration().getBoolean("CustomPlaceholderScript.SilentClassNotFound");
        for (String key : config.getConfiguration().getConfigurationSection("CustomPlaceholderScript.AdditionClasses").getValues(false).keySet()) {
            String classPath = config.getConfiguration().getString("CustomPlaceholderScript.AdditionClasses." + key);
            try {
                Class<?> clazz = Class.forName(classPath);
                HoloMobHealthAPI.registerClassToCustomPlaceholderScript(key, clazz);
            } catch (ClassNotFoundException e) {
                if (!silentClassNotFound) {
                    e.printStackTrace();
                }
            }
        }

        LanguageUtils.loadTranslations(language);
    }

    public static int getUpdateRange(World world) {
        if (version.isOld()) {
            return 80;
        } else if (version.isNewerOrEqualTo(MCVersion.V1_18)) {
            return Math.min(Math.min(world.getViewDistance() << 4, world.getSimulationDistance() << 4), 80);
        } else if (version.isNewerOrEqualTo(MCVersion.V1_16)) {
            return Math.min((world.getViewDistance() << 4) / 2, 80);
        } else {
            return Math.min((Bukkit.getViewDistance() << 4) / 2, 80);
        }
    }

    public static void sendMessage(CommandSender sender, Component component) {
        if (version.isLegacyRGB()) {
            try {
                sender.spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.colorDownsamplingGson().serialize(component)));
            } catch (Throwable e) {
                if (sender instanceof Player) {
                    ((Player) sender).spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.colorDownsamplingGson().serialize(component)));
                } else {
                    sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component));
                }
            }
        } else {
            sender.spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(component)));
        }
    }

    @Override
    public void onLoad() {
        if (isPluginEnabled("WorldGuard", false)) {
            String version = getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
            if (version.startsWith("7.")) {
                getServer().getLogger().info("[HoloMobHealth] Registering WorldGuard State Flags...");
                WorldGuardUtils.registerFlag();
                worldGuardHook = true;
            }
        }
    }

    @Override
    public void onEnable() {
        plugin = this;

        exactMinecraftVersion = Bukkit.getVersion().substring(Bukkit.getVersion().indexOf("(") + 5, Bukkit.getVersion().indexOf(")"));
        version = MCVersion.resolve();

        getServer().getPluginManager().registerEvents(new Debug(), this);
        if (version.isNewerOrEqualTo(MCVersion.V1_11)) {
            getServer().getPluginManager().registerEvents(new DamageIndicator(), this);
        }

        Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        Charts.setup(metrics);

        if (!version.isSupported()) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        getServer().getPluginManager().registerEvents(new Events(), this);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        try {
            Config.loadConfig(CONFIG_ID, new File(getDataFolder(), "config.yml"), getClass().getClassLoader().getResourceAsStream("config.yml"), getClass().getClassLoader().getResourceAsStream("config.yml"), true);
        } catch (IOException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            JarUtils.copyFolderFromJar("placeholder_scripts", getDataFolder(), CopyOption.COPY_IF_NOT_EXIST);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        //Legacy Placeholders Converter
        List<String> lines = getConfiguration().getStringList("Display.Text");
        if (LegacyPlaceholdersConverter.getLegacyPlaceholderSet().stream().anyMatch(each -> lines.stream().anyMatch(line -> line.contains(each)))) {
            LegacyPlaceholdersConverter.convert();
        }

        loadConfig();

        if (armorStandMode) {
            getServer().getPluginManager().registerEvents(new ArmorStandPacket(), plugin);
            ArmorStandPacket.update();
        }

        getCommand("holomobhealth").setExecutor(new Commands());

        if (isPluginEnabled("Citizens")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into Citizens!");
            citizensHook = true;
        }

        if (isPluginEnabled("MythicMobs")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into MythicMobs!");
            mythicHook = true;
        }

        if (isPluginEnabled("Shopkeepers")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into Shopkeepers!");
            shopkeepersHook = true;
        }

        if (isPluginEnabled("MyPet")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into MyPet!");
            myPetHook = true;
        }

        if (isPluginEnabled("UltimateStacker")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into UltimateStacker!");
            ultimateStackerHook = true;
        }

        if (isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into PlaceholderAPI!");
            placeholderAPIHook = true;
        }

        if (isPluginEnabled("UltimateStacker")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into UltimateStacker!");
            ultimateStackerHook = true;
        }

        if (isPluginEnabled("RoseStacker")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into RoseStacker!");
            roseStackerHook = true;
        }

        if (isPluginEnabled("ModelEngine", false)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into ModelEngine!");
            modelEngineHook = true;
        }

        if (worldGuardHook) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[HoloMobHealth] Hooked into WorldGuard! (v7)");
        }

        if (rangeEnabled) {
            RangeModule.reloadNumbers();
            RangeModule.run();
        }

        Database.setup();

        if (updaterEnabled) {
            getServer().getPluginManager().registerEvents(new Updater(), this);
        }

        ArmorstandDisplay.run();

        if (isPluginEnabled("PlaceholderAPI")) {
            new Placeholders().register();
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
                    ArmorStandPacket.playerStatus.put(player, new ConcurrentHashMap<>());
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
                int[] entityIdArray = ArmorStandPacket.active.stream().mapToInt(each -> each.getEntityId()).toArray();
                PacketContainer[] packets = NMS.getInstance().createEntityDestroyPacket(entityIdArray);

                PacketSender.sendServerPackets(Bukkit.getOnlinePlayers(), packets);
            }
        }

        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] HoloMobHealth has been Disabled!");
    }

}
