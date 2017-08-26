package me.becja10.PvPModerator;

import java.util.UUID;

import org.bukkit.ChatColor;

public class PvPPlayer{

	private UUID _id;
	
	private boolean _inCombat;
	private long _combatStarted;
	
	private boolean _isInvisible;
	
	private boolean _isBlocked;
	private long _unblockedTime;
	private BlockedReason _reason;
	
	public PvPPlayer(UUID id, boolean invisible){
		_id = id;
		
		_isInvisible = invisible;
		
		_inCombat = _isBlocked = false;
		_combatStarted = _unblockedTime = 0L;
		
	}
	
	public void setReason(BlockedReason r, long unblockedTime){
		_reason = r;
	}
	
	public String getWarnMessage(boolean isTarget, BlockedPlayer vic)
	{
		String ret = "";
		
		if(isTarget){
			switch(_reason){
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
				switch(_reason){
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
