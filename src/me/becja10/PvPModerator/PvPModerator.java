package me.becja10.PvPModerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import Listeners.InvisibleListener;
import Managers.PvPPlayerManager;


public class PvPModerator extends JavaPlugin implements Listener{

	public static PvPModerator instance;
	public final static Logger logger = Logger.getLogger("Minecraft");
	
	private HashMap<UUID, BlockedPlayer> blockedPlayers;
	private HashMap<UUID, Long> recentlyWarned;
	private HashMap<UUID, Long> combatPlayers;
	public ArrayList<UUID> invisiblePlayers;
	public ArrayList<UUID> invisibleCooldown;
	public ArrayList<PvPPlayer> players;
	
	public PvPPlayerManager pvPPlayerManager;
	public InvisibleListener invisibleListener;
	
	private String configPath;
	private FileConfiguration config;
	private FileConfiguration outConfig;
	
	public int invisibleCooldownTime = 10000;
	
	//Config Settings
	private long tpBuffer; private String _tpBuffer = "Buffers.Teleport";
	private long newPlayerBuffer; private String _newPlayerBuffer = "Buffers.New players";
	
	private boolean allowNewbieCancel; private String _newbieCancel = "Settings.Allow new players to remove protection";
	private boolean safeInvisible; private String _safeInvisible = "Settings.Invisible players are safe from pvp";
	
	public PvPModerator() {
		instance = this;
	}

	public static PvPModerator getInstance() {
		return instance;
	}	
	
	

	
	private void loadConfig(){
		configPath = this.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
		config = YamlConfiguration.loadConfiguration(new File(configPath));
		outConfig = new YamlConfiguration();
		
		tpBuffer = config.getLong(_tpBuffer, 15);
		newPlayerBuffer = config.getLong(_newPlayerBuffer, 3600);
		
		allowNewbieCancel = config.getBoolean(_newbieCancel, true);
		safeInvisible = config.getBoolean(_safeInvisible, true);
		
		outConfig.set(_tpBuffer, tpBuffer);
		outConfig.set(_newPlayerBuffer, newPlayerBuffer);
		outConfig.set(_newbieCancel, allowNewbieCancel);
		outConfig.set(_safeInvisible, safeInvisible);
		
		saveConfig(outConfig, configPath);
		
		tpBuffer *= 1000L; //convert to ms
		newPlayerBuffer *= 1000L; //convert to ms		
	}
	
	private void saveConfig(FileConfiguration config, String path)
	{
        try{config.save(path);}
        catch(IOException exception){logger.info("Unable to write to the configuration file at \"" + path + "\"");}
	}
	
	@Override
	public void onEnable(){
		
		Bukkit.getServer().getPluginManager().registerEvents(new InvisibleListener(), this);
		pvPPlayerManager = new PvPPlayerManager();
		
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginManager manager = getServer().getPluginManager();

		logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " has been enabled!");
		instance = this;
		
		NewbieStorage.setUpStorage(this, logger);
		
		blockedPlayers = NewbieStorage.loadStorage();
		recentlyWarned = new HashMap<UUID, Long>();
		invisiblePlayers = new ArrayList<UUID>();
		invisibleCooldown = new ArrayList<UUID>();
		combatPlayers = new HashMap<UUID, Long>();
		
		players = new ArrayList<PvPPlayer>();
		
		manager.registerEvents(this, this);
		
		loadConfig();	
		
		//new removeInvisplayer().runTaskTimer(this, 20, 10);
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
		switch(cmd.getName().toLowerCase()){
			case "pvpmodreload":
				if(sender instanceof Player && !sender.hasPermission("pvpmoderator.admin"))
					sender.sendMessage(ChatColor.RED + "You can't make me :P");
				else{
					NewbieStorage.reloadNewbieStorages();
					loadConfig();
					sender.sendMessage(ChatColor.GREEN + "[PvPModerator] Reload successful");
				}
				break;
			case "removeprotection":
				if(sender instanceof Player){
					Player p = (Player) sender;
					if(blockedPlayers.containsKey(p.getUniqueId())){
						blockedPlayers.remove(p.getUniqueId());
						p.sendMessage(ChatColor.GREEN + "Your protection has been removed.");
					}
					else{
						p.sendMessage(ChatColor.RED + "You aren't under any protection.");
					}
				}
				break;
			case "amiprotected":
				if(sender instanceof Player){
					Player p = (Player) sender;
					if(isPlayerProtected(p))
						p.sendMessage(ChatColor.GREEN + "Yes");
					else
						p.sendMessage(ChatColor.RED + "No");
				}
		}
		return true;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event){
		if(NewbieStorage.isNewPlayer(event.getPlayer().getUniqueId())){
			NewbieStorage.addNewbie(event.getPlayer().getUniqueId(), newPlayerBuffer);
			addToBlocked(event.getPlayer().getUniqueId(), newPlayerBuffer, BlockedReason.NewPlayer);
		}
		
		PvPPlayer player = new PvPPlayer(event.getPlayer().getUniqueId(), event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY));
		long playTime = (event.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK) / 20) * 1000;
		if(System.currentTimeMillis() - playTime < 1000 * 60 * 60 * 6){
			player.setReason(BlockedReason.NewPlayer, System.currentTimeMillis() + newPlayerBuffer);
		}
		
		players.add(player);
	}
	
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
    	PvPPlayer pp = pvPPlayerManager.getPvPPlayerObject(event.getPlayer().getUniqueId());
        if (pp != null){
        	pp.set_CooldownActivate(false);
        	pp.set_isInvisible(false);
        	players.remove(pvPPlayerManager.getPvPPlayerObject(event.getPlayer().getUniqueId()));
        }
    }
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTP(PlayerTeleportEvent event){
		if(event.isCancelled())
			return;
		if(event.getCause() == TeleportCause.COMMAND || event.getCause() == TeleportCause.PLUGIN){
			BlockedPlayer bp = blockedPlayers.get(event.getPlayer().getUniqueId());
			//don't put in list if new player because it breaks the newbie timing
			if(bp == null || bp.reason == BlockedReason.TPEvent){
				addToBlocked(event.getPlayer().getUniqueId(), tpBuffer, BlockedReason.TPEvent);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageByEntityEvent event){
		Player attacker = null;
		Player defender = null;
		
		if(event.getEntity() instanceof Player)
			defender = (Player) event.getEntity();
		
		attacker = getAttacker(event.getDamager());
		
		if(attacker == null || attacker == defender || defender == null){
			return;
		}
		
		if(invisiblePlayers.contains(attacker.getUniqueId())){
			sendWarning(attacker,ChatColor.GREEN + "You cannot pvp whilst invisible.");
			event.setDamage(0);
			event.setCancelled(true);
		}
		
		if(invisibleCooldown.contains(attacker.getUniqueId())){
			sendWarning(attacker,ChatColor.GREEN + "You cannot pvp whilst on cooldown from being invisible");
			event.setDamage(0);
			event.setCancelled(true);
		}
		
		if(invisiblePlayers.contains(defender.getUniqueId())){
			sendWarning(attacker,ChatColor.GREEN + "You cannot pvp whilst other player is invisible.");
			event.setDamage(0);
			event.setCancelled(true);
		}
		
		if(invisibleCooldown.contains(defender.getUniqueId())){
			sendWarning(attacker,ChatColor.GREEN + "You cannot pvp whilst other player is on cooldown from being invisible.");
			event.setDamage(0);
			event.setCancelled(true);
		}
		
		if(shouldCancel(defender, attacker)){
			event.setCancelled(true);
			event.setDamage(0);
			sendWarningIfNeeded(defender, attacker);
		}
	}
	

	private void sendWarning(Player p, String msg) {
		Long vt = recentlyWarned.get(p.getUniqueId());
		if(vt == null || vt <= System.currentTimeMillis()){
			recentlyWarned.put(p.getUniqueId(), System.currentTimeMillis() + 3000);
			p.sendMessage(msg);
		}
		
	}
	
	private void sendWarningIfNeeded(Player vic, Player perp){
		BlockedPlayer v = blockedPlayers.get(vic.getUniqueId());
		BlockedPlayer p = blockedPlayers.get(perp.getUniqueId());
		
		if(v != null){
			Long vt = recentlyWarned.get(vic.getUniqueId());
			//warn if they either aren't in the list, or they weren't warned in the last 5 seconds
			if(vt == null || vt <= System.currentTimeMillis()){
				recentlyWarned.put(vic.getUniqueId(), System.currentTimeMillis() + 5000);//warn them again in 5 seconds
				vic.sendMessage(v.getWarnMessage(true, null));
				if(p == null)
					perp.sendMessage(v.getWarnMessage(false, v));
			}
		}
		
		if(p != null){
			Long pt = recentlyWarned.get(perp.getUniqueId());
			if(pt == null || pt <= System.currentTimeMillis()){
				recentlyWarned.put(perp.getUniqueId(), System.currentTimeMillis() + 5000);
				perp.sendMessage(p.getWarnMessage(false, v));
			}
		}
	}
	
	private boolean shouldCancel(Player vic, Player perp){
		//don't cancel for admins
		if(perp != null && perp.hasPermission("pvpmoderator.admin"))
			return false;
		return isPlayerProtected(vic) || isPlayerProtected(perp); //block if either vic or perp should have pvp cancelled
	}
	
	private boolean isPlayerProtected(Player p){
		if(p == null)
			return false;
		

		
		BlockedPlayer bp = blockedPlayers.get(p.getUniqueId());
		
		if(bp != null){
			if(bp.time <= System.currentTimeMillis())
				blockedPlayers.remove(p.getUniqueId()); //if it's past the time they are blocked till, remove them,
			else //otherwise they are protected
				return true;
		}
		
		return false;		
	}
	
	private Player getAttacker(Entity damageSource){
		
		Player attacker = null;
		
		//if the damaged is caused by a player
		if(damageSource instanceof Player)
			attacker = (Player) damageSource;
	  
		//if the damage came from an Arrow
		else if (damageSource instanceof Arrow)
		{
			Arrow arrow = (Arrow) damageSource;
			//and the shooter is a player
			if(arrow.getShooter() instanceof Player)
				attacker = (Player)arrow.getShooter();
		}
		//check if the damage was caused by a thrown potion
		else if(damageSource instanceof ThrownPotion)
		{
			ThrownPotion potion = (ThrownPotion)damageSource;
			//and the thrower is a player
			if(potion.getShooter() instanceof Player)
				attacker = (Player)potion.getShooter();
		}
		
		else if(damageSource instanceof Egg)
		{
			Egg egg = (Egg) damageSource;
			if(egg.getShooter() instanceof Player)
				attacker = (Player)egg.getShooter();
		}
		
		else if(damageSource instanceof Snowball)
		{
			Snowball ball = (Snowball) damageSource;
			if(ball.getShooter() instanceof Player)
				attacker = (Player)ball.getShooter();
		}
		
		else if(damageSource instanceof FishHook)
		{
			FishHook hook = (FishHook) damageSource;
			if(hook.getShooter() instanceof Player)
				attacker = (Player)hook.getShooter();
		}
		
		else if(damageSource instanceof EnderPearl)
		{
			EnderPearl pearl = (EnderPearl) damageSource;
			if(pearl.getShooter() instanceof Player)
				attacker = (Player)pearl.getShooter();
		}
		
		return attacker;
	}
	
	private void addToBlocked(UUID id, long time, BlockedReason reason){
		blockedPlayers.put(id, new BlockedPlayer(System.currentTimeMillis() + time, reason));
	}
}
