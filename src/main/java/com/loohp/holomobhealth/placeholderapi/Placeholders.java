package com.loohp.holomobhealth.placeholderapi;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.database.Database;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public String getAuthor() {
        return String.join(", ", HoloMobHealth.plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "holomobhealth";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getRequiredPlugin() {
        return HoloMobHealth.plugin.getName();
    }

    @Override
    public String onRequest(OfflinePlayer offlineplayer, String identifier) {

        if (identifier.equals("displaytoggle")) {
            if (offlineplayer.isOnline()) {
                return HoloMobHealth.playersEnabled.contains(offlineplayer.getPlayer()) ? "enabled" : "disabled";
            } else {
                return Database.getPlayerInfo(offlineplayer.getUniqueId()).getOrDefault("display", true) ? "enabled" : "disabled";
            }
        }

        return null;
    }
}
