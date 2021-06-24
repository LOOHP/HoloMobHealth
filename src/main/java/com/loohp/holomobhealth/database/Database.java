package com.loohp.holomobhealth.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.FileConfiguration;

import com.loohp.holomobhealth.HoloMobHealth;
import com.loohp.holomobhealth.protocol.EntityMetadata;

import net.md_5.bungee.api.ChatColor;

public class Database {
	
	public static boolean isMYSQL = false;
	
	private static Connection connection;
	private static FileConfiguration config = HoloMobHealth.getConfiguration();
	private static String host, database, username, password;
	private static String table = "HoloMobHealth_USER_PERFERENCES";
	private static int port;
    
    private static Object syncdb = new Object();
	
	public static void setup() {
		String type = config.getString("Database.Type");
		if (type.equalsIgnoreCase("MYSQL")) {
			isMYSQL = true;
		} else {
			isMYSQL = false;
		}
		synchronized (syncdb) {
			if (isMYSQL) {
				mysqlSetup(true);
				createTable();
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				sqliteSetup(true);
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void open() {
		if (isMYSQL) {
			mysqlSetup(false);
		} else {
			sqliteSetup(false);
		}
	}
	
	public static void mysqlSetup(boolean echo) {
        host = config.getString("Database.MYSQL.Host");
        port =  config.getInt("Database.MYSQL.Port");
        database = config.getString("Database.MYSQL.Database");
        username = config.getString("Database.MYSQL.Username");
        password = config.getString("Database.MYSQL.Password");

        try {
			if (getConnection() != null && !getConnection().isClosed()) {
				return;
			}
			
			Class.forName("com.mysql.jdbc.Driver");
			setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
			
			if (echo == true) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[HoloMobHealth] MYSQL CONNECTED");
			}
		} catch (SQLException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] MYSQL Failed to connect! (SQLException)");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] MYSQL Failed to connect! (ClassNotFoundException)");
			e.printStackTrace();
		}
	}
	
	public static void sqliteSetup(boolean echo) {	   
		try {
			Class.forName("org.sqlite.JDBC");
	        connection = DriverManager.getConnection("jdbc:sqlite:plugins/HoloMobHealth/database.db");
	        if (echo) {
	        	Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[HoloMobHealth] Opened Sqlite database successfully");
	        }

	        Statement stmt = connection.createStatement();
	        String sql = "CREATE TABLE IF NOT EXISTS " + table + " " +
	                      "(UUID TEXT PRIMARY KEY, " +
	                       "NAME TEXT NOT NULL, " + 
	                       "DISPLAY BOOLEAN);"; 
	        stmt.executeUpdate(sql);
	        stmt.close(); 
	    } catch (Exception e) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[HoloMobHealth] Unable to connect to sqlite database!!!");
	    	e.printStackTrace();
	    	HoloMobHealth.plugin.getPluginLoader().disablePlugin(HoloMobHealth.plugin);
	    }
	}

	public static Connection getConnection() {
		return connection;
	}

	public static void setConnection(Connection connection) {
		Database.connection = connection;
	}
	
    public static void createTable() {
    	synchronized (syncdb) {
	    	open();
	        try {
	        	PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (UUID Text, NAME Text, DISPLAY BOOLEAN)");
	
	            statement.execute();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public static boolean playerExists(Player player) {
    	return playerExists(player.getUniqueId());
    }
    
	public static boolean playerExists(UUID uuid) {
		synchronized (syncdb) {
			boolean exist = false;
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
				statement.setString(1, uuid.toString());
	
				ResultSet results = statement.executeQuery();
				if (results.next()) {
					exist = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return exist;
		}
	}

	public static void createPlayer(Player player) {
		synchronized (syncdb) {
			open();
			try {
				PreparedStatement insert = getConnection().prepareStatement("INSERT INTO " + table + " (UUID,NAME,DISPLAY) VALUES (?,?,?)");
				insert.setString(1, player.getUniqueId().toString());
				insert.setString(2, player.getName());
				insert.setBoolean(3, true);
				insert.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean toggle(Player player) {
		synchronized (syncdb) {
			open();
			boolean newvalue = true;
			if (HoloMobHealth.playersEnabled.contains(player)) {
				newvalue = false;
				HoloMobHealth.playersEnabled.remove(player);
			} else {
				HoloMobHealth.playersEnabled.add(player);
			}
			try {
				PreparedStatement statement = getConnection().prepareStatement("UPDATE " + table + " SET DISPLAY=? WHERE UUID=?");
				statement.setBoolean(1, newvalue);
				statement.setString(2, player.getUniqueId().toString());
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updatePlayer(player), 10);
			return newvalue;
		}
	}
	
	public static HashMap<String, Boolean> getPlayerInfo(UUID uuid) {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		synchronized (syncdb) {
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
				statement.setString(1, uuid.toString());
				ResultSet results = statement.executeQuery();
				results.next();
				
				map.put("display", results.getBoolean("DISPLAY"));			
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	
	public static HashMap<String, Boolean> getPlayerInfo(Player player) {
		return getPlayerInfo(player.getUniqueId());
	}
	
	public static void loadPlayer(Player player) {
		synchronized (syncdb) {
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
				statement.setString(1, player.getUniqueId().toString());
				ResultSet results = statement.executeQuery();
				results.next();
				
				boolean display = results.getBoolean("DISPLAY");
				
				if (HoloMobHealth.playersEnabled.contains(player)) {
					if (!display) {
						HoloMobHealth.playersEnabled.remove(player);
					}
				} else {
					if (display) {
						HoloMobHealth.playersEnabled.add(player);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Bukkit.getScheduler().runTaskLater(HoloMobHealth.plugin, () -> EntityMetadata.updatePlayer(player), 10);
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
