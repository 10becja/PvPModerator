package me.becja10.PvPModerator;

import org.bukkit.ChatColor;

public class BlockedPlayer {

	public long time;
	public BlockedReason reason;
	
	public BlockedPlayer(long l, BlockedReason r){
		time = l;
		reason = r;
	}
	
	public String getWarnMessage(boolean isTarget, BlockedPlayer vic)
	{
		String ret = "";
		
		if(isTarget){
			switch(reason){
			case NewPlayer:
				ret = ChatColor.GREEN + "You're protected from PvP because you are new. Use " + 
					  ChatColor.YELLOW + "/removeprotection" + ChatColor.GREEN + " if you wish to PvP.";
				break;
			case TPEvent:
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
				case NewPlayer:
					ret = ChatColor.RED + "This player is new! You can't kill them yet.";
					break;
				case TPEvent:
					ret = ChatColor.RED + "This player is in tp cooldown. You can't attack them yet.";
					break;
				}
			}
		}
		
		return ret;
	}
	
}
