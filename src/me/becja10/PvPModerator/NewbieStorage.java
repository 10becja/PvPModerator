package me.becja10.PvPModerator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class NewbieStorage {

	private static Logger logger;
	private static FileConfiguration config = null;
	private static File NewbieStorages = null;
	private static String path;
	
	public static FileConfiguration getNewbieStorage() {
		/*
		 *     
		 */
		if (config == null)
			reloadNewbieStorages();
		return config;
	}

	public static void reloadNewbieStorages() {
		if (NewbieStorages == null)
			NewbieStorages = new File(path);
		config = YamlConfiguration.loadConfiguration(NewbieStorages);
	}
	
	public static void saveNewbieStorages() {
		if ((config == null) || (NewbieStorages == null))
			return;
		try {
			getNewbieStorage().save(NewbieStorages);
		} catch (IOException ex) {
			logger.warning("Unable to write to the file \"" + path + "\"");
		}
	}
	
	public static void setUpStorage(JavaPlugin plugin, Logger log){
		path = plugin.getDataFolder().getAbsolutePath()	+ File.separator + "NewbieStorages".toLowerCase();
		reloadNewbieStorages();		
	}
	
	public static boolean isNewPlayer(UUID id){
		return !config.contains(id.toString());
	}
	
	/**
	 * Stores players who have joined and when their time will be expired
	 * @param id player uuid
	 * @param time when they aren't a new player anymore
	 */
	public static void addNewbie(UUID id, long time){
		config.set(id.toString(), System.currentTimeMillis() + time);
		saveNewbieStorages();
	}
	
	public static HashMap<UUID, Long> loadStorage(){
		HashMap<UUID, Long> ret = new HashMap<UUID, Long>();
		boolean needSave = false;
		for(String key : config.getKeys(false)){
			UUID id;
			try{
				id = UUID.fromString(key);
			}
			catch(IllegalArgumentException ex){
				config.set(key, null);
				needSave = true;
				continue;
			}
			
			long time = config.getLong(key, 0);
			//if the time stored is less than now, they aren't a newbie anymore
			if(time > System.currentTimeMillis())
				ret.put(id, time);
		}
		
		if(needSave)
			saveNewbieStorages();
		
		return ret;
	}
}

