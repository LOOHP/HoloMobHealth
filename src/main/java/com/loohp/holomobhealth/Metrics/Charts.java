package com.loohp.holomobhealth.Metrics;

import java.util.concurrent.Callable;

import org.bukkit.Bukkit;

import com.loohp.holomobhealth.Registries.CustomPlaceholderScripts;

public class Charts {
	
	public static void setup(Metrics metrics) {
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_mobs_displaying", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return Bukkit.getWorlds().stream().mapToInt(world -> world.getEntities().size()).sum();
            }
        }));
		
		metrics.addCustomChart(new Metrics.SingleLineChart("total_custom_placeholder_scripts", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return CustomPlaceholderScripts.getScriptsCount();
            }
        }));
	}

}
