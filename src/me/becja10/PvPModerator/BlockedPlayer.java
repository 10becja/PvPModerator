package me.becja10.PvPModerator;

import org.bukkit.ChatColor;

public class BlockedPlayer {

	public long time;
	public int reason;
	
	public static final int NEW_PLAYER = 0;
	public static final int TP_EVENT = 1;
		
	public BlockedPlayer(long l, int r){
		time = l;
		reason = r;
	}
	
	public String getWarnMessage(boolean isTarget, BlockedPlayer vic)
	{
		String ret = "";
		
		if(isTarget){
			switch(reason){
			case NEW_PLAYER:
				ret = ChatColor.GREEN + "You're protected from PvP because you are new. Use " + 
					  ChatColor.YELLOW + "/removeprotection" + ChatColor.GREEN + " if you wish to PvP.";
				break;
			case TP_EVENT:
				ret = ChatColor.GREEN + "You've recently teleported and must wait before fighting.";
				break;
			}
		}
		else{
			if(vic == null){
				ret = ChatColor.RED + "You cannot PvP while protected. Use " + ChatColor.YELLOW + 
						"/removeprotection" + ChatColor.RED + " if you wish to PvP";
			}
			else{			
				switch(reason){
				case NEW_PLAYER:
					ret = ChatColor.RED + "This player is new! You can't kill them yet.";
					break;
				case TP_EVENT:
					ret = ChatColor.RED + "This player is in tp cooldown. You can't attack them yet.";
					break;
				}
			}
		}
		
		return ret;
	}
	
}
