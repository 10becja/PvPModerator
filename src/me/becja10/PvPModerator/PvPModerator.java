package me.becja10.PvPModerator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PvPModerator extends JavaPlugin implements Listener{

	public static PvPModerator instance;
	public final static Logger logger = Logger.getLogger("Minecraft");
	
	private HashMap<UUID, Long> blockedPlayers;

	
	private String configPath;
	private FileConfiguration config;
	private FileConfiguration outConfig;
	
	//Config Settings
	private long tpBuffer; private String _tpBuffer = "Buffers.Teleport";
	private long newPlayerBuffer; private String _newPlayerBuffer = "Buffers.New players";
	
	private boolean allowNewbieCancel; private String _newbieCancel = "Settings.Allow new players to remove protection";
	
		
	private void loadConfig(){
		configPath = this.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
		config = YamlConfiguration.loadConfiguration(new File(configPath));
		outConfig = new YamlConfiguration();
		
		tpBuffer = config.getLong(_tpBuffer, 15) * 1000L;
		newPlayerBuffer = config.getLong(_newPlayerBuffer, 3600) * 1000L;
		
		allowNewbieCancel = config.getBoolean(_newbieCancel, true);
		
		outConfig.set(_tpBuffer, tpBuffer);
		outConfig.set(_newPlayerBuffer, newPlayerBuffer);
		outConfig.set(_newbieCancel, allowNewbieCancel);
		
		saveConfig(outConfig, configPath);		
	}
	
	private void saveConfig(FileConfiguration config, String path)
	{
        try{config.save(path);}
        catch(IOException exception){logger.info("Unable to write to the configuration file at \"" + path + "\"");}
	}
	
	@Override
	public void onEnable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginManager manager = getServer().getPluginManager();

		logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " has been enabled!");
		instance = this;
		
		NewbieStorage.setUpStorage(this, logger);
		
		blockedPlayers = NewbieStorage.loadStorage();
		
		manager.registerEvents(this, this);
		
		loadConfig();		
	}
		
	@Override
	public void onDisable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " Has Been Disabled!");
		saveConfig(outConfig, configPath);

	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event){
		if(NewbieStorage.isNewPlayer(event.getPlayer().getUniqueId())){
			NewbieStorage.addNewbie(event.getPlayer().getUniqueId(), newPlayerBuffer);			
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTP(PlayerTeleportEvent event){
		if(event.getCause() == TeleportCause.COMMAND || event.getCause() == TeleportCause.PLUGIN){
			addToBlocked(event.getPlayer().getUniqueId(), tpBuffer);
		}
	}
	
	
	
	private void addToBlocked(UUID id, long time){
		blockedPlayers.put(id, System.currentTimeMillis() + time);
	}
}
